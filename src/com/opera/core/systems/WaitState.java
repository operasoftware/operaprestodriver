package com.opera.core.systems;

import java.util.logging.Logger;

import org.openqa.selenium.WebDriverException;
import com.opera.core.systems.scope.exceptions.ResponseNotReceivedException;

/**
 *
 * @author Jan Vidar Krey (janv@opera.com)
 */
public class WaitState {
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	
    private int tag;
    private String xmlResponse;
    private WebDriverException exception = null;
    private int exitCode = 0;
    
    enum State
    {

    }
    
    enum WaitResult
    {
        NONE,           /* Not waiting */
        WAITING,        /* Waiting for event to happen */
        RESPONSE,       /* Got a response */
        XMLRESPONSE,    /* Got an XML response */
        ERROR,          /* Got an error response */
        EXCEPTION,      /* An exception occured (STP connection is not alive) */
        DISCONNECTED,   /* STP connection is disconnected */
        BINARY_EXIT,    /* Opera binary crashed. */
        HANDSHAKE       /* STP Handshake */
    }

    WaitResult result;
    boolean connected;

    public WaitState()
    {
        result = WaitResult.NONE;
        connected = true;
    }

    private void wakeup()
    {
        synchronized (this)
        {
            notifyAll();
        }
    }

    private void internalWait(long timeout) throws WebDriverException
    {
        try {
            synchronized (this)
            {
                synchronized (result)
                {
                    if (result != WaitResult.WAITING)
                        return;
                }
                
                if (!connected)
                    throw new CommunicationException("Waiting aborted - not connected!");
                wait(timeout);
            }
        } catch (InterruptedException e) {
            throw new WebDriverException(e);
        }
    }

    boolean isWaiting()
    {
        synchronized (result)
        {
            return result != WaitResult.NONE;
        }
    }
    
    void onHandshake()
    {
    	logger.info("Got handshake");
        synchronized (result)
        {
            this.result = WaitResult.HANDSHAKE;
        }
        wakeup();
    }

    void onResponse(int tag)
    {
    	logger.info("Got response for " + tag);
        synchronized (result)
        {
            if (tag == this.tag)
            {
                this.result = WaitResult.RESPONSE;
            } else {
                this.result = WaitResult.EXCEPTION;
                exception = new WebDriverException("Protocol error: Tag mismatch!");
            }
        }
        wakeup();
    }
    
    void onXMLResponse(String response)
    {
    	logger.info("Got XML response for " + xmlResponse);
        synchronized (result)
        {
            if (response.contains(xmlResponse)) {
                this.result = WaitResult.XMLRESPONSE;
                this.xmlResponse = response;
            } else {
                this.result = WaitResult.EXCEPTION;
                exception = new WebDriverException("Protocol error: Tag mismatch!");
            }
        }
        wakeup();
    }
    

    void onError(int tag)
    {
    	logger.info("Got ERROR for " + tag);
        synchronized (result)
        {
            if (tag == this.tag)
            {
                this.result = WaitResult.ERROR;
            }
            else
            {
                this.result = WaitResult.EXCEPTION;
                exception = new WebDriverException("Protocol error: Tag mismatch!");
            }
        }
        wakeup();
    }

    void onException(Exception e)
    {
    	logger.info("Got exception for " + tag);
        synchronized (result)
        {
            this.result = WaitResult.EXCEPTION;
            exception = new WebDriverException(e);
            connected = false;
        }
        wakeup();
    }

    void onDisconnected()
    {
        synchronized (this)
        {
            connected = false;
        }

        if (isWaiting())
        {
        	logger.info("Got disconnected for " + tag);
            synchronized (result)
            {
                this.result = WaitResult.DISCONNECTED;
            }
            wakeup();
        }
    }

    void onBinaryExit(int code)
    {
    	logger.info("Got BinaryExit for " + tag + ", exit code=" + code);
        synchronized (result)
        {
            this.result = WaitResult.BINARY_EXIT;
            this.exitCode = code;
        }

        wakeup();
    }
    
    void waitForHandshake(long timeout) throws WebDriverException
    {
        synchronized (result)
        {
            this.result = WaitResult.WAITING;
            this.tag = -1;
            this.exception = null;
        }

        internalWait(timeout);

        synchronized (result)
        {
            WaitResult old_result = result;
            result = WaitResult.NONE;
            switch (old_result)
            {
                case WAITING:
                    throw new ResponseNotReceivedException("No response in a timely fashion.");

                case HANDSHAKE:
                    return;

                case EXCEPTION:
                    throw exception;

                case DISCONNECTED:
		    throw new CommunicationException("Disconnected STP connection.");

                case BINARY_EXIT:
                    throw new CommunicationException("Binary stopped/crashed.");

                default:
                    throw new WebDriverException("Unexpected result, expecting handshake");
            }
        }
    }

    boolean waitFor(int tag, long timeout) throws WebDriverException
    {
        while (true)
        {
            synchronized (result)
            {
                this.result = WaitResult.WAITING;
                this.tag = tag;
                this.exception = null;
            }

            internalWait(timeout);

            synchronized (result)
            {
                WaitResult old_result = result;
                result = WaitResult.NONE;
                switch (old_result)
                {
                    case WAITING:
                        throw new ResponseNotReceivedException("No response in a timely fashion.");

                    case RESPONSE:
                        return true;
                    
                    case XMLRESPONSE:
                        continue;

                    case ERROR:
                        return false;

                    case EXCEPTION:
                        throw exception;

                    case DISCONNECTED:
                        throw new CommunicationException("Disconnected STP connection.");

                    case BINARY_EXIT:
                        throw new CommunicationException("Binary stopped/crashed");
                }
            }
        }
    }

    @Deprecated
    boolean waitForXMLResponse(String xmlResponse, long timeout) throws WebDriverException
    {
        while (true)
        {
            synchronized (result)
            {
                this.result = WaitResult.WAITING;
                this.xmlResponse = xmlResponse;
                this.tag = -1;
                this.exception = null;
            }

            internalWait(timeout);

            synchronized (result)
            {
                WaitResult old_result = result;
                result = WaitResult.NONE;
                switch (old_result)
                {
                    case WAITING:
                        throw new ResponseNotReceivedException("No response in a timely fashion.");

                    case RESPONSE:
                        continue;

                    case XMLRESPONSE:
                        return true;

                    case ERROR:
                        continue;

                    case EXCEPTION:
                        throw exception;

                    case DISCONNECTED:
                        throw new CommunicationException("Disconnected STP connection.");

                    case BINARY_EXIT:
                        throw new CommunicationException("Binary stopped/crashed");
                }
            }
        }
        
    }
}
