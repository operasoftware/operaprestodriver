/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.opera.core.systems;

/**
 *
 * @author janv
 */
public class CommunicationException extends org.openqa.selenium.WebDriverException {

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
