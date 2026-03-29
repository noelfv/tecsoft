package com.bbva.orchestrator.core.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapperLocalExceptionTest {

    @Test
    void testConstructorWithMessage() {
        // Arrange
        String message = "Test message";
        // Act
        MapperFieldsException exception = new MapperFieldsException(message);
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
        MapperFieldsException exception = new MapperFieldsException(code,description, cause);
        // Assert
        assertEquals(code, exception.getCode());
        assertEquals(description, exception.getDescription());
        assertEquals(cause, exception.getCause());
    }

}