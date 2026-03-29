package com.bbva.orchestrator.core.parser.iso8583.strategy.subfields;

import com.bbva.gateway.interceptors.GrpcHeadersInfo;
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

class CompositeFixedFieldParserTest {
    private CompositeFixedFieldParser parser;
    private MockedStatic<GrpcHeadersInfo> mockedHeaders;


    @BeforeEach
    void setUp() {
        mockedHeaders = mockStatic(GrpcHeadersInfo.class);
        CompositeFieldDefinition mockDefinition = mock(CompositeFieldDefinition.class);
        ParsedSubFieldResult mockSubField1 = mock(ParsedSubFieldResult.class);
        ParsedSubFieldResult mockSubField2 = mock(ParsedSubFieldResult.class);

        when(mockDefinition.getId()).thenReturn("field1");
        when(mockDefinition.getSubFields()).thenReturn(Arrays.asList(mockSubField1, mockSubField2));
        when(mockSubField1.id()).thenReturn("sub1");
        when(mockSubField1.length()).thenReturn(2);
        when(mockSubField2.id()).thenReturn("sub2");
        when(mockSubField2.length()).thenReturn(3);

        parser = new CompositeFixedFieldParser(Collections.singletonList(mockDefinition));
    }

    @AfterEach
    void tearDown() {
        mockedHeaders.close();
    }

    @Test
    void testParseReturnsEmptyMapIfDefinitionNotFound() {
        Map<String, String> result = parser.buildSubFieldsSpecific("unknown", "12345");
        assertTrue(result.isEmpty());
    }

    @Test
    void testParseReturnsEmptyMapIfRawValueIsNullOrEmpty() {
        assertTrue(parser.buildSubFieldsSpecific("field1", null).isEmpty());
        assertTrue(parser.buildSubFieldsSpecific("field1", "").isEmpty());
    }

    @Test
    void testParseReturnsSubFieldsCorrectly() {
        String rawValue = "12abc";
        Map<String, String> result = parser.buildSubFieldsSpecific("field1", rawValue);
        assertEquals(2, result.size());
        assertEquals("12", result.get("sub1"));
        assertEquals("abc", result.get("sub2"));
    }

    @Test
    void testParseStopsIfNotEnoughDataForSubField() {
        // rawValue solo tiene longitud suficiente para el primer subcampo
        String rawValue = "12"; // mockSubField1.length() = 2, mockSubField2.length() = 3
        Map<String, String> result = parser.buildSubFieldsSpecific("field1", rawValue);
        // Solo debe contener el primer subcampo
        assertEquals(1, result.size());
        assertEquals("12", result.get("sub1"));
        assertNull(result.get("sub2"));
    }

}
