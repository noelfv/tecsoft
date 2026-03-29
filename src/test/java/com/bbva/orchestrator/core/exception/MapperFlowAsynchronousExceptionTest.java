package com.bbva.orchestrator.core.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MapperFlowAsynchronousExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        String expectedMessage = "Error en el flujo asíncrono";

        MandatoryFieldsException exception = new MandatoryFieldsException(expectedMessage);

        assertNotNull(exception);
        assertEquals(expectedMessage, exception.getMessage());
        assertNull(exception.getCode(), "El código debería ser nulo para este constructor");
        assertNull(exception.getDescription(), "La descripción debería ser nula para este constructor");
    }

    @Test
    void shouldCreateExceptionWithCodeDescriptionAndCause() {
        String expectedCode = "ERR-ASYNC-001";
        String expectedDescription = "Fallo al procesar la respuesta asíncrona";
        Throwable expectedCause = new NullPointerException("Causa raíz del error");

        MandatoryFieldsException exception = new MandatoryFieldsException(expectedCode, expectedDescription, expectedCause);

        assertNotNull(exception);
        assertEquals(expectedCode, exception.getCode());
        assertEquals(expectedDescription, exception.getDescription());
        assertEquals(expectedCause, exception.getCause());
        assertEquals(expectedCause.toString(), exception.getMessage());
    }
}