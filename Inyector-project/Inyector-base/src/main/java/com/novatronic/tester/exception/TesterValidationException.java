/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.exception;

/**
 *
 * @author ofernandez
 */
public class TesterValidationException extends TesterException{

    public TesterValidationException() {
        super();
    }

    public TesterValidationException(String message) {
        super(message);
    }

    public TesterValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TesterValidationException(Throwable cause) {
        super(cause);
    }
    
}
