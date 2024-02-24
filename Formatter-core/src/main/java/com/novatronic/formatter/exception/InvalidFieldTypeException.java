/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.formatter.exception;

/**
 *
 * @author ofernandez
 * @version 1.0
 * @since 1.0, 10/11/2010
 */
public class InvalidFieldTypeException extends FieldException{

    public InvalidFieldTypeException(Throwable cause) {
        super(cause);
    }

    public InvalidFieldTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidFieldTypeException(String message) {
        super(message);
    }

    public InvalidFieldTypeException() {
        super();
    }

}
