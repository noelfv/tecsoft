package com.bbva.orchestrator.core.parser.iso8583.strategy.impl;

import com.bbva.orchestrator.core.exception.ParserFieldsException;
import com.bbva.orchestrator.core.fields.definitions.IFieldDefinition;
import com.bbva.orchestrator.core.parser.iso8583.ParsedFieldResult;
import com.bbva.orchestrator.core.parser.iso8583.handlers.NetworkHandlerField;
import com.bbva.orchestrator.core.parser.iso8583.strategy.FieldParserStrategy;
import com.bbva.orchestrator.core.parser.iso8583.strategy.fields.LlvarLengthPrefixParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class LlvarLengthPrefixParserTest {

    @Mock
    private FieldParserStrategy actualValueParser;

    @Mock
    private NetworkHandlerField networkHandlerField;

    @Mock
    private IFieldDefinition fieldDefinition;

    @InjectMocks
    private LlvarLengthPrefixParser llvarLengthPrefixParser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        llvarLengthPrefixParser = new LlvarLengthPrefixParser(actualValueParser);
    }

    @Test
    void parse_success_returnsParsedResult() {
        String rawDataSegment = "05ABCDE";
        int prefixLength = 2;
        int actualValueDecLength = 5;
        String actualValueResult = "ABCDE";

        when(networkHandlerField.getHeaderFieldVar(any(IFieldDefinition.class))).thenReturn(prefixLength);
        when(networkHandlerField.decodeHeaderFieldVar(anyInt(), anyString(), any(IFieldDefinition.class))).thenReturn(actualValueDecLength);
        when(networkHandlerField.decode(anyString(), any())).thenReturn(actualValueResult);

        // Act
        ParsedFieldResult result = llvarLengthPrefixParser.parse(rawDataSegment, fieldDefinition, networkHandlerField);

        // Assert
        assertNotNull(result);
        assertEquals(actualValueResult, result.value());
    }

    @Test
    void parse_runtimeException_throwsParserLocalException() {
        String rawDataSegment = "05ABC";
        int prefixLength = 2;
        int actualValueDecLength = 5;

        when(networkHandlerField.getHeaderFieldVar(any(IFieldDefinition.class))).thenReturn(prefixLength);
        when(networkHandlerField.decodeHeaderFieldVar(anyInt(), anyString(), any(IFieldDefinition.class))).thenReturn(actualValueDecLength);
        when(networkHandlerField.decode(anyString(), any())).thenThrow(new RuntimeException());
        when(fieldDefinition.getIdentifier()).thenReturn("FIELD_LLVAR");

        assertThrows(ParserFieldsException.class, () -> {
            llvarLengthPrefixParser.parse(rawDataSegment, fieldDefinition, networkHandlerField);
        }, "Se esperaba una ParserLocalException debido a una RuntimeException subyacente.");
    }

    @Test
    void build_success_returnsCombinedString() {
        String fieldValue = "ABCDE";
        String prefixHex = "0005";
        String actualRawValueHex = "4142434445";
        String expectedCombinedString = prefixHex + actualRawValueHex;

        when(networkHandlerField.encodeHeaderFieldVar(anyString(), any(IFieldDefinition.class))).thenReturn(prefixHex);
        when(networkHandlerField.encode(anyString(), any())).thenReturn(actualRawValueHex);

        String result = llvarLengthPrefixParser.build(fieldValue, fieldDefinition, networkHandlerField);

        assertNotNull(result);
        assertEquals(expectedCombinedString, result);
    }

    @Test
    void parse_success_appliesLeftZeroValidation_retainsZeroIfLengthMatches() {
        String rawDataSegment = "0A0123456789";
        int prefixLength = 2;
        String actualValueWithZero = "0123456789";
        when(networkHandlerField.getHeaderFieldVar(any(IFieldDefinition.class))).thenReturn(prefixLength);

        when(networkHandlerField.decodeHeaderFieldVar(anyInt(), anyString(), any(IFieldDefinition.class))).thenReturn(10);

        when(networkHandlerField.decode(anyString(), any())).thenReturn(actualValueWithZero);

        ParsedFieldResult result = llvarLengthPrefixParser.parse(rawDataSegment, fieldDefinition, networkHandlerField);

        assertNotNull(result);
        assertEquals(actualValueWithZero, result.value(), "Debe mantener el '0' ya que la longitud prefijo (10) coincide con la longitud de datos (10).");
    }

    @Test
    void parse_success_appliesLeftZeroValidation_noZeroPresent() {
        String rawDataSegment = "0712345678";
        int prefixLength = 2;
        String actualValue = "1234567";

        when(networkHandlerField.getHeaderFieldVar(any(IFieldDefinition.class))).thenReturn(prefixLength);

        when(networkHandlerField.decodeHeaderFieldVar(anyInt(), anyString(), any(IFieldDefinition.class))).thenReturn(7);

        when(networkHandlerField.decode(anyString(), any())).thenReturn(actualValue);

        ParsedFieldResult result = llvarLengthPrefixParser.parse(rawDataSegment, fieldDefinition, networkHandlerField);

        assertNotNull(result);
        assertEquals(actualValue, result.value(), "El valor no empieza con '0', debe ser devuelto sin cambios.");
    }
}