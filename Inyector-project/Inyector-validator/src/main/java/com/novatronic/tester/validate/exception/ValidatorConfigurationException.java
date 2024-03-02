/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.validate.exception;

import com.novatronic.tester.exception.TesterValidationException;

/**
 *
 * @author ofernandez
 * @version 1.0
 * @since 1.0, 30/12/2010
 */
public class ValidatorConfigurationException extends TesterValidationException {

    public ValidatorConfigurationException(Throwable cause) {
        super(cause);
    }

    public ValidatorConfigurationException(String message, Throwable cause) {
        super(message,cause);
    }

    public ValidatorConfigurationException(String message) {
        super(message);
    }

    public ValidatorConfigurationException() {
        super();
    }

}
