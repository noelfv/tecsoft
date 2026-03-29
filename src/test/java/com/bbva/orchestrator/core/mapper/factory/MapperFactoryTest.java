package com.bbva.orchestrator.core.mapper.factory;

import com.bbva.gateway.interceptors.GrpcHeadersInfo;
import com.bbva.orchestrator.core.mapper.factory.impl.DefaultDelegateMapper;
import com.bbva.orchestrator.core.mapper.factory.impl.MastercardDelegateMapper;
import com.bbva.orchestrator.core.mapper.factory.impl.VisaDelegateMapper;
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
class MapperFactoryTest {

    @Mock
    private VisaDelegateMapper visaMapper;
    @Mock
    private MastercardDelegateMapper masterCardMapper;
    @Mock
    private DefaultDelegateMapper defaultMapper;

    private MapperFactory mapperFactory;

    private MockedStatic<GrpcHeadersInfo> mockedHeaders;

    @BeforeEach
    void setUp() {
        mockedHeaders = mockStatic(GrpcHeadersInfo.class);
    }

    @AfterEach
    void tearDown() {
        mockedHeaders.close();
    }

    @Test
    void getDelegateMapper_withNullPeerId_shouldReturnDefault() {
        mapperFactory = new MapperFactory(List.of(defaultMapper));
        ISO20022DelegateMapper result = mapperFactory.getDelegateMapper(null);
        assertEquals(defaultMapper, result);
    }

    @Test
    void getDelegateMapper_withEmptyPeerId_shouldReturnDefault() {
        mapperFactory = new MapperFactory(List.of(defaultMapper));
        ISO20022DelegateMapper result = mapperFactory.getDelegateMapper("");
        assertEquals(defaultMapper, result);
    }

    @Test
    void getDelegateMapper_noArgs_shouldUseGrpcHeaders() {
        mapperFactory = new MapperFactory(List.of(visaMapper));
        when(GrpcHeadersInfo.getNetwork()).thenReturn("peer01");

        ISO20022DelegateMapper result = mapperFactory.getDelegateMapper();

        assertEquals(visaMapper, result);
    }

    @Test
    void getDefaultMapper_whenNoDefaultBean_shouldReturnFirstName() {
        mapperFactory = new MapperFactory(List.of(visaMapper, masterCardMapper));
        ISO20022DelegateMapper result = mapperFactory.getDelegateMapper("unknown");

        assertNotNull(result);
        assertTrue(result == visaMapper || result == masterCardMapper);
    }

    @Test
    void getDefaultMapper_whenNoParsersExist_shouldThrowException() {
        mapperFactory = new MapperFactory(Collections.emptyList());

        assertThrows(IllegalStateException.class, () -> {
            mapperFactory.getDelegateMapper("any_peer");
        });
    }
}