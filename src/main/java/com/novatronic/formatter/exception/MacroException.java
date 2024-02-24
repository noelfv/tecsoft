/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.formatter.exception;

/**
 *
 * @author ofernandez
 * @version 1.0
 * @since 1.0, 10/12/2010
 */
public class MacroException extends RuntimeException{

    public MacroException(Throwable cause) {
        super(cause);
    }

    public MacroException(String message, Throwable cause) {
        super(message, cause);
    }

    public MacroException(String message) {
        super(message);
    }

    public MacroException() {
    }
    
}
