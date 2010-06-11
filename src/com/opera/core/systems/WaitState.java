package com.opera.core.systems;

import java.util.LinkedList;
import java.util.logging.Logger;

import org.openqa.selenium.WebDriverException;

import com.opera.core.systems.scope.exceptions.CommunicationException;
import com.opera.core.systems.scope.exceptions.ResponseNotReceivedException;
import com.opera.core.systems.scope.protos.UmsProtos.Response;

/**
 *
 * @author Jan Vidar Krey (janv@opera.com)
 */
public class WaitState {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private boolean connected;
    private Object lock = new Object();

    enum WaitResult
    {
        RESPONSE,       /* Got a response */
        ERROR,          /* Got an error response */
        EXCEPTION,      /* An exception occured (STP connection is not alive) */
        DISCONNECTED,   /* STP connection is disconnected */
        BINARY_EXIT,    /* Opera binary crashed. */
        HANDSHAKE,       /* STP Handshake */
        EVENT_WINDOW_LOADED, /* finished loaded */
    }

    private class ResultItem
    {
        int data;
        WaitResult waitResult;
        WebDriverException exception;
        Response response;

        public ResultItem(WebDriverException ex)
        {
            waitResult = WaitResult.EXCEPTION;
            exception = ex;
            logger.fine("EVENT: " + waitResult.toString() + ", exception: " + ex.toString());
        }

        public ResultItem(WaitResult result)
        {
            this.waitResult = result;
            logger.fine("EVENT: " + result.toString());
        }

        // BINARY_EXIT - with exit code.
        // WINDOW_LOADED with windowId
        // ERROR with tag id
        public ResultItem(WaitResult result, int data)
        {
            this.waitResult = result;
            this.data = data;
            logger.fine("EVENT: " + result.toString() + ", data=" + data);
        }

        public ResultItem(Response response, int tag)
        {
            this.response = response;
            this.data = tag;
            waitResult = WaitResult.RESPONSE;
            logger.fine("EVENT: " + waitResult.toString() + ", data=" + data);
        }
    }

    LinkedList<ResultItem> events = new LinkedList<ResultItem>();

    enum ResponseType {
    	HANDSHAKE,
    	RESPONSE,
    	WINDOW_LOADED;
    }
    public WaitState()
    {
        connected = true;
    }

    private void internalWait(long timeout) throws WebDriverException
    {
        try {
            if (!connected)
                throw new CommunicationException("Waiting aborted - not connected!");
            lock.wait(timeout);
        } catch (InterruptedException e) {
            throw new WebDriverException(e);
        }
    }

    void onHandshake()
    {
        synchronized (lock)
        {
            logger.info("Got handshake");
            events.add(new ResultItem(WaitResult.HANDSHAKE));
            lock.notify();
        }
    }

    void onResponse(int tag, Response response)
    {
        synchronized (lock)
        {
            logger.info("Got response for " + tag);
            events.add(new ResultItem(response, tag));
            lock.notify();
        }
    }

    void onError(int tag)
    {

        synchronized (lock)
        {
            logger.info("Got ERROR for " + tag);
            events.add(new ResultItem(WaitResult.ERROR, tag));
            lock.notify();
        }
    }

    void onException(Exception e)
    {
        synchronized (lock)
        {
            logger.info("Got exception");
            events.add(new ResultItem(new WebDriverException(e)));
            connected = false;
            lock.notify();
        }
    }

    void onDisconnected()
    {
        synchronized (lock)
        {
            logger.info("Got disconnected.");
            events.add(new ResultItem(WaitResult.DISCONNECTED));
            connected = false;
            lock.notify();
        }
    }

    void onBinaryExit(int code)
    {
        synchronized (lock)
        {
            logger.info("Got BinaryExit: exit code=" + code);
            events.add(new ResultItem(WaitResult.BINARY_EXIT, code));
            connected = false;
            lock.notify();
        }
    }

    void onWindowLoaded(int windowId)
    {
        synchronized (lock)
        {
            logger.info("Event: onWindowLoaded");
            events.add(new ResultItem(WaitResult.EVENT_WINDOW_LOADED, windowId));
            lock.notify();
        }
    }

    private ResultItem getResult()
    {
        if (events.isEmpty())
            return null;

        return events.removeFirst();
    }
    
    private final ResultItem pollResultItem(long timeout) {
    	ResultItem result = getResult();

		if (result == null) {
			internalWait(timeout);
			result = getResult();
		}

		if (result == null)
			throw new ResponseNotReceivedException("No response in a timely fashion.");
		
		return result;
    }
    
	private final Response waitAndParseResult(long timeout, int match, final ResponseType type) {
		{
			synchronized (lock) {
				while (true) {
					ResultItem result = pollResultItem(timeout);
					
					WaitResult waitResult = result.waitResult;
					
					switch (waitResult) {
					case HANDSHAKE:
						if (type == ResponseType.HANDSHAKE)
							return null;
						break;

					case RESPONSE:
						if (result.data == match && type == ResponseType.RESPONSE)
							return result.response;
						break;

					case ERROR:
						if (result.data == match && type == ResponseType.RESPONSE)
							return null;
						break;

					case EXCEPTION:
						throw result.exception;

					case DISCONNECTED:
					case BINARY_EXIT:
						throw new CommunicationException( "Problem encountered : " + waitResult.toString());

					case EVENT_WINDOW_LOADED:
						if (result.data == match && type == ResponseType.WINDOW_LOADED)
							return null;
						break;
					}
				}
			}
		}
	}

	public void waitForHandshake(long value) {
		waitAndParseResult(value, 0, ResponseType.HANDSHAKE);
	}

	public void waitForWindowLoaded(int windowId, long timeout) {
		waitAndParseResult(timeout, windowId, ResponseType.WINDOW_LOADED);
	}

	public Response waitFor(int tag, long timeout) {
		return waitAndParseResult(timeout, tag, ResponseType.RESPONSE);
	}
}
