package com.bbva.orchestrator.core.exception;

import lombok.Getter;

public class MapperFieldsException extends RuntimeException {

    @Getter
    private String code;
    @Getter
    private String description;

    public MapperFieldsException(String message) {
        super(message);
    }

    public MapperFieldsException(String code, String description, Throwable cause) {
        super(cause);
        this.code=code;
        this.description=description;
    }
}
