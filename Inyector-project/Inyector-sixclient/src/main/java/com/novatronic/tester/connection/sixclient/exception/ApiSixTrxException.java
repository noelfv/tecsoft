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
public class ApiSixTrxException extends TesterConnectionException{

    public ApiSixTrxException(Throwable cause) {
        super(cause);
    }

    public ApiSixTrxException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApiSixTrxException(String message) {
        super(message);
    }

    public ApiSixTrxException() {
    }
    
}
