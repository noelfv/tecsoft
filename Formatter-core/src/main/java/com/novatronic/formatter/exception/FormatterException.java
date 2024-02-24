/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.formatter.exception;

import com.novatronic.formatter.internal.InternalFormat;
import com.novatronic.formatter.util.VariableByteBuffer;

/**
 *
 * @author ofernandez
 * @version 1.0
 * @since 1.0, 05/11/2010
 */
public class FormatterException extends RuntimeException{
    private InternalFormat intFormat;
    private VariableByteBuffer frame;
    
    public FormatterException(Throwable cause) {
        super(cause);
    }

    public FormatterException(String message, Throwable cause) {
        super(message, cause);
    }

    public FormatterException(String message) {
        super(message);
    }

    public FormatterException() {
    }
    
    public FormatterException(String message, Throwable cause, InternalFormat intFormat) {
        super(message, cause);
        this.intFormat = intFormat;
    }
    
    public FormatterException(Throwable cause, InternalFormat intFormat) {
        super(cause);
        this.intFormat = intFormat;
    }

    public FormatterException(String message, InternalFormat intFormat) {
        super(message);
        this.intFormat = intFormat;
    }
    
    public FormatterException(String message, Throwable cause, VariableByteBuffer frame) {
        super(message, cause);
        this.frame = frame;
    }
    
    public FormatterException(Throwable cause, VariableByteBuffer frame) {
        super(cause);
        this.frame = frame;
    }
    
    public FormatterException(String message, VariableByteBuffer frame) {
        super(message);
        this.frame = frame;
    }
    
    public FormatterException(String message, Throwable cause, VariableByteBuffer frame, InternalFormat intFormat) {
        super(message, cause);
        this.frame = frame;
        this.intFormat = intFormat;
    }
    
    public FormatterException(String message, VariableByteBuffer frame, InternalFormat intFormat) {
        super(message);
        this.frame = frame;
        this.intFormat = intFormat;
    }

    public FormatterException(Throwable cause, VariableByteBuffer frame, InternalFormat intFormat) {
        super(cause);
        this.frame = frame;
        this.intFormat = intFormat;
    }

    public VariableByteBuffer getFrame() {
        return frame;
    }

    public InternalFormat getIntFormat() {
        return intFormat;
    }
    
}
