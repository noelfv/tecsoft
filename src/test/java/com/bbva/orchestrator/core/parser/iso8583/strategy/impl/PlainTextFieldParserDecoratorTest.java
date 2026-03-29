package com.bbva.orchestrator.core.parser.iso8583.strategy.impl;

import com.bbva.orchestrator.core.fields.definitions.IFieldDefinition;
import com.bbva.orchestrator.core.fields.definitions.ISODataType;
import com.bbva.orchestrator.core.parser.iso8583.handlers.impl.MastercardHandlerField;
import com.bbva.orchestrator.core.parser.iso8583.strategy.fields.PlainTextFieldParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlainTextFieldParserDecoratorTest {

    @Mock
    private IFieldDefinition mockFieldDefinition;

    @InjectMocks
    private PlainTextFieldParser decorator;

    private MastercardHandlerField mastercardHandlerField;

    @Test
    void parse_shouldAlwaysThrowUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> {
            decorator.parse("some_data",  mockFieldDefinition,mastercardHandlerField);
        });
    }

    @Test
    void build_withNullFieldValue_shouldReturnEmptyString() {
        String result = decorator.build(null, mockFieldDefinition,mastercardHandlerField);
        assertEquals("", result);
    }

    @Test
    void build_withEmptyFieldValue_shouldReturnEmptyString() {
        String result = decorator.build("", mockFieldDefinition,mastercardHandlerField);
        assertEquals("", result);
    }

    @Test
    void build_withVariableField_shouldReturnLengthPrefixAndValue() {
        String fieldValue = "someValue";
        when(mockFieldDefinition.isVariable()).thenReturn(true);
        when(mockFieldDefinition.getLength()).thenReturn(2);

        String result = decorator.build(fieldValue, mockFieldDefinition,mastercardHandlerField);

        assertEquals("09someValue", result);
    }

    @Test
    void build_withSimpleNumericDecimalField_shouldReturnSanitizedValue() {
        String fieldValue = "123.45";
        when(mockFieldDefinition.isVariable()).thenReturn(false);
        when(mockFieldDefinition.getTypeData()).thenReturn(ISODataType.NUMERIC_DECIMAL);

        String result = decorator.build(fieldValue, mockFieldDefinition,mastercardHandlerField);

        assertEquals("12345", result);
    }

    @Test
    void build_withSimpleDefaultField_shouldReturnValueAsIs() {
        String fieldValue = "someValue";
        when(mockFieldDefinition.isVariable()).thenReturn(false);
        when(mockFieldDefinition.getTypeData()).thenReturn(ISODataType.ALPHA_NUMERIC);

        String result = decorator.build(fieldValue, mockFieldDefinition,mastercardHandlerField);

        assertEquals("someValue", result);
    }

    @Test
    void build_withNullFieldDefinition_shouldThrowNullPointerException() {
        String fieldValue = "someValue";

        assertThrows(NullPointerException.class, () -> {
            decorator.build(fieldValue, null,mastercardHandlerField);
        });
    }
}