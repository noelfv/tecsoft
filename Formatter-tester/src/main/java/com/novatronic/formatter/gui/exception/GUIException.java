/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.formatter.gui.exception;

/**
 *
 * @author Omar
 */
public class GUIException extends RuntimeException {

    public GUIException(Throwable cause) {
        super(cause);
    }

    public GUIException(String message, Throwable cause) {
        super(message, cause);
    }

    public GUIException(String message) {
        super(message);
    }

    public GUIException() {
    }
    
}
