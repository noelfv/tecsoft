package com.bbva.orchestrator.core.exception;

import lombok.Getter;

public class LogicFieldsException extends RuntimeException {

    @Getter
    private String code;
    @Getter
    private String description;

    public LogicFieldsException(String message) {
        super(message);
    }

    public LogicFieldsException(String code, String description, Throwable cause) {
        super(cause);
        this.code=code;
        this.description=description;
    }
}
