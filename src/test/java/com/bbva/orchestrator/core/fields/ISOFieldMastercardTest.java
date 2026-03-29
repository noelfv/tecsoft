package com.bbva.orchestrator.core.fields;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ISOFieldMastercardTest {

    @ParameterizedTest
    @EnumSource(MastercardISOField.class)
    void enumConstants_shouldHaveNonNullProperties(MastercardISOField field) {
        assertNotNull(field.getName());
        assertNotNull(field.getTypeData());
        assertNotNull(field.getParserStrategy());
        assertNotNull(field.getIdentifier());
        assertTrue(field.getId() >= 0);
        assertTrue(field.getLength() >= 0);
    }

    @Test
    void getById_withExistingId_shouldReturnCorrectField() {
        int fieldId = 3; // PROCESSING_CODE
        MastercardISOField expectedField = MastercardISOField.PROCESSING_CODE;

        MastercardISOField result = MastercardISOField.getById(fieldId);

        assertNotNull(result);
        assertEquals(expectedField, result);
    }

    @Test
    void getById_withNonExistentId_shouldReturnNull() {
        int nonExistentId = 999;

        MastercardISOField result = MastercardISOField.getById(nonExistentId);

        assertNull(result);
    }

    @ParameterizedTest
    @MethodSource("fieldPropertiesProvider")
    void isVariable_shouldReturnCorrectConfiguredValue(MastercardISOField field, boolean expectedIsVariable) {
        assertEquals(expectedIsVariable, field.isVariable());
    }

    // Este método estático provee los datos para el test de arriba.
    // Cada 'Arguments.of' es un caso de prueba: (Enum a probar, valor esperado para isVariable)
    private static Stream<Arguments> fieldPropertiesProvider() {
        return Stream.of(
                // Caso 1: Un campo de longitud FIJA
                Arguments.of(MastercardISOField.MESSAGE_TYPE, false),
                // Caso 2: Un campo de longitud VARIABLE
                Arguments.of(MastercardISOField.PRIMARY_ACCOUNT_NUMBER, true),
                // Caso 3: Otro campo de longitud FIJA
                Arguments.of(MastercardISOField.PROCESSING_CODE, false),
                // Caso 4: Otro campo de longitud VARIABLE
                Arguments.of(MastercardISOField.ACQUIRING_INSTITUTION_IDENTIFICATION_CODE, true)
                // Puedes añadir más casos aquí para ser más exhaustivo
        );
    }
}