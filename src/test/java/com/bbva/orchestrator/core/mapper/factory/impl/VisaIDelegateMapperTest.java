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
class VisaIDelegateMapperTest {

    @Mock
    private DefaultDelegateMapper mockDelegate;

    @InjectMocks
    private VisaDelegateMapper visaDelegateMapper;

    @Test
    void mapper_shouldDelegateCallToDefaultMapper() {
        // Arrange
        CanonicalFields input = CanonicalFields.of(Collections.emptyMap());
        ISO20022 expectedResult = ISO20022.builder().build();
        when(mockDelegate.mapper(input)).thenReturn(expectedResult);
        // Act
        ISO20022 actualResult = visaDelegateMapper.mapper(input);
        // Assert
        assertEquals(expectedResult, actualResult);
        verify(mockDelegate, times(1)).mapper(input);
    }

    @Test
    void unMapper_shouldDelegateCallToDefaultMapper() {
        // Arrange
        Map<String, String> expectedResult = Collections.emptyMap();
        ISO20022 iso20022 = ISO20022.builder().build();
        when(mockDelegate.unMapper(iso20022)).thenReturn(expectedResult);
        // Act
        Map<String, String> actualResult = visaDelegateMapper.unMapper(iso20022);
        // Assert
        assertEquals(expectedResult, actualResult);
        verify(mockDelegate, times(1)).unMapper(iso20022);
    }
}
