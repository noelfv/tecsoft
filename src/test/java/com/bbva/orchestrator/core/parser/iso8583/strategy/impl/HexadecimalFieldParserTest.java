package com.bbva.orchestrator.core.parser.iso8583.strategy.impl;

import com.bbva.orchestrator.core.exception.ParserFieldsException;
import com.bbva.orchestrator.core.fields.definitions.IFieldDefinition;
import com.bbva.orchestrator.core.parser.iso8583.ParsedFieldResult;
import com.bbva.orchestrator.core.parser.iso8583.handlers.NetworkHandlerField;
import com.bbva.orchestrator.core.parser.iso8583.strategy.fields.HexadecimalFieldParser;
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

class HexadecimalFieldParserTest {

    @Mock
    private NetworkHandlerField networkHandlerField;

    @Mock
    private IFieldDefinition fieldDefinition;

    @InjectMocks
    private HexadecimalFieldParser hexadecimalFieldParser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void parse_success_returnsParsedResult() {
        String rawDataSegment = "01A2B3C4";
        int expectedHexLength = 8;
        when(networkHandlerField.decodeLengthField(any(IFieldDefinition.class))).thenReturn(expectedHexLength);
        ParsedFieldResult result = hexadecimalFieldParser.parse(rawDataSegment, fieldDefinition, networkHandlerField);
        assertNotNull(result);
        assertEquals(rawDataSegment, result.value());
    }

    @Test
    void parse_shortRawDataSegment_throwsParserLocalException() {
        String rawDataSegment = "01A2B3";
        int expectedHexLength = 8;
        when(networkHandlerField.decodeLengthField(any(IFieldDefinition.class))).thenReturn(expectedHexLength);
        when(fieldDefinition.getIdentifier()).thenReturn("FIELD_01");
        assertThrows(ParserFieldsException.class, () -> {
            hexadecimalFieldParser.parse(rawDataSegment, fieldDefinition, networkHandlerField);
        }, "Se esperaba una ParserLocalException debido al segmento de datos demasiado corto.");
    }

    @Test
    void build_success_returnsEncodedString() {
        String processedDataSegment = "ABCDEF";
        String expectedEncodedString = "3031414243444546";
        when(networkHandlerField.encode(anyString(), any())).thenReturn(expectedEncodedString);
        String result = hexadecimalFieldParser.build(processedDataSegment, fieldDefinition, networkHandlerField);
        assertNotNull(result);
        assertEquals(expectedEncodedString, result);
    }
}