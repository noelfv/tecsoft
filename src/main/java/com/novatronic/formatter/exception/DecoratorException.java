/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.exception;

/**
 *
 * @author Omar
 */
public class DecoratorException extends FieldException{

    public DecoratorException(Throwable cause) {
        super(cause);
    }

    public DecoratorException(String message, Throwable cause) {
        super(message, cause);
    }

    public DecoratorException(String message) {
        super(message);
    }

    public DecoratorException() {
    }
}
