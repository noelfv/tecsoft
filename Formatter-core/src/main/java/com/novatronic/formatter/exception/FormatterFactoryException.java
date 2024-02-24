/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.formatter.exception;

/**
 *
 * @author ofernandez
 * @version 1.0
 * @since 1.0, 09/11/2010
 */
public class FormatterFactoryException extends FormatterException{

    public FormatterFactoryException() {
        super();
    }

    public FormatterFactoryException(String message) {
        super(message);
    }

    public FormatterFactoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public FormatterFactoryException(Throwable cause) {
        super(cause);
    }

}
