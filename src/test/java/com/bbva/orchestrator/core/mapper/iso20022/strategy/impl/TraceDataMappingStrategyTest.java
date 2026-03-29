package com.bbva.orchestrator.core.mapper.iso20022.strategy.impl;

import com.bbva.gateway.dto.iso20022.TraceDataDTO;
import com.bbva.gateway.interceptors.GrpcHeadersInfo;
import com.bbva.orchestrator.core.exception.MapperFieldsException;
import com.bbva.orchestrator.core.mapper.model.CanonicalFields;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class TraceDataMappingStrategyTest {

    @InjectMocks
    private TraceDataMappingStrategy traceDataMappingStrategy;

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
    void mapper_shouldReturnCorrectTraceDataList_WithBinForeignFalse() {
        CanonicalFields fields = CanonicalFields.of(Map.of(
                "networkData", "network_data_value",
                "header", "header_value"
        ));
        mockedHeaders.when(GrpcHeadersInfo::getTraceId).thenReturn("trace_id_value");

        List<TraceDataDTO> resultList = traceDataMappingStrategy.mapper(fields);

        assertNotNull(resultList);
        assertEquals(3, resultList.size());

        assertTrue(resultList.stream().anyMatch(dto ->
                dto.getKey().equals("posAdditionalData") && dto.getValue().equals("network_data_value")));
        assertTrue(resultList.stream().anyMatch(dto ->
                dto.getKey().equals("header") && dto.getValue().equals("header_value")));
        assertTrue(resultList.stream().anyMatch(dto ->
                dto.getKey().equals("PAYMENT_ID") && dto.getValue().equals("trace_id_value")));
    }

    @Test
    void mapper_shouldReturnBinForeignTrue_WhenContainsBinReturnsTrue() {
        CanonicalFields fields = CanonicalFields.of(Map.of(
                "networkData", "network_data_value",
                "header", "header_value"
        ));
        mockedHeaders.when(GrpcHeadersInfo::getTraceId).thenReturn("trace_id_value");

        List<TraceDataDTO> resultList = traceDataMappingStrategy.mapper(fields);

        assertNotNull(resultList);
        assertEquals(3, resultList.size());
    }

    @Test
    void unMapper_whenListIsEmpty_shouldReturnEmptyMap() {
        List<TraceDataDTO> emptyList = Collections.emptyList();

        Map<String, String> result = traceDataMappingStrategy.unMapper("PEER02", emptyList);

        Assertions.assertNotNull(result);
    }

    @Test
    void unMapper_whenListHasValues_shouldReturnCorrectMap() {
        List<TraceDataDTO> traceList = List.of(
                TraceDataDTO.builder().key("posAdditionalData").value("pos_value").build(),
                TraceDataDTO.builder().key("header").value("header_value").build()
        );

        Map<String, String> result = traceDataMappingStrategy.unMapper("PEER01", traceList);

        assertNotNull(result);
        assertEquals("pos_value", result.get("networkData"));
        assertEquals("header_value", result.get("header"));
    }

    @Test
    void unMapper_whenListIsNull_shouldReturnMapWithNullValues() {
        Map<String, String> result = traceDataMappingStrategy.unMapper("PEER01", null);

        assertNotNull(result);
        assertNull(result.get("networkData"));
        assertNull(result.get("header"));
    }

    @Test
    void mapper_shouldThrowMapperFieldsException_WhenNullInput() {
        // null input causes NPE → MapperFieldsException
        assertThatThrownBy(() -> traceDataMappingStrategy.mapper(null))
                .isInstanceOf(MapperFieldsException.class)
                .extracting("code")
                .isEqualTo("PGWP-00121");
    }

    @Test
    void mapper_shouldSucceedWithValidData() {
        CanonicalFields fields = CanonicalFields.of(Map.of(
                "networkData", "network_data_value",
                "header", "header_value"
        ));
        mockedHeaders.when(GrpcHeadersInfo::getTraceId).thenReturn("trace_id_value");

        List<TraceDataDTO> result = traceDataMappingStrategy.mapper(fields);

        assertNotNull(result);
        assertEquals(3, result.size());
    }
}
