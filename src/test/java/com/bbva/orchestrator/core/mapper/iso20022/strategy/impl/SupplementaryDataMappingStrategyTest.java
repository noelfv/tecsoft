package com.bbva.orchestrator.core.mapper.iso20022.strategy.impl;

import com.bbva.gateway.dto.iso20022.SupplementaryDataDTO;
import com.bbva.orchestrator.core.dto.ISO8583;
import com.bbva.orchestrator.core.exception.MapperFieldsException;
import com.bbva.orchestrator.core.utils.MapperUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplementaryDataMappingStrategyTest {

    @Mock
    private MapperUtil fieldService;
    @Mock
    private ISO8583 mockInput;

    @InjectMocks
    private SupplementaryDataMappingStrategy supplementaryDataMappingStrategy;

    @Test
    void map_shouldOnlyAddFieldsThatAreNotNullOrEmpty() {
        when(mockInput.getNetworkManagementInformationCode()).thenReturn("001");
        when(mockInput.getKeyManagement()).thenReturn("some_key_data");
        when(mockInput.getSettlementData()).thenReturn(null); // Este no se debe añadir
        when(mockInput.getIssuerTraceId()).thenReturn("   "); // Este tampoco se debe añadir

        List<SupplementaryDataDTO> resultList = supplementaryDataMappingStrategy.mapper(mockInput, Collections.emptyMap());

        assertNotNull(resultList);
        assertEquals(2, resultList.size());

        SupplementaryDataDTO firstElement = resultList.get(0);
        assertEquals("networkManagementInfoCode", firstElement.getPlaceAndName());
        assertEquals("001", firstElement.getEnvelope());

        SupplementaryDataDTO secondElement = resultList.get(1);
        assertEquals("keyManagement", secondElement.getPlaceAndName());
        assertEquals("some_key_data", secondElement.getEnvelope());
    }

    @Test
    void unMapper_simplestTest() {

        List<SupplementaryDataDTO> supplementaryDataDTO = Collections.singletonList(Mockito.mock(SupplementaryDataDTO.class, Mockito.RETURNS_DEEP_STUBS));

        Map<String, String> result = supplementaryDataMappingStrategy.unMapper("PEER02",supplementaryDataDTO);

        assertNull(result.get("prueba"));
    }

    @Test
    void shouldThrowMapperFieldsException_WhenRuntimeExceptionOccurs() {
        // GIVEN
        ISO8583 inputMock = mock(ISO8583.class);
        Map<String, String> subFields = new HashMap<>();

        // Simulamos que el input lanza una excepción al acceder a un dato
        when(inputMock.getNetworkManagementInformationCode()).thenThrow(new RuntimeException("Error al mapear desde ISO8583"));

        // WHEN & THEN
        assertThatThrownBy(() -> supplementaryDataMappingStrategy.mapper(inputMock, subFields))
                .isInstanceOf(MapperFieldsException.class)
                .hasMessage("java.lang.RuntimeException: Error al mapear desde ISO8583")
                .extracting("code") // Asumiendo que tu excepción tiene un campo 'code'
                .isEqualTo("PGWP-00121");
    }
}