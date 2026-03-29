package com.bbva.orchestrator.core.parser.factory.impl;

import com.bbva.gateway.interceptors.GrpcHeadersInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import java.util.Collections;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

class DefaultDelegateParserTest {

    private DefaultDelegateParser defaultDelegateParser;
    MockedStatic<GrpcHeadersInfo> grpcHeadersInfo = mockStatic(GrpcHeadersInfo.class);


    @BeforeEach
    void setUp() {
        defaultDelegateParser = new DefaultDelegateParser();
    }
    @AfterEach
    void tearDown() {
        grpcHeadersInfo.close();
    }

    @Test
    void parser_shouldThrowUnsupportedOperationException() {
        String dummyMessage = "mensaje para error";
        grpcHeadersInfo.when(GrpcHeadersInfo::getNetwork).thenReturn("peer03");
        Exception exception = assertThrows(UnsupportedOperationException.class, () -> {
            defaultDelegateParser.parser(dummyMessage);
        });

        String expectedMessage = "RED NO SOPORTADA : peer03" ;
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void unParser_shouldThrowUnsupportedOperationException() {
        Map<String, String> dummyMap = Collections.emptyMap();

        Exception exception = assertThrows(UnsupportedOperationException.class, () -> {
            defaultDelegateParser.unParser(dummyMap);
        });

        String expectedMessage = "RED NO SOPORTADA : null";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void unParserPlainText_shouldThrowUnsupportedOperationException() {
        grpcHeadersInfo.when(GrpcHeadersInfo::getNetwork).thenReturn("peer03");
        Exception exception = assertThrows(UnsupportedOperationException.class, () -> {
            defaultDelegateParser.unParserPlainText(Collections.emptyMap());
        });
        String expectedMessage = "RED NO SOPORTADA : peer03";
        assertEquals(expectedMessage, exception.getMessage());
    }

}