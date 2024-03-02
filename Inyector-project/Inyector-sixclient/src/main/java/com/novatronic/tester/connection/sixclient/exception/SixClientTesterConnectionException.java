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
public class SixClientTesterConnectionException extends TesterConnectionException{

    public SixClientTesterConnectionException(Throwable cause) {
        super(cause);
    }

    public SixClientTesterConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public SixClientTesterConnectionException(String message) {
        super(message);
    }

    public SixClientTesterConnectionException() {
    }

}
