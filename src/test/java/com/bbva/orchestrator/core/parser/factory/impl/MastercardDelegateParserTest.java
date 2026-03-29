package com.bbva.orchestrator.core.parser.factory.impl;

import com.bbva.orchestrator.core.network.mastercard.MastercardProcessField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MastercardDelegateParserTest {

    private MastercardProcessField fieldParser;
    private MastercardDelegateParser parser;

    @BeforeEach
    void setUp() {
        fieldParser = mock(MastercardProcessField.class);
        parser = new MastercardDelegateParser(fieldParser);
    }

    @Test
    void testUnParser() {
        Map<String, String> map = new HashMap<>();
        when(fieldParser.unMapFields(map)).thenReturn("unparsed");
        assertEquals("unparsed", parser.unParser(map));
    }

    @Test
    void testUnParserPlainText() {
        Map<String, String> map = new HashMap<>();
        when(fieldParser.unMapFieldsPlainText(map)).thenReturn("plain");
        assertEquals("plain", parser.unParserPlainText(map));
    }

    @Test
    void testParser_messageTypeStartsWith08() {
        Map<String, String> map = new HashMap<>();
        map.put("messageType", "08X");
        when(fieldParser.mapFields("msg")).thenReturn(map);
        Map<String, String> result = parser.parser("msg");
        assertEquals("PEER02", result.get("networkName"));
        assertEquals("08X", result.get("plainTextPCI"));
    }

    @Test
    void testParser_messageTypeStartsWith019() {
        Map<String, String> map = new HashMap<>();
        map.put("messageType", "019Y");
        when(fieldParser.mapFields("msg")).thenReturn(map);
        Map<String, String> result = parser.parser("msg");
        assertEquals("PEER02", result.get("networkName"));
        assertEquals("019Y", result.get("plainTextPCI"));
    }

    @Test
    void testParser_messageTypeOther_noException() {
        Map<String, String> map = new HashMap<>();
        map.put("messageType", "1234");
        Map<String, String> masked = new HashMap<>();
        when(fieldParser.mapFields("msg")).thenReturn(map);
        try (var mocked = mockStatic(com.bbva.orchestrator.core.utils.ParserUtil.class)) {
            mocked.when(() -> com.bbva.orchestrator.core.utils.ParserUtil.maskSensitiveFields(map)).thenReturn(masked);
            when(fieldParser.unMapFieldsPlainText(masked)).thenReturn("maskedResult");
            Map<String, String> result = parser.parser("msg");
            assertEquals("maskedResult", result.get("plainTextPCI"));
        }
    }

    @Test
    void testParser_messageTypeOther_withException() {
        Map<String, String> map = new HashMap<>();
        map.put("messageType", "1234");
        when(fieldParser.mapFields("msg")).thenReturn(map);
        try (var mocked = mockStatic(com.bbva.orchestrator.core.utils.ParserUtil.class)) {
            mocked.when(() -> com.bbva.orchestrator.core.utils.ParserUtil.maskSensitiveFields(map)).thenThrow(new RuntimeException());
            Map<String, String> result = parser.parser("msg");
            assertEquals("1234", result.get("plainTextPCI"));
        }
    }

}
