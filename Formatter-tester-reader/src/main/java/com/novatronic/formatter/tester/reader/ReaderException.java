/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.tester.reader;

/**
 *
 * @author Omar
 */
public class ReaderException extends RuntimeException{

    public ReaderException(Throwable cause) {
        super(cause);
    }

    public ReaderException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReaderException(String message) {
        super(message);
    }

    public ReaderException() {
    }
    
}
