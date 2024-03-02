/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.connection.tcpserver.exceptions;

import com.novatronic.tester.exception.TesterConnectionException;

/**
 *
 * @author ofernandez
 */
public class TCPServerConnectionException extends TesterConnectionException{

    public TCPServerConnectionException(Throwable cause) {
        super(cause);
    }

    public TCPServerConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public TCPServerConnectionException(String message) {
        super(message);
    }

    public TCPServerConnectionException() {
    }
    
}
