/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.connection.tcpclient.exception;

import com.novatronic.tester.exception.TesterConnectionException;

/**
 *
 * @author ofernandez
 */
public class TCPClientConnectionException extends TesterConnectionException{

    public TCPClientConnectionException(Throwable cause) {
        super(cause);
    }

    public TCPClientConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public TCPClientConnectionException(String message) {
        super(message);
    }

    public TCPClientConnectionException() {
        super();
    }
    
}
