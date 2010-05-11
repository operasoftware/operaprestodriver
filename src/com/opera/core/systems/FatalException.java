/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.opera.core.systems;

/**
 *
 * @author janv
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
