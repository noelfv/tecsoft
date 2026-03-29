package com.bbva.orchestrator.core.parser.iso8583.strategy.impl;

import com.bbva.orchestrator.core.exception.ParserFieldsException;
import com.bbva.orchestrator.core.fields.definitions.IFieldDefinition;
import com.bbva.orchestrator.core.parser.iso8583.ParsedFieldResult;
import com.bbva.orchestrator.core.parser.iso8583.handlers.NetworkHandlerField;
import com.bbva.orchestrator.core.parser.iso8583.strategy.fields.NumericDecimalFieldParser;
import com.bbva.orchestrator.core.utils.FieldUtil;
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
class NumericDecimalFieldParserTest {

    @Mock
    private NetworkHandlerField networkHandlerField;

    @Mock
    private IFieldDefinition fieldDefinition;

    @InjectMocks
    private NumericDecimalFieldParser numericDecimalFieldParser;

    @Test
    void parse_success_returnsParsedResult() {
        // Arrange
        // 👇 CAMBIO CLAVE: Mockear FieldUtil en lugar de ISOUtil
        try (MockedStatic<FieldUtil> mockedFieldUtil = mockStatic(FieldUtil.class)) {
            String rawDataSegment = "12345678";
            int expectedHexLength = 8;
            String decodedValue = "12345678";
            String decodedDecimalValue = "123456.78";

            when(networkHandlerField.decodeLengthField(any(IFieldDefinition.class))).thenReturn(expectedHexLength);
            when(networkHandlerField.decode(anyString(), any())).thenReturn(decodedValue);

            // 👇 Usar el mock correcto (mockedFieldUtil) para configurar la llamada
            mockedFieldUtil.when(() -> FieldUtil.validAmount(decodedValue)).thenReturn(decodedDecimalValue);

            // Act
            ParsedFieldResult result = numericDecimalFieldParser.parse(rawDataSegment, fieldDefinition, networkHandlerField);

            // Assert
            assertNotNull(result);
            assertEquals(decodedDecimalValue, result.value());
        }
    }

    @Test
    void parse_runtimeException_throwsParserLocalException() {
        // Arrange
        String rawDataSegment = "1234";
        when(fieldDefinition.getIdentifier()).thenReturn("FIELD_NUMERIC");
        when(networkHandlerField.decodeLengthField(any(IFieldDefinition.class))).thenReturn(8);

        // Act & Assert
        assertThrows(ParserFieldsException.class, () -> {
            numericDecimalFieldParser.parse(rawDataSegment, fieldDefinition, networkHandlerField);
        });
    }

    @Test
    void build_success_returnsEncodedString() {
        // Arrange
        try (MockedStatic<FieldUtil> mockedISOUtil = mockStatic(FieldUtil.class)) {
            String processedDataSegment = "123456.78";
            String cleanValue = "12345678";
            String encodedValue = "3132333435363738";

            mockedISOUtil.when(() -> FieldUtil.revertValidAmount(processedDataSegment)).thenReturn(cleanValue);
            when(networkHandlerField.encode(anyString(), any())).thenReturn(encodedValue);

            // Act
            String result = numericDecimalFieldParser.build(processedDataSegment, fieldDefinition, networkHandlerField);

            // Assert
            assertNotNull(result);
            assertEquals(encodedValue, result);
        }
    }
}