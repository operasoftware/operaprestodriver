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
    private WebDriverException exception = null;
    private int exitCode = 0;
    private Object lock = new Object();
    private int windowId = 0;
    
    enum WaitResult
    {
        NONE,           /* Not waiting */
        WAITING,        /* Waiting for event to happen */
        RESPONSE,       /* Got a response */
        ERROR,          /* Got an error response */
        EXCEPTION,      /* An exception occured (STP connection is not alive) */
        DISCONNECTED,   /* STP connection is disconnected */
        BINARY_EXIT,    /* Opera binary crashed. */
        HANDSHAKE,       /* STP Handshake */
        EVENT_WINDOW_LOADED, /* finished loaded */
    }

    WaitResult result;
    boolean connected;

    public WaitState()
    {
        result = WaitResult.NONE;
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
            this.result = WaitResult.HANDSHAKE;
            lock.notify();
        }
    }

    void onResponse(int tag)
    {
        synchronized (lock)
        {
            logger.info("Got response for " + tag);
            if (tag == this.tag)
            {
                this.result = WaitResult.RESPONSE;
            } else {
                this.result = WaitResult.EXCEPTION;
                exception = new WebDriverException("Protocol error: Tag mismatch!");
            }
            lock.notify();
        }
    }

    void onError(int tag)
    {

        synchronized (lock)
        {
            logger.info("Got ERROR for " + tag);

            if (tag == this.tag)
            {
                this.result = WaitResult.ERROR;
            }
            else
            {
                this.result = WaitResult.EXCEPTION;
                exception = new WebDriverException("Protocol error: Tag mismatch!");
            }
            lock.notify();
        }
    }

    void onException(Exception e)
    {
        synchronized (lock)
        {
            logger.info("Got exception for " + tag);

            this.result = WaitResult.EXCEPTION;
            exception = new WebDriverException(e);
            connected = false;
            lock.notify();
        }
    }

    void onDisconnected()
    {
        synchronized (lock)
        {
            logger.info("Got disconnected for " + tag);
            this.result = WaitResult.DISCONNECTED;
            lock.notify();
        }
    }

    void onBinaryExit(int code)
    {
        synchronized (lock)
        {
            logger.info("Got BinaryExit for " + tag + ", exit code=" + code);
            this.result = WaitResult.BINARY_EXIT;
            this.exitCode = code;
            lock.notify();
        }
    }

    void onWindowLoaded(int windowId)
    {
        synchronized (lock)
        {
            logger.info("Event: onWindowLoaded");
            this.result = WaitResult.EVENT_WINDOW_LOADED;
            this.windowId = windowId;
            lock.notify();
        }
    }
    
    void waitForHandshake(long timeout) throws WebDriverException
    {
        synchronized (lock)
        {
            while (true)
            {
                if (this.result == WaitResult.NONE)
                {
                    this.result = WaitResult.WAITING;
                    this.tag = -1;
                    this.exception = null;
                    this.windowId = 0;

                    internalWait(timeout);
                }

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

                    case EVENT_WINDOW_LOADED:
                        break;

                    default:
                        throw new WebDriverException("Unexpected result, expecting handshake");
                }
            }
        }
    }

    boolean waitFor(int tag, long timeout) throws WebDriverException
    {
        synchronized (lock)
        {
            while (true)
            {
                if (this.result == WaitResult.NONE)
                {
                    this.result = WaitResult.WAITING;
                    this.tag = tag;
                    this.exception = null;
                    this.windowId = 0;

                    internalWait(timeout);
                }

                WaitResult old_result = result;
                result = WaitResult.NONE;
                switch (old_result)
                {
                    case WAITING:
                        throw new ResponseNotReceivedException("No response in a timely fashion.");

                    case RESPONSE:
                        return true;
                    
                    case ERROR:
                        return false;

                    case EXCEPTION:
                        throw exception;

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
            while (true)
            {
                if (this.result == WaitResult.NONE)
                {
                    this.result = WaitResult.WAITING;
                    this.tag = tag;
                    this.exception = null;
                    this.windowId = 0;

                    internalWait(timeout);
                }

                WaitResult old_result = result;
                result = WaitResult.NONE;
                switch (old_result)
                {
                    case WAITING:
                        throw new ResponseNotReceivedException("No response in a timely fashion.");

                    case RESPONSE:
                        break;

                    case ERROR:
                        break;

                    case EXCEPTION:
                        throw exception;

                    case DISCONNECTED:
                        throw new CommunicationException("Disconnected STP connection.");

                    case BINARY_EXIT:
                        throw new CommunicationException("Binary stopped/crashed");

                    case EVENT_WINDOW_LOADED:
                        if (this.windowId == windowId)
                            return true;
                        break;
                }
            }
        }
    }

}
