package com.bbva.orchestrator.core.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LogicFieldsExceptionTest {

    @Test
    void testConstructorWithMessage() {
        LogicFieldsException ex = new LogicFieldsException("Mensaje de error");
        assertEquals("Mensaje de error", ex.getMessage());
        assertNull(ex.getCode());
        assertNull(ex.getDescription());
    }

    @Test
    void testConstructorWithCodeDescriptionAndCause() {
        Throwable cause = new RuntimeException("Causa interna");
        LogicFieldsException ex = new LogicFieldsException("COD123", "Descripción", cause);
        assertEquals("COD123", ex.getCode());
        assertEquals("Descripción", ex.getDescription());
        assertEquals(cause, ex.getCause());
    }
}

