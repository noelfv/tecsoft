package com.bbva.orchestrator.core.exception;

import lombok.Getter;

public class ParserFieldsException extends RuntimeException {

    @Getter
    private String code;
    @Getter
    private String description;

    public ParserFieldsException(String message) {
        super(message);
    }

    public ParserFieldsException(String code, String description, Throwable cause) {
        super(cause);
        this.code=code;
        this.description=description;
    }
}
