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
public class CreateFrameException extends FormatterException{

    public CreateFrameException(Throwable cause, VariableByteBuffer frame, InternalFormat intFormat) {
        super(cause, frame, intFormat);
    }

    public CreateFrameException(String message, VariableByteBuffer frame, InternalFormat intFormat) {
        super(message, frame, intFormat);
    }

    public CreateFrameException(String message, Throwable cause, VariableByteBuffer frame, InternalFormat intFormat) {
        super(message, cause, frame, intFormat);
    }

    public CreateFrameException(String message, VariableByteBuffer frame) {
        super(message, frame);
    }

    public CreateFrameException(Throwable cause, VariableByteBuffer frame) {
        super(cause, frame);
    }

    public CreateFrameException(String message, Throwable cause, VariableByteBuffer frame) {
        super(message, cause, frame);
    }

    public CreateFrameException(String message, InternalFormat intFormat) {
        super(message, intFormat);
    }

    public CreateFrameException(Throwable cause, InternalFormat intFormat) {
        super(cause, intFormat);
    }

    public CreateFrameException(String message, Throwable cause, InternalFormat intFormat) {
        super(message, cause, intFormat);
    }

    public CreateFrameException() {
    }

    public CreateFrameException(String message) {
        super(message);
    }

    public CreateFrameException(String message, Throwable cause) {
        super(message, cause);
    }

    public CreateFrameException(Throwable cause) {
        super(cause);
    }
}
