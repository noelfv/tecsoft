package com.bbva.orchestrator.network.common;

import com.bbva.orchestrator.core.commons.CommonsProcessSubField;
import com.bbva.orchestrator.core.dto.ISO8583;
import com.bbva.orchestrator.core.parser.iso8583.strategy.subfields.CompositeFixedFieldParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para DefaultISOSubFieldParser.
 */
@ExtendWith(MockitoExtension.class)
class DefaultISOSubFieldParserTest {

    @Mock
    private CompositeFixedFieldParser compositeFieldParser; // El mock de la dependencia

    @Mock
    private ISO8583 iso8583; // El mock del objeto de entrada

    @InjectMocks
    private CommonsProcessSubField defaultISOSubFieldParser; // La clase que estamos probando

    /**
     * Prueba el "camino feliz" donde ambos campos (03 y 54) tienen valores
     * y el parser devuelve sus respectivos subcampos.
     */
    @Test
    void testParseSubfields_HappyPath() {
        // --- ARRANGE (Configuración) ---
        // 1. Datos de entrada simulados
        String processingCodeValue = "001000"; // Valor para el campo 3
        String additionalAmountsValue = "C0012345"; // Valor para el campo 54
        String pointServiceEntryModeValue = "051"; // Valor para el campo 22
        String posCardIssuer = "C0012345"; // Valor para el campo 61

        // 2. Mapas de subcampos que el mock del parser debe devolver
        Map<String, String> subfields03 = Map.of("transaction_code", "00", "account_from", "10", "account_to", "00");
        Map<String, String> subfields54 = Map.of("currency_code", "C", "amount", "0012345");
        Map<String, String> subfields22 = Map.of("22.01", "00", "22.02", "5");
        Map<String, String> subfields61 = Map.of("61.01", "C", "61.02", "0012345");

        // 3. Configuramos el comportamiento de los mocks
        when(iso8583.getProcessingCode()).thenReturn(processingCodeValue);
        when(iso8583.getAdditionalAmounts()).thenReturn(additionalAmountsValue);
        when(iso8583.getPointServiceEntryMode()).thenReturn(pointServiceEntryModeValue);
        when(iso8583.getPosCardIssuer()).thenReturn(posCardIssuer);
        when(compositeFieldParser.buildSubFieldsSpecific("03", processingCodeValue)).thenReturn(subfields03);
        when(compositeFieldParser.buildSubFieldsSpecific("54", additionalAmountsValue)).thenReturn(subfields54);
        when(compositeFieldParser.buildSubFieldsSpecific("22", pointServiceEntryModeValue)).thenReturn(subfields22);
        when(compositeFieldParser.buildSubFieldsSpecific("61", posCardIssuer)).thenReturn(subfields61);

        // --- ACT (Ejecución) ---
        Map<String, String> result = defaultISOSubFieldParser.parseSubfields(iso8583);

        // --- ASSERT (Verificación) ---
        assertNotNull(result);
        assertEquals(9, result.size(), "El mapa final debe contener todos los subcampos de ambos campos.");
        // Verificamos que los valores de ambos mapas están presentes
        assertEquals("00", result.get("transaction_code"));
        assertEquals("C", result.get("currency_code"));
        assertTrue(result.containsKey("account_from"));
        assertTrue(result.containsKey("amount"));
        assertEquals("00", result.get("22.01"));
        assertEquals("C", result.get("61.01"));

        // Verificamos que se llamó al parser con los valores correctos
        verify(compositeFieldParser).buildSubFieldsSpecific("03", processingCodeValue);
        verify(compositeFieldParser).buildSubFieldsSpecific("54", additionalAmountsValue);
        verify(compositeFieldParser).buildSubFieldsSpecific("22", pointServiceEntryModeValue);
        verify(compositeFieldParser).buildSubFieldsSpecific("61", posCardIssuer);
    }

    /**
     * Prueba el caso en que los campos del objeto ISO8583 son nulos.
     * El método debe manejar esto correctamente y devolver un mapa vacío.
     */
    @Test
    void testParseSubfields_WhenFieldsAreNull() {
        // --- ARRANGE ---
        // Configuramos el ISO8583 para que devuelva null para los campos
        when(iso8583.getProcessingCode()).thenReturn(null);
        when(iso8583.getAdditionalAmounts()).thenReturn(null);
        when(iso8583.getPointServiceEntryMode()).thenReturn(null);
        when(iso8583.getPosCardIssuer()).thenReturn(null);

        // Si el parser recibe null, debe devolver un mapa vacío
        when(compositeFieldParser.buildSubFieldsSpecific("03", null)).thenReturn(Collections.emptyMap());
        when(compositeFieldParser.buildSubFieldsSpecific("54", null)).thenReturn(Collections.emptyMap());
        when(compositeFieldParser.buildSubFieldsSpecific("22", null)).thenReturn(Collections.emptyMap());
        when(compositeFieldParser.buildSubFieldsSpecific("61", null)).thenReturn(Collections.emptyMap());
        // --- ACT ---
        Map<String, String> result = defaultISOSubFieldParser.parseSubfields(iso8583);

        // --- ASSERT ---
        assertNotNull(result);
        assertTrue(result.isEmpty(), "El resultado debe ser un mapa vacío si no hay datos de entrada.");
    }

    /**
     * Prueba qué sucede si los subcampos de los campos 03 y 54 tuvieran una clave en común.
     * El valor del último mapa agregado (campo 54) debe prevalecer.
     */
    @Test
    void testParseSubfields_WithOverlappingKeys() {
        // --- ARRANGE ---
        String processingCodeValue = "DATA03";
        String additionalAmountsValue = "DATA54";
        String pointServiceEntryModeValue = "DATA22"; // Valor para el campo 22
        String posCardIssuer = "DATA61"; // Valor para el campo 61

        // Mapas con una clave común ("shared_key")
        Map<String, String> subfields03 = Map.of("shared_key", "value_from_03", "key_03", "A");
        Map<String, String> subfields54 = Map.of("shared_key", "value_from_54_overwritten", "key_54", "B");
        Map<String, String> subfields22 = Map.of("shared_key", "value_from_22", "key_03", "A");
        Map<String, String> subfields61 = Map.of("shared_key", "value_from_61_overwritten", "key_54", "B");

        when(iso8583.getProcessingCode()).thenReturn(processingCodeValue);
        when(iso8583.getAdditionalAmounts()).thenReturn(additionalAmountsValue);
        when(iso8583.getPointServiceEntryMode()).thenReturn(pointServiceEntryModeValue);
        when(iso8583.getPosCardIssuer()).thenReturn(posCardIssuer);
        when(compositeFieldParser.buildSubFieldsSpecific("03", processingCodeValue)).thenReturn(subfields03);
        when(compositeFieldParser.buildSubFieldsSpecific("54", additionalAmountsValue)).thenReturn(subfields54);
        when(compositeFieldParser.buildSubFieldsSpecific("22", pointServiceEntryModeValue)).thenReturn(subfields22);
        when(compositeFieldParser.buildSubFieldsSpecific("61", posCardIssuer)).thenReturn(subfields61);

        // --- ACT ---
        Map<String, String> result = defaultISOSubFieldParser.parseSubfields(iso8583);

        // --- ASSERT ---
        assertEquals(3, result.size(), "El tamaño debe ser 3 porque una clave se sobrescribe.");
        assertEquals("value_from_61_overwritten", result.get("shared_key"), "El valor del campo 61 debe sobrescribir al del campo 03.");
        assertEquals("A", result.get("key_03"));
        assertEquals("B", result.get("key_54"));
    }
}