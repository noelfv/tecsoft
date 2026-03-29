package com.bbva.orchestrator.core.parser.iso8583.strategy.subfields;

import com.bbva.gateway.utils.LogsTraces;
import com.bbva.orchestrator.core.fields.definitions.subfields.fixed.CompositeFieldDefinition;
import com.bbva.orchestrator.core.parser.iso8583.ParsedSubFieldResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CompositeVariableFieldParserTest {

    private CompositeVariableFieldParser parser;

    private MockedStatic<LogsTraces> mockedLogs;
    private CompositeFieldDefinition mockDefinition;
    private ParsedSubFieldResult mockSubField1;
    private ParsedSubFieldResult mockSubField2;

    @BeforeEach
    void setUp() {
        mockedLogs = mockStatic(LogsTraces.class);

        mockDefinition = mock(CompositeFieldDefinition.class);
        mockSubField1 = mock(ParsedSubFieldResult.class);
        mockSubField2 = mock(ParsedSubFieldResult.class);

        when(mockDefinition.getId()).thenReturn("60");
        when(mockDefinition.getSubFields()).thenReturn(Arrays.asList(mockSubField1, mockSubField2));

        when(mockSubField1.id()).thenReturn("60.01");
        when(mockSubField1.length()).thenReturn(2);

        when(mockSubField2.id()).thenReturn("60.02");
        when(mockSubField2.length()).thenReturn(3);

        parser = new CompositeVariableFieldParser(Collections.singletonList(mockDefinition));
    }

    @AfterEach
    void tearDown() {
        mockedLogs.close();
    }

    @Test
    void testParseReturnsEmptyMapIfDefinitionNotFound() {
        Map<String, String> result = parser.buildSubFieldsSpecific("99", "12345");
        assertTrue(result.isEmpty());
    }

    @Test
    void testParseReturnsEmptyMapIfRawValueIsNullOrEmpty() {
        assertTrue(parser.buildSubFieldsSpecific("60", null).isEmpty());
        assertTrue(parser.buildSubFieldsSpecific("60", "").isEmpty());
    }

    @Test
    void testParseReturnsAllSubFieldsCorrectly() {
        String rawValue = "12abc";

        Map<String, String> result = parser.buildSubFieldsSpecific("60", rawValue);

        assertEquals(2, result.size());
        assertEquals("12", result.get("60.01"));
        assertEquals("abc", result.get("60.02"));
    }

    @Test
    void testParseTruncatedValid() {
        String rawValue = "12";

        Map<String, String> result = parser.buildSubFieldsSpecific("60", rawValue);

        // Debe haber procesado el primero y parado limpiamente
        assertEquals(1, result.size());
        assertEquals("12", result.get("60.01"));
        assertNull(result.get("60.02"));
    }

    @Test
    void testParseStopsIfIntegrityCutOff() {
        String rawValue = "12ab";

        Map<String, String> result = parser.buildSubFieldsSpecific("60", rawValue);

        assertEquals(1, result.size());
        assertEquals("12", result.get("60.01"));
        assertNull(result.get("60.02"));
    }

    @Test
    void testParseOverflowWarning() {
        String rawValue = "12abcXYZ";

        Map<String, String> result = parser.buildSubFieldsSpecific("60", rawValue);

        assertEquals(2, result.size());
        assertEquals("12", result.get("60.01"));
        assertEquals("abc", result.get("60.02"));

        mockedLogs.verify(() ->
                        LogsTraces.writeWarning(contains("Data restante en campo 60 sin definición")),
                times(1)
        );
    }
}