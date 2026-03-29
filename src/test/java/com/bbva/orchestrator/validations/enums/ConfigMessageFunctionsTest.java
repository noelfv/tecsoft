package com.bbva.orchestrator.validations.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigMessageFunctionsTest {


    @Test
    void testMessageFunction() {
        String messageFunction = ConfigMessageFunctions.REQUIRED_0200.getMessageFunction();
        assertEquals("PAGO", messageFunction);
    }

    @Test
    void testFieldRequired() {
        String fieldRequired = ConfigMessageFunctions.REQUIRED_0200.getFieldRequired();
        assertEquals("REQUIRED", fieldRequired);
    }
}
