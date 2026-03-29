package com.bbva.orchestrator.validations.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConfigValidationsTest {


    @Test
    void testGetRequired() {
        ConfigMessageFunctions[] required = ConfigValidations.TRANSACTION_TRANSACTIONTYPE.getRequired();
        assertNotNull(required);
        assertEquals(ConfigMessageFunctions.REQUIRED_0200, required[0]);
    }

    @Test
    void testGetFormat() {
        String format = ConfigValidations.TRANSACTION_TRANSACTIONTYPE.getFormat();
        assertEquals("number", format);
    }
}
