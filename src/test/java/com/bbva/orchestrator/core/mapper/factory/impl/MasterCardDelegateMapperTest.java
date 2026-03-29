package com.bbva.orchestrator.core.mapper.factory.impl;

import com.bbva.gateway.dto.iso20022.ISO20022;
import com.bbva.orchestrator.core.dto.ISO8583;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Collections;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class MasterCardDelegateMapperTest {

    @Mock
    private DefaultDelegateMapper mockDelegate;

    @InjectMocks
    private MastercardDelegateMapper masterCardDelegateMapper;

    @Test
    void mapper_shouldDelegateCallToDefaultMapper() {
        ISO8583 input = ISO8583.builder().build();
        Map<String, String> subFields = Collections.emptyMap();
        ISO20022 expectedResult = ISO20022.builder().build();

        when(mockDelegate.mapper(input, subFields)).thenReturn(expectedResult);

        ISO20022 actualResult = masterCardDelegateMapper.mapper(input, subFields);

        assertEquals(expectedResult, actualResult);
        verify(mockDelegate, times(1)).mapper(input, subFields);
    }

    @Test
    void mapper_shouldDelegateCallToDefaultMapper_0110() {
        ISO8583 input = ISO8583.builder()
                .messageType("0100")
                .build();
        Map<String, String> subFields = Collections.emptyMap();
        ISO20022 expectedResult = ISO20022.builder().build();

        when(mockDelegate.mapper(input, subFields)).thenReturn(expectedResult);

        ISO20022 actualResult = masterCardDelegateMapper.mapper(input, subFields);

        assertEquals(expectedResult, actualResult);
        verify(mockDelegate, times(1)).mapper(input, subFields);
    }

    @Test
    void unMapper_shouldDelegateCallToDefaultMapper() {
        Map<String, String> expectedResult = Collections.emptyMap();
        ISO20022 iso20022 = ISO20022.builder().build();

        when(mockDelegate.unMapper(iso20022)).thenReturn(expectedResult);

        Map<String, String> actualResult = masterCardDelegateMapper.unMapper(iso20022);

        assertEquals(expectedResult, actualResult);
        verify(mockDelegate, times(1)).unMapper(iso20022);
    }
}