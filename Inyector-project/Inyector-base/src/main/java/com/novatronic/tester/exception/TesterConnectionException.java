/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.exception;

/**
 *
 * @author ofernandez
 */
public class TesterConnectionException extends TesterException{

    public TesterConnectionException(Throwable cause) {
        super(cause);
    }

    public TesterConnectionException(String message, Throwable cause) {
        super(message,cause);
    }

    public TesterConnectionException(String message) {
        super(message);
    }

    public TesterConnectionException() {
        super();
    }

}
