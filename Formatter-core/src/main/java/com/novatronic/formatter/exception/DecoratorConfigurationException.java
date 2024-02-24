/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.exception;

/**
 *
 * @author ofernandez
 */
public class DecoratorConfigurationException extends DecoratorException{

    public DecoratorConfigurationException() {
    }

    public DecoratorConfigurationException(String message) {
        super(message);
    }

    public DecoratorConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DecoratorConfigurationException(Throwable cause) {
        super(cause);
    }
    
}
