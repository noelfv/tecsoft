package com.bbva.orchestrator.core.mapper.iso20022.strategy.impl;

import com.bbva.gateway.dto.iso20022.SupplementaryDataDTO;
import com.bbva.orchestrator.core.exception.MapperFieldsException;
import com.bbva.orchestrator.core.mapper.model.CanonicalFields;
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

@ExtendWith(MockitoExtension.class)
class SupplementaryDataMappingStrategyTest {

    @Mock
    private MapperUtil fieldService;

    @InjectMocks
    private SupplementaryDataMappingStrategy supplementaryDataMappingStrategy;

    @Test
    void map_shouldOnlyAddFieldsThatAreNotNullOrEmpty() {
        Map<String, String> map = new HashMap<>();
        map.put("networkManagementInformationCode", "001");
        map.put("keyManagement", "some_key_data");
        // settlementData absent → getSettlementData() returns null — not added
        map.put("issuerTraceId", "   "); // blank → not added
        CanonicalFields fields = CanonicalFields.of(map);

        List<SupplementaryDataDTO> resultList = supplementaryDataMappingStrategy.mapper(fields);

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
        List<SupplementaryDataDTO> supplementaryDataDTO = Collections.singletonList(
                Mockito.mock(SupplementaryDataDTO.class, Mockito.RETURNS_DEEP_STUBS));

        Map<String, String> result = supplementaryDataMappingStrategy.unMapper("PEER02", supplementaryDataDTO);

        assertNull(result.get("prueba"));
    }

    @Test
    void shouldThrowMapperFieldsException_WhenRuntimeExceptionOccurs() {
        // null input causes NPE → MapperFieldsException
        assertThatThrownBy(() -> supplementaryDataMappingStrategy.mapper(null))
                .isInstanceOf(MapperFieldsException.class)
                .extracting("code")
                .isEqualTo("PGWP-00121");
    }
}
