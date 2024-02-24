/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.formatter.exception;

/**
 *
 * @author ofernandez
 * @version 1.0
 * @since 1.0, 10/12/2010
 */
public class FieldValueException extends FieldException{

    public FieldValueException(Throwable cause) {
        super(cause);
    }

    public FieldValueException(String message, Throwable cause) {
        super(message, cause);
    }

    public FieldValueException(String message) {
        super(message);
    }

    public FieldValueException() {
    }

}
