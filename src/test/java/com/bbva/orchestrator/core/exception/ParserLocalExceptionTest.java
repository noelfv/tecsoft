package com.bbva.orchestrator.core.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParserLocalExceptionTest {

    @Test
    void testConstructorWithMessage() {
        // Arrange
        String message = "Test message";
        // Act
        ParserFieldsException exception = new ParserFieldsException(message);
        // Assert
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testConstructorWithMessageAndThrowable() {
        // Arrange
        String code = "PGWP-00001";
        String description = "Test message";
        Throwable cause = new RuntimeException("Cause");
        // Act
        ParserFieldsException exception = new ParserFieldsException(code,description, cause);
        // Assert
        assertEquals(code, exception.getCode());
        assertEquals(description, exception.getDescription());
        assertEquals(cause, exception.getCause());
    }

}