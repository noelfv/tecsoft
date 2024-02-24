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
public class FilterFactoryException extends FormatterException{

    public FilterFactoryException() {
        super();
    }

    public FilterFactoryException(String message) {
        super(message);
    }

    public FilterFactoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public FilterFactoryException(Throwable cause) {
        super(cause);
    }

}
