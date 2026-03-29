package com.bbva.orchestrator.core.parser.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ISO8583DelegateParserTest {

    private ISO8583DelegateParser testParser;

    // Implementación anónima
    @BeforeEach
    void setUp() {
        testParser = new ISO8583DelegateParser() {
            @Override
            public Map<String, String> parser(String originalMessage) {
                return null;
            }

            @Override
            public String unParser(Map<String, String> mappedFields) {
                return null;
            }

           /* @Override
            public Map<String, String> parserSubFields(ISO8583 values) {
                return Map.of();
            }*/

            @Override
            public String unParserPlainText(Map<String, String> mappedFields) {
                return "";
            }
        };
    }

  /*  @Test
    void parserSubFields_shouldReturnNewEmptyMap() {
        ISO8583 iso8583 = ISO8583.builder().build();
        Map<String, String> resultMap = testParser.parserSubFields(iso8583);

        assertNotNull(resultMap);
        assertTrue(resultMap.isEmpty());
        assertNotSame(iso8583, resultMap);
    }*/

    @Test
    void unParserPlainText_shouldReturnNull() {
        Map<String, String> inputMap = new HashMap<>();

        String result = testParser.unParserPlainText(inputMap);

        assertEquals("",result);
    }
}