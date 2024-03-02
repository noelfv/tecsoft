/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.dataacces.db.exceptions;

import com.novatronic.tester.exception.TesterException;

/**
 *
 * @author ofernandez
 */
public class DBTesterException extends TesterException{

    public DBTesterException() {
        super();
    }

    public DBTesterException(String message) {
        super(message);
    }

    public DBTesterException(String message, Throwable cause) {
        super(message, cause);
    }

    public DBTesterException(Throwable cause) {
        super(cause);
    }

}
