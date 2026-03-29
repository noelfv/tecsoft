package com.bbva.orchestrator.core.parser.factory;

import com.bbva.gateway.interceptors.GrpcHeadersInfo;
import com.bbva.orchestrator.core.parser.factory.impl.DefaultDelegateParser;
import com.bbva.orchestrator.core.parser.factory.impl.MastercardDelegateParser;
import com.bbva.orchestrator.core.parser.factory.impl.VisaDelegateParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParserFactoryTest {

    @Mock
    private VisaDelegateParser visaParser;
    @Mock
    private MastercardDelegateParser masterCardParser;
    @Mock
    private DefaultDelegateParser defaultParser;

    private ParserFactory parserFactory;

    private MockedStatic<GrpcHeadersInfo> mockedHeaders;

    @BeforeEach
    void setUp() {
        parserFactory = new ParserFactory(List.of(visaParser, masterCardParser, defaultParser));

        mockedHeaders = mockStatic(GrpcHeadersInfo.class);
    }

    @AfterEach
    void tearDown() {
        mockedHeaders.close();
    }

    @Test
    void getDelegateParser_withPeer01_shouldReturnVisaParser() {
        ISO8583DelegateParser result = parserFactory.getDelegateParser("peer01");

        assertEquals(visaParser, result);
    }

    @Test
    void getDelegateParser_withPeer02_shouldReturnMasterCardParser() {
        ISO8583DelegateParser result = parserFactory.getDelegateParser("peer02");

        assertEquals(masterCardParser, result);
    }

    @Test
    void getDelegateParser_withUnknownPeer_shouldReturnDefaultParser() {
        ISO8583DelegateParser result = parserFactory.getDelegateParser("unknown_peer");

        assertEquals(defaultParser, result);
    }

    @Test
    void getDelegateParser_withNullPeerId_shouldReturnDefaultParser() {
        ISO8583DelegateParser result = parserFactory.getDelegateParser(null);

        assertEquals(defaultParser, result);
    }

    @Test
    void getDelegateParser_noArgs_shouldUseGrpcHeadersAndReturnCorrectParser() {
        when(GrpcHeadersInfo.getNetwork()).thenReturn("peer02");

        ISO8583DelegateParser result = parserFactory.getDelegateParser();

        assertEquals(masterCardParser, result);
    }

    @Test
    void getDefaultParser_whenNoDefaultBeanExists_shouldReturnFirstAvailable() {

        ParserFactory factoryWithoutDefault = new ParserFactory(List.of(visaParser, masterCardParser));

        ISO8583DelegateParser result = factoryWithoutDefault.getDelegateParser("unknown_peer");

        assertNotNull(result);
        assertTrue(result == visaParser || result == masterCardParser);
    }

    @Test
    void getDefaultParser_whenNoParsersAvailable_shouldThrowException() {

        ParserFactory emptyFactory = new ParserFactory(Collections.emptyList());

        assertThrows(IllegalStateException.class, () -> {
            emptyFactory.getDelegateParser("any_peer");
        });
    }
}