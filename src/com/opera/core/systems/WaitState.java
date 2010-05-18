package com.opera.core.systems;

import org.openqa.selenium.WebDriverException;
import com.opera.core.systems.scope.exceptions.ResponseNotReceivedException;

/**
 *
 * @author Jan Vidar Krey (janv@opera.com)
 */
public class WaitState {

    private int tag;
    private WebDriverException exception = null;
    private int exitCode = 0;

    enum WaitResult
    {
        NONE,       /* Not waiting */
        WAITING,    /* Waiting for event to happen */
        RESPONSE,   /* Got a response */
        ERROR,      /* Got an error response */
        EXCEPTION,  /* An exception occured (STP connection is not alive) */
        DISCONNECTED, /* STP connection is disconnected */
        BINARY_EXIT /* Opera binary crashed. */
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
        System.out.println("Waiting " + timeout + " ms for response on tag " + tag);
        try {
            synchronized (this)
            {
                if (!connected)
                    throw new WebDriverException("Waiting aborted - not connected!");
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

    void onResponse(int tag)
    {
        System.out.println("Got response for " + tag);
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

    void onError(int tag)
    {
        System.out.println("Got ERROR for " + tag);
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
        System.out.println("Got exception for " + tag);
        synchronized (result)
        {
            this.result = WaitResult.EXCEPTION;
            exception = new WebDriverException(e);
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
            System.out.println("Got disconnected for " + tag);
            synchronized (result)
            {
                this.result = WaitResult.DISCONNECTED;
            }
            wakeup();
        }
    }

    void onBinaryExit(int code)
    {
        System.out.println("Got BinaryExit for " + tag + ", exit code=" + code);
        synchronized (result)
        {
            this.result = WaitResult.BINARY_EXIT;
            this.exitCode = code;
        }

        wakeup();
    }
    
    boolean waitFor(int tag, long timeout) throws WebDriverException
    {
        if (isWaiting())
            throw new WebDriverException("Already wating for " + tag + ", state=" + result.toString());

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
                    System.out.println(" -> WAITING");
                    throw new ResponseNotReceivedException("No response in a timely fashion.");
                
                case RESPONSE:
                    System.out.println(" -> RESPONSE");
                    return true;

                case ERROR:
                    System.out.println(" -> ERROR");
                    return false;

                case EXCEPTION:
                    System.out.println(" -> EXCEPTION");
                    throw exception;

		case DISCONNECTED:
                    System.out.println(" -> DISCONNECTED");
		    throw new WebDriverException("Disconnected STP connection.");

                case BINARY_EXIT:
                    if (exitCode != 0)
                        System.out.println(" -> CRASHED");
                    else
                        System.out.println(" -> EXIT");
                    throw new WebDriverException("Binary stopped.");
            }
        }
        return false;
    }
}
