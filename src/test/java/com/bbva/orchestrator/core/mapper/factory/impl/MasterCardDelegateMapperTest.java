package com.bbva.orchestrator.core.mapper.factory.impl;

import com.bbva.gateway.dto.iso20022.ISO20022;
import com.bbva.orchestrator.core.mapper.model.CanonicalFields;
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
        CanonicalFields input = CanonicalFields.of(Collections.emptyMap());
        ISO20022 expectedResult = ISO20022.builder().build();

        when(mockDelegate.mapper(input)).thenReturn(expectedResult);

        ISO20022 actualResult = masterCardDelegateMapper.mapper(input);

        assertEquals(expectedResult, actualResult);
        verify(mockDelegate, times(1)).mapper(input);
    }

    @Test
    void mapper_shouldDelegateCallToDefaultMapper_0110() {
        CanonicalFields input = CanonicalFields.of(Map.of("messageType", "0100"));
        ISO20022 expectedResult = ISO20022.builder().build();

        when(mockDelegate.mapper(input)).thenReturn(expectedResult);

        ISO20022 actualResult = masterCardDelegateMapper.mapper(input);

        assertEquals(expectedResult, actualResult);
        verify(mockDelegate, times(1)).mapper(input);
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
