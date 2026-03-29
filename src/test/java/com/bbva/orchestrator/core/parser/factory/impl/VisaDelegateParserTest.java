package com.bbva.orchestrator.core.parser.factory.impl;

import com.bbva.orchestrator.core.network.visa.VisaProcessField;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VisaDelegateParserTest {

    @InjectMocks
    private VisaDelegateParser visaDelegateParser;

    @Mock
    private VisaProcessField visaISOFieldParser;

    @Test
    void unParser_shouldReturnEmptyString() {
        Map<String, String> mappedFields = new HashMap<>();

        String result = visaDelegateParser.unParser(mappedFields);

        assertNull(result);
    }

    @Test
    void unParserPlainText_shouldDelegateToFieldParser() {
        Map<String, String> mappedFields = new HashMap<>();
        mappedFields.put("field1", "value1");
        mappedFields.put("field2", "value2");
        String expectedPlainText = "plain_text_result";
        when(visaISOFieldParser.unMapFieldsPlainText(mappedFields)).thenReturn(expectedPlainText);

        String result = visaDelegateParser.unParserPlainText(mappedFields);

        assertEquals(expectedPlainText, result);
        verify(visaISOFieldParser).unMapFieldsPlainText(mappedFields);
    }

    @Test
    void adjustFields_shouldRemoveLeftZero_whenValueHasFourChars() {
        Map<String, String> mapValues = new HashMap<>();

        mapValues.put("transactionCurrencyCode", "SETTLEMENT_CURRENCY_CODE");

        visaDelegateParser.adjustFields(mapValues);

        assertEquals("ETT", mapValues.get("transactionCurrencyCode"), "El valor de 4 chars debe ser cortado a 3 (eliminando el '0' inicial).");
    }

    @Test
    void adjustFields_shouldCutValue_whenNoLeftZeroAndFourChars() {
        Map<String, String> mapValues = new HashMap<>();
        mapValues.put("settlementCurrencyCode", "1234");

        visaDelegateParser.adjustFields(mapValues);

        assertEquals("234", mapValues.get("settlementCurrencyCode"), "Debe realizar el corte a pesar de no empezar con '0'.");
    }

    @Test
    void adjustFields_shouldHandleNullAndEmptyValues() {
        Map<String, String> mapValues = new HashMap<>();
        mapValues.put("acquirerCountryCode", null);
        mapValues.put("transactionCurrencyCode", "");

        visaDelegateParser.adjustFields(mapValues);

        assertNull(mapValues.get("acquirerCountryCode"), "El campo null debe permanecer null.");
        assertEquals("", mapValues.get("transactionCurrencyCode"), "El campo vacío debe permanecer vacío.");
    }

    @Test
    void adjustFields_shouldThrowException_ifValueIsTooShort() {
        Map<String, String> mapValues = new HashMap<>();
        mapValues.put("transactionCurrencyCode", "840");

        assertThrows(StringIndexOutOfBoundsException.class, () -> {
            visaDelegateParser.adjustFields(mapValues);
        }, "Debe lanzar StringIndexOutOfBoundsException porque '840'.substring(1, 4) requiere longitud >= 4.");
    }

    @Test
    void parser_shouldProcessStandardTransaction_AndAdjustFields() {
        String originalMessage = "MESSAGE";
        String expectedPciText = "MASCARA";

        Map<String, String> initialMap = new HashMap<>();
        initialMap.put("messageType", "0200");
        initialMap.put("transactionCurrencyCode", "0840");
        initialMap.put("pan", "1234567890123456");

        when(visaISOFieldParser.mapFields(originalMessage)).thenReturn(initialMap);
        when(visaISOFieldParser.unMapFieldsPlainText(anyMap())).thenReturn(expectedPciText);

        Map<String, String> result = visaDelegateParser.parser(originalMessage);

        assertNotNull(result);
        assertEquals("PEER01", result.get("networkName"));
        assertEquals("840", result.get("transactionCurrencyCode"));
        assertEquals(expectedPciText, result.get("plainTextPCI"));

        verify(visaISOFieldParser).mapFields(originalMessage);
        verify(visaISOFieldParser).unMapFieldsPlainText(anyMap());
    }

    @Test
    void parser_shouldSkipMasking_WhenMessageTypeIs0800() {
        String originalMessage = "ISO_0800_MESSAGE";
        Map<String, String> initialMap = new HashMap<>();
        initialMap.put("messageType", "0800");

        when(visaISOFieldParser.mapFields(originalMessage)).thenReturn(initialMap);

        Map<String, String> result = visaDelegateParser.parser(originalMessage);

        assertEquals("0800", result.get("plainTextPCI"));
    }

    @Test
    void parser_shouldSkipMasking_WhenMessageTypeIs0190() {
        String originalMessage = "ISO_0190_MESSAGE";
        Map<String, String> initialMap = new HashMap<>();
        initialMap.put("messageType", "0190");

        when(visaISOFieldParser.mapFields(originalMessage)).thenReturn(initialMap);

        Map<String, String> result = visaDelegateParser.parser(originalMessage);

        assertEquals("0190", result.get("plainTextPCI"));
    }

    @Test
    void parser_shouldReturnMessageType_WhenPciGenerationFails() {
        String originalMessage = "ISO_ERROR_MESSAGE";
        Map<String, String> initialMap = new HashMap<>();
        initialMap.put("messageType", "0210");
        initialMap.put("transactionCurrencyCode", "0840");

        when(visaISOFieldParser.mapFields(originalMessage)).thenReturn(initialMap);
        when(visaISOFieldParser.unMapFieldsPlainText(anyMap())).thenThrow(new RuntimeException("Error masking fields"));

        Map<String, String> result = visaDelegateParser.parser(originalMessage);

        assertEquals("0210", result.get("plainTextPCI"));
        assertEquals("840", result.get("transactionCurrencyCode"));
        assertEquals("PEER01", result.get("networkName"));
    }


}