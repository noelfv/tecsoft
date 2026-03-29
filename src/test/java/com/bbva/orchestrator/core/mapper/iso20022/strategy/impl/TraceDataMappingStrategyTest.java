package com.bbva.orchestrator.core.mapper.iso20022.strategy.impl;

import com.bbva.gateway.dto.iso20022.TraceDataDTO;
import com.bbva.gateway.interceptors.GrpcHeadersInfo;
import com.bbva.orchestrator.core.dto.ISO8583;
import com.bbva.orchestrator.core.exception.MapperFieldsException;
import com.bbva.orchestrator.core.utils.MapperUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraceDataMappingStrategyTest {

    @Mock
    private ISO8583 mockInput;

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
        // bin_foreign = false → tarjeta dentro de la red
        when(mockInput.getNetworkData()).thenReturn("network_data_value");
        when(mockInput.getHeader()).thenReturn("header_value");
        when(GrpcHeadersInfo.getTraceId()).thenReturn("trace_id_value");

        List<TraceDataDTO> resultList = traceDataMappingStrategy.mapper(mockInput, Collections.emptyMap());

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
        // bin_foreign = true → tarjeta fuera de la red
        when(mockInput.getNetworkData()).thenReturn("network_data_value");
        when(mockInput.getHeader()).thenReturn("header_value");
        when(GrpcHeadersInfo.getTraceId()).thenReturn("trace_id_value");

        List<TraceDataDTO> resultList = traceDataMappingStrategy.mapper(mockInput, Collections.emptyMap());

        assertNotNull(resultList);
        assertEquals(3, resultList.size());
    }

    @Test
    void unMapper_whenListIsEmpty_shouldReturnEmptyMap() {
        // Preparación: Crea una lista vacía.
        List<TraceDataDTO> emptyList = Collections.emptyList();

        // Ejecución
        Map<String, String> result = traceDataMappingStrategy.unMapper("PEER02", emptyList);

        // Verificación
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
    void mapper_shouldThrowMapperFieldsException_WhenRuntimeExceptionOccurs() {
        // GIVEN
        ISO8583 inputMock = mock(ISO8583.class);
        Map<String, String> subFields = new HashMap<>();

        // Simulamos que el input lanza una excepción al acceder a un dato
        when(inputMock.getNetworkData()).thenThrow(new RuntimeException("Error al mapear desde ISO8583"));

        // WHEN & THEN
        assertThatThrownBy(() -> traceDataMappingStrategy.mapper(inputMock, subFields))
                .isInstanceOf(MapperFieldsException.class)
                .hasMessage("java.lang.RuntimeException: Error al mapear desde ISO8583")
                .extracting("code") // Asumiendo que tu excepción tiene un campo 'code'
                .isEqualTo("PGWP-00121");
    }

    @Test
    void mapper_shouldThrowMapperFieldsException_WhenContainsBinThrows() {
        // Arrange
        when(mockInput.getNetworkData()).thenReturn("network_data_value");
        when(mockInput.getHeader()).thenReturn("header_value");
        when(GrpcHeadersInfo.getTraceId()).thenReturn("trace_id_value");

        Map<String, String> subFields = Collections.emptyMap();

        // Act & Assert
        assertThatThrownBy(() -> traceDataMappingStrategy.mapper(mockInput, subFields))
                .isInstanceOf(MapperFieldsException.class)
                .extracting("code")
                .isEqualTo("PGWP-00121");
    }

}