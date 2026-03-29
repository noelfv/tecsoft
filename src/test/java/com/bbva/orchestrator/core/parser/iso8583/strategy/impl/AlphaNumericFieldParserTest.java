package com.bbva.orchestrator.core.parser.iso8583.strategy.impl;

import com.bbva.orchestrator.core.exception.ParserFieldsException;
import com.bbva.orchestrator.core.fields.definitions.IFieldDefinition;
import com.bbva.orchestrator.core.parser.iso8583.ParsedFieldResult;
import com.bbva.orchestrator.core.parser.iso8583.handlers.NetworkHandlerField;
import com.bbva.orchestrator.core.parser.iso8583.strategy.fields.AlphaNumericFieldParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class AlphaNumericFieldParserTest {

    @Mock
    private NetworkHandlerField networkHandlerField;

    @Mock
    private IFieldDefinition fieldDefinition;

    @InjectMocks
    private AlphaNumericFieldParser alphaNumericFieldParser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void parse_success_returnsParsedResult() {
        // Arrange
        String rawDataSegment = "414243444546"; // "ABCDEF" en hexadecimal
        int expectedHexLength = 12;
        String decodedValue = "ABCDEF";
        when(networkHandlerField.decodeLengthField(any(IFieldDefinition.class))).thenReturn(expectedHexLength);
        when(networkHandlerField.decode(anyString(), any())).thenReturn(decodedValue);

        // Act
        ParsedFieldResult result = alphaNumericFieldParser.parse(rawDataSegment, fieldDefinition, networkHandlerField);

        // Assert
        assertNotNull(result);
        assertEquals(decodedValue, result.value());
    }

    @Test
    void parse_runtimeException_throwsParserLocalException() {
        // Arrange
        String rawDataSegment = "414243";
        when(fieldDefinition.getIdentifier()).thenReturn("FIELD_ALPHANUMERIC");
        when(networkHandlerField.decodeLengthField(any(IFieldDefinition.class))).thenReturn(12);

        // Act & Assert
        assertThrows(ParserFieldsException.class, () -> {
            alphaNumericFieldParser.parse(rawDataSegment, fieldDefinition, networkHandlerField);
        }, "Se esperaba una ParserLocalException debido al segmento de datos demasiado corto.");
    }

    @Test
    void build_success_returnsEncodedString() {
        // Arrange
        String processedDataSegment = "ABCDEF";
        String encodedValue = "414243444546";
        when(networkHandlerField.encode(anyString(), any())).thenReturn(encodedValue);

        // Act
        String result = alphaNumericFieldParser.build(processedDataSegment, fieldDefinition, networkHandlerField);

        // Assert
        assertNotNull(result);
        assertEquals(encodedValue, result);
    }
}