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
public class ApiSixConfigurationException extends TesterConnectionException{

    public ApiSixConfigurationException(Throwable cause) {
        super(cause);
    }

    public ApiSixConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApiSixConfigurationException(String message) {
        super(message);
    }

    public ApiSixConfigurationException() {
    }
    
}
