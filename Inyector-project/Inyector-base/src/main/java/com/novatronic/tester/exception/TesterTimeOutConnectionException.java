/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.exception;

/**
 *
 * @author ofernandez
 * @version 1.0
 * @since 1.0, 10/12/2010
 */
public class TesterTimeOutConnectionException extends TesterConnectionException{

    public TesterTimeOutConnectionException() {
        super();
    }

    public TesterTimeOutConnectionException(String message) {
        super(message);
    }

    public TesterTimeOutConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public TesterTimeOutConnectionException(Throwable cause) {
        super(cause);
    }

}
