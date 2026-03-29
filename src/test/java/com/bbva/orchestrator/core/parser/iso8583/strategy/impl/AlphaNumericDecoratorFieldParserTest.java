package com.bbva.orchestrator.core.parser.iso8583.strategy.impl;

import com.bbva.orchestrator.core.exception.ParserFieldsException;
import com.bbva.orchestrator.core.fields.definitions.IFieldDefinition;
import com.bbva.orchestrator.core.fields.definitions.ISODataType;
import com.bbva.orchestrator.core.parser.iso8583.ParsedFieldResult;
import com.bbva.orchestrator.core.parser.iso8583.handlers.NetworkHandlerField;
import com.bbva.orchestrator.core.parser.iso8583.strategy.fields.AlphaNumericDecoratorFieldParser;
import com.bbva.orchestrator.core.utils.ISOUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlphaNumericDecoratorFieldParserTest {

    @Mock
    private NetworkHandlerField networkHandlerField;

    @Mock
    private IFieldDefinition fieldDefinition;

    @InjectMocks
    private AlphaNumericDecoratorFieldParser alphaNumericDecoratorFieldParser;

    @Test
    void parse_success_returnsDecodedValue() {
        // Arrange
        String rawDataSegment = "C1C2C3C4";
        String decodedValue = "ABCD";
        int expectedLength = 0;

        when(networkHandlerField.decode(anyString(), any())).thenReturn(decodedValue);

        // Act
        ParsedFieldResult result = alphaNumericDecoratorFieldParser.parse(rawDataSegment, fieldDefinition, networkHandlerField);

        // Assert
        assertNotNull(result);
        assertEquals(decodedValue, result.value());
    }

    @Test
    void parse_runtimeException_throwsParserLocalException() {
        // Arrange
        String rawDataSegment = "C1C2C3C4";
        when(fieldDefinition.getIdentifier()).thenReturn("FIELD_DECORATOR");
        when(networkHandlerField.decode(anyString(), any())).thenThrow(new RuntimeException("Test Exception"));

        // Act & Assert
        assertThrows(ParserFieldsException.class, () -> {
            alphaNumericDecoratorFieldParser.parse(rawDataSegment, fieldDefinition, networkHandlerField);
        }, "Se esperaba una ParserLocalException debido a una RuntimeException subyacente.");
    }

    @Test
    void build_alphaNumericType_returnsEBCDICHex() {
        // Arrange
        try (MockedStatic<ISOUtil> mockedISOUtil = mockStatic(ISOUtil.class)) {
            String processedDataSegment = "ABC";
            String ebcdicHexValue = "C1C2C3";
            when(fieldDefinition.getTypeData()).thenReturn(ISODataType.ALPHA_NUMERIC);
            mockedISOUtil.when(() -> ISOUtil.stringToEBCDICHex(processedDataSegment)).thenReturn(ebcdicHexValue);

            // Act
            String result = alphaNumericDecoratorFieldParser.build(processedDataSegment, fieldDefinition, networkHandlerField);

            // Assert
            assertNotNull(result);
            assertEquals(ebcdicHexValue, result);
        }
    }

    @Test
    void build_otherType_returnsProcessedDataSegment() {
        // Arrange
        String processedDataSegment = "12345";
        when(fieldDefinition.getTypeData()).thenReturn(ISODataType.NUMERIC);

        // Act
        String result = alphaNumericDecoratorFieldParser.build(processedDataSegment, fieldDefinition, networkHandlerField);

        // Assert
        assertNotNull(result);
        assertEquals(processedDataSegment, result);
    }

    @Test
    void build_runtimeException_throwsParserLocalException() {
        // Arrange
        try (MockedStatic<ISOUtil> mockedISOUtil = mockStatic(ISOUtil.class)) {
            String processedDataSegment = "ABC";
            when(fieldDefinition.getIdentifier()).thenReturn("FIELD_DECORATOR");
            when(fieldDefinition.getName()).thenReturn("Decorator Field");
            when(fieldDefinition.getTypeData()).thenReturn(ISODataType.ALPHA_NUMERIC);
            mockedISOUtil.when(() -> ISOUtil.stringToEBCDICHex(processedDataSegment)).thenThrow(new RuntimeException("Test Exception"));

            // Act & Assert
            assertThrows(ParserFieldsException.class, () -> {
                alphaNumericDecoratorFieldParser.build(processedDataSegment, fieldDefinition, networkHandlerField);
            }, "Se esperaba una ParserLocalException debido a una RuntimeException subyacente.");
        }
    }
}