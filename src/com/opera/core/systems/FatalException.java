package com.opera.core.systems;

/**
 * Exception that is used when failing to start opera or the STP connection
 * during eary phases of initializations.
 *
 * @author Jan Vidar Krey (janv@opera.com)
 */
public class FatalException extends org.openqa.selenium.WebDriverException {

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
