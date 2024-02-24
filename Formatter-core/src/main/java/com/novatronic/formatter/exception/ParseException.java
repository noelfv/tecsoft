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
public class ParseException extends RuntimeException{

    public ParseException(Throwable cause) {
        super(cause);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseException(String message) {
        super(message);
    }

    public ParseException() {
    }
    
}
