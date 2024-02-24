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
public class FilterConfigurationException extends FieldException{

    public FilterConfigurationException(Throwable cause) {
        super(cause);
    }

    public FilterConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public FilterConfigurationException(String message) {
        super(message);
    }

    public FilterConfigurationException() {
    }

}
