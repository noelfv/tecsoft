/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.exception;

/**
 *
 * @author ofernandez
 */
public class TesterException extends RuntimeException{

    public TesterException(Throwable cause) {
        super(cause);
    }

    public TesterException(String message, Throwable cause) {
        super(message,cause);
    }

    public TesterException(String message) {
        super(message);
    }

    public TesterException() {
        super();
    }
    
}
