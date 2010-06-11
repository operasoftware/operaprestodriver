package com.opera.core.systems.scope.exceptions;

import org.openqa.selenium.WebDriverException;

/**
 * This event is to be used when the STP communication fails in an unrecoverable
 * fashion - connection has been closed.
 *
 * @author Jan Vidar Krey (janv@opera.com)
 */
@SuppressWarnings("serial")
public class CommunicationException extends WebDriverException {


	/**
     * Creates a new instance of <code>CommunicationException</code> without detail message.
     */
    public CommunicationException() {
    }


    /**
     * Constructs an instance of <code>CommunicationException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public CommunicationException(String msg) {
        super(msg);
    }
}
