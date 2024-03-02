/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.tester.connection.sixclient.exception;

import com.novatronic.tester.exception.TesterConnectionException;

/**
 *
 * @author ofernandez
 */
public class ApiSixDestroyException extends TesterConnectionException{

    public ApiSixDestroyException(Throwable cause) {
        super(cause);
    }

    public ApiSixDestroyException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApiSixDestroyException(String message) {
        super(message);
    }

    public ApiSixDestroyException() {
    }
    
}
