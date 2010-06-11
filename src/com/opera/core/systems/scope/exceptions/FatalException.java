package com.opera.core.systems.scope.exceptions;

import org.openqa.selenium.WebDriverException;

/**
 * Exception that is used when failing to start opera or the STP connection
 * during eary phases of initializations.
 *
 * @author Jan Vidar Krey (janv@opera.com)
 */
@SuppressWarnings("serial")
public class FatalException extends WebDriverException {


	/**
     * Creates a new instance of <code>FatalException</code> without detail message.
     */
    public FatalException() {
    }

    /**
     * Constructs an instance of <code>FatalException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public FatalException(String msg) {
        super(msg);
    }
}
