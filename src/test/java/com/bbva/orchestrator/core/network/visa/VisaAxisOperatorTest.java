package com.bbva.orchestrator.core.network.visa;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class VisaAxisOperatorTest {

    @Test
    void testPrivateConstructor() throws Exception {
        Constructor<VisaAxisOperator> constructor = VisaAxisOperator.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        VisaAxisOperator instance = constructor.newInstance();
        assertNotNull(instance);
    }

    @Test
    void channelECommerceIndicator_MissingData() {
        Map<String, String> subFields = new HashMap<>();
        assertFalse(VisaAxisOperator.channelECommerceIndicator(subFields, "59"), "Falta VAR_6008");

        subFields.put("60.08", "02");
        assertFalse(VisaAxisOperator.channelECommerceIndicator(subFields, null), "pointServiceConditionCode es nulo");
    }

    @Test
    void channelECommerceIndicator_Success() {
        Map<String, String> subFields = Map.of("60.08", "02");
        assertTrue(VisaAxisOperator.channelECommerceIndicator(subFields, "59"));
        assertTrue(VisaAxisOperator.channelECommerceIndicator(subFields, "08"));
    }

    @Test
    void channelECommerceIndicator_InvalidValues() {
        Map<String, String> subFields = new HashMap<>();
        subFields.put("60.08", null);
        assertFalse(VisaAxisOperator.channelECommerceIndicator(subFields, "59"));

        Map<String, String> subFieldsInvalid = Map.of("60.08", "99");
        assertFalse(VisaAxisOperator.channelECommerceIndicator(subFieldsInvalid, "59"));
    }

    @Test
    void channelECommerceIndicator_Subfields_Null() {
        assertFalse(VisaAxisOperator.channelECommerceIndicator(null, "59"));
    }

    @Test
    void channelTPVIndicator_NullSubfields() {
        assertEquals("UNSP", VisaAxisOperator.channelTPVIndicator(null, "6011"));
    }

    @Test
    void channelTPVIndicator_IsPost() {
        Map<String, String> subFields = Map.of(
                "22.01", "05",
                "60.01", "3",
                "60.02", "1"
        );
        assertEquals("POST", VisaAxisOperator.channelTPVIndicator(subFields, "any"));
    }

    @ParameterizedTest
    @CsvSource({
            "6011, ATMT",
            "6010, OTHP",
            "1234, UNSP"
    })
    void channelTPVIndicator_AtmOrOther(String merchantType, String expected) {
        Map<String, String> subFields = Map.of("03.01", "01");
        assertEquals(expected, VisaAxisOperator.channelTPVIndicator(subFields, merchantType));
    }

    @Test
    void channelTPVIndicator_DefaultUnsp() {
        Map<String, String> subFields = Map.of("22.01", "99"); // Inválido para POST
        assertEquals("UNSP", VisaAxisOperator.channelTPVIndicator(subFields, "6011"));
    }

    @Test
    void testDummyMethods() {
        assertNull(VisaAxisOperator.valueElectronicCommerceIndicators(null));
        assertNull(VisaAxisOperator.securityLevelECI("someECI"));
        assertEquals("ALL", VisaAxisOperator.entryModeIndicator(null, "someMode"));
    }

    @Test
    void channelTPVIndicator_IsPost_BranchCoverage() {
        Map<String, String> map1 = new HashMap<>();
        map1.put("22.01", null);
        VisaAxisOperator.channelTPVIndicator(map1, "any");

        Map<String, String> map2 = Map.of("22.01", "XX");
        VisaAxisOperator.channelTPVIndicator(map2, "any");

        Map<String, String> map3 = new HashMap<>();
        map3.put("22.01", "05");
        map3.put("60.01", null);
        VisaAxisOperator.channelTPVIndicator(map3, "any");

        Map<String, String> map4 = new HashMap<>();
        map4.put("22.01", "05");
        map4.put("60.01", "3");
        map4.put("60.02", null);
        VisaAxisOperator.channelTPVIndicator(map4, "any");

        Map<String, String> map5 = Map.of(
                "22.01", "05",
                "60.01", "3",
                "60.02", "9" // No está en VALID_VALUES_6002
        );
        VisaAxisOperator.channelTPVIndicator(map5, "any");
    }

    @Test
    void channelTPVIndicator_Val6001_Null() {
        Map<String, String> subFields = new HashMap<>();
        subFields.put("22.01", "05"); // Pasa la primera condición
        subFields.put("60.01", null); // Falla la segunda (la que tienes en amarillo)
        subFields.put("60.02", "1");

        String result = VisaAxisOperator.channelTPVIndicator(subFields, "any");
        assertEquals("UNSP", result);
    }

    @Test
    void channelTPVIndicator_Val6001_Invalid() {
        Map<String, String> subFields = Map.of(
                "22.01", "05", // Pasa la primera
                "60.01", "9",  // Falla la segunda (no está en VALID_VALUES_6001)
                "60.02", "1"
        );

        String result = VisaAxisOperator.channelTPVIndicator(subFields, "any");
        assertEquals("UNSP", result);
    }
}