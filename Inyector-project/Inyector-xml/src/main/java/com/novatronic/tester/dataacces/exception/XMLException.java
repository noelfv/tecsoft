/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.dataacces.exception;

import com.novatronic.tester.exception.TesterException;

/**
 *
 * @author nteruya
 */
public class XMLException extends TesterException {

    public XMLException(Throwable cause) {
        super(cause);
    }

    public XMLException(String message, Throwable cause) {
        super(message,cause);
    }

    public XMLException(String message) {
        super(message);
    }

    public XMLException() {
        super();
    }
}
