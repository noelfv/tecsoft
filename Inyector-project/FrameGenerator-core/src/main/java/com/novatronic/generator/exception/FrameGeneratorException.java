/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.generator.exception;

/**
 *
 * @author ofernandez
 * @version 1.0
 * @since 1.0, 12/11/2010
 */
public class FrameGeneratorException extends RuntimeException{

    public FrameGeneratorException(Throwable cause) {
        super(cause);
    }

    public FrameGeneratorException(String message, Throwable cause) {
        super(message, cause);
    }

    public FrameGeneratorException(String message) {
        super(message);
    }

    public FrameGeneratorException() {
    }
    
}
