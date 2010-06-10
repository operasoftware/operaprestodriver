package com.opera.core.systems;

import java.util.logging.Logger;
import java.util.LinkedList;

import org.openqa.selenium.WebDriverException;
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
        WaitResult result;
        WebDriverException exception;
        Response response;

        public ResultItem(WebDriverException ex)
        {
            result = WaitResult.EXCEPTION;
            exception = ex;
            logger.fine("EVENT: " + result.toString() + ", exception: " + ex.toString());
        }

        public ResultItem(WaitResult result)
        {
            this.result = result;
            logger.fine("EVENT: " + result.toString());
        }

        // BINARY_EXIT - with exit code.
        // WINDOW_LOADED with windowId
        // ERROR with tag id
        public ResultItem(WaitResult result, int data)
        {
            this.result = result;
            this.data = data;
            logger.fine("EVENT: " + result.toString() + ", data=" + data);
        }

        public ResultItem(Response response, int tag)
        {
            this.response = response;
            this.data = tag;
            result = WaitResult.RESPONSE;
            logger.fine("EVENT: " + result.toString() + ", data=" + data);
        }
    }

    LinkedList<ResultItem> events = new LinkedList<ResultItem>();

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

    void waitForHandshake(long timeout) throws WebDriverException
    {
        synchronized (lock)
        {
            logger.fine("WAIT: for handshake");
            while (true)
            {
                ResultItem result = getResult();

                if (result == null)
                {
                    internalWait(timeout);
                    result = getResult();
                }

                if (result == null)
                    throw new ResponseNotReceivedException("No response in a timely fashion.");

                switch (result.result)
                {
                    case HANDSHAKE:
                        return;

                    case EXCEPTION:
                        throw result.exception;

                    case DISCONNECTED:
                        throw new CommunicationException("Disconnected STP connection.");

                    case BINARY_EXIT:
                        throw new CommunicationException("Binary stopped/crashed.");

                    case EVENT_WINDOW_LOADED:
                        break;

                    default:
                        throw new WebDriverException("Unexpected result, expecting handshake");
                }
            }
        }
    }

    Response waitFor(int tag, long timeout) throws WebDriverException
    {
        synchronized (lock)
        {
            logger.fine("WAIT: for response " + tag);
            while (true)
            {
                ResultItem result = getResult();

                if (result == null)
                {
                    internalWait(timeout);
                    result = getResult();
                }

                if (result == null)
                    throw new ResponseNotReceivedException("No response in a timely fashion.");


                switch (result.result)
                {
                    case RESPONSE:
                        if (result.data == tag)
                            return result.response;
                        break;
                    
                    case ERROR:
                        if (result.data == tag)
                            return null;
                        break;

                    case EXCEPTION:
                        throw result.exception;

                    case DISCONNECTED:
                        throw new CommunicationException("Disconnected STP connection.");

                    case BINARY_EXIT:
                        throw new CommunicationException("Binary stopped/crashed");

                    case EVENT_WINDOW_LOADED:
                        break;
                }
            }
        }
    }

    boolean waitForWindowLoaded(int windowId, long timeout) throws WebDriverException
    {
        synchronized (lock)
        {
            logger.fine("WAIT: for window " + windowId + " to finish loading.");
            while (true)
            {
                ResultItem result = getResult();

                if (result == null)
                {
                    internalWait(timeout);
                    result = getResult();
                }

                if (result == null)
                    throw new ResponseNotReceivedException("No response in a timely fashion.");

                switch (result.result)
                {
                    case RESPONSE:
                        break;

                    case ERROR:
                        break;

                    case EXCEPTION:
                        throw result.exception;

                    case DISCONNECTED:
                        throw new CommunicationException("Disconnected STP connection.");

                    case BINARY_EXIT:
                        throw new CommunicationException("Binary stopped/crashed");

                    case EVENT_WINDOW_LOADED:
                        if (result.data == windowId)
                            return true;
                        break;
                }
            }
        }
    }

}
