package com.bbva.orchestrator.core.exception;

import lombok.Getter;

public class MandatoryFieldsException extends RuntimeException {

    @Getter
    private String code;
    @Getter
    private String description;

    public MandatoryFieldsException(String message) {
        super(message);
    }

    public MandatoryFieldsException(String code, String description, Throwable cause) {
        super(cause);
        this.code=code;
        this.description=description;
    }
}
