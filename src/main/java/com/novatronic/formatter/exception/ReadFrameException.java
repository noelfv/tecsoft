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
 */
public class ReadFrameException extends FormatterException{

    public ReadFrameException(Throwable cause, VariableByteBuffer frame, InternalFormat intFormat) {
        super(cause, frame, intFormat);
    }

    public ReadFrameException(String message, VariableByteBuffer frame, InternalFormat intFormat) {
        super(message, frame, intFormat);
    }

    public ReadFrameException(String message, Throwable cause, VariableByteBuffer frame, InternalFormat intFormat) {
        super(message, cause, frame, intFormat);
    }

    public ReadFrameException(String message, VariableByteBuffer frame) {
        super(message, frame);
    }

    public ReadFrameException(Throwable cause, VariableByteBuffer frame) {
        super(cause, frame);
    }

    public ReadFrameException(String message, Throwable cause, VariableByteBuffer frame) {
        super(message, cause, frame);
    }

    public ReadFrameException(String message, InternalFormat intFormat) {
        super(message, intFormat);
    }

    public ReadFrameException(Throwable cause, InternalFormat intFormat) {
        super(cause, intFormat);
    }

    public ReadFrameException(String message, Throwable cause, InternalFormat intFormat) {
        super(message, cause, intFormat);
    }

    public ReadFrameException() {
    }

    public ReadFrameException(String message) {
        super(message);
    }

    public ReadFrameException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReadFrameException(Throwable cause) {
        super(cause);
    }
}
