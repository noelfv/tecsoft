package com.bbva.orchestrator.core.parser.iso8583.strategy.impl;

import com.bbva.orchestrator.core.exception.ParserFieldsException;
import com.bbva.orchestrator.core.fields.definitions.IFieldDefinition;
import com.bbva.orchestrator.core.parser.iso8583.ParsedFieldResult;
import com.bbva.orchestrator.core.parser.iso8583.handlers.NetworkHandlerField;
import com.bbva.orchestrator.core.parser.iso8583.strategy.fields.BinaryStringFieldParser;
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

class BinaryStringFieldParserTest {

    @Mock
    private NetworkHandlerField networkHandlerField;

    @Mock
    private IFieldDefinition fieldDefinition;

    @InjectMocks
    private BinaryStringFieldParser binaryStringFieldParser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void parse_success_returnsParsedResult() {
        // Arrange
        String rawDataSegment = "01A2B3C4D5";
        int expectedHexLength = 10;
        String decodedValue = "01A2B3C4D5";
        when(networkHandlerField.decodeLengthField(any(IFieldDefinition.class))).thenReturn(expectedHexLength);
        when(networkHandlerField.decode(anyString(), any())).thenReturn(decodedValue);

        // Act
        ParsedFieldResult result = binaryStringFieldParser.parse(rawDataSegment, fieldDefinition, networkHandlerField);

        // Assert
        assertNotNull(result);
        assertEquals(decodedValue, result.value());
    }

    @Test
    void parse_runtimeException_throwsParserLocalException() {
        // Arrange
        String rawDataSegment = "01A2B";
        when(fieldDefinition.getIdentifier()).thenReturn("FIELD_BINARY");
        when(networkHandlerField.decodeLengthField(any(IFieldDefinition.class))).thenReturn(10);

        // Act & Assert
        assertThrows(ParserFieldsException.class, () -> {
            binaryStringFieldParser.parse(rawDataSegment, fieldDefinition, networkHandlerField);
        }, "Se esperaba una ParserLocalException debido al segmento de datos demasiado corto.");
    }

    @Test
    void build_success_returnsEncodedString() {
        // Arrange
        String processedDataSegment = "ABCDE";
        String encodedValue = "4142434445";
        when(networkHandlerField.encode(anyString(), any())).thenReturn(encodedValue);

        // Act
        String result = binaryStringFieldParser.build(processedDataSegment, fieldDefinition, networkHandlerField);

        // Assert
        assertNotNull(result);
        assertEquals(encodedValue, result);
    }
}