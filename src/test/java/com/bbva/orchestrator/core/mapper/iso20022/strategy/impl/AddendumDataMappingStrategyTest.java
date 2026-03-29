package com.bbva.orchestrator.core.mapper.iso20022.strategy.impl;

import com.bbva.gateway.dto.iso20022.AddendumDataDTO;
import com.bbva.gateway.dto.iso20022.AdditionalDataDTO;
import com.bbva.gateway.dto.iso20022.ISO20022;
import com.bbva.gateway.interceptors.GrpcHeadersInfo;
import com.bbva.orchestrator.core.exception.MapperFieldsException;
import com.bbva.orchestrator.core.mapper.model.CanonicalFields;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddendumDataMappingStrategyTest {

    @Mock
    private MapperUtil mapperUtil;

    @InjectMocks
    private AddendumDataMappingStrategy addendumDataMappingStrategy;

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
    void map_whenContextMessageExists_shouldReturnAllData() {
        String traceId = "test-trace-id";
        CanonicalFields fields = CanonicalFields.of(Map.of(
                "originalMessage", "original_message",
                "messageType", "0100"
        ));
        when(GrpcHeadersInfo.getTraceId()).thenReturn(traceId);

        AddendumDataDTO result = addendumDataMappingStrategy.mapper(fields);

        assertNotNull(result);
    }

    @Test
    void map_whenContextMessageIsNull_shouldReturnDataWithoutContext() {
        CanonicalFields fields = CanonicalFields.of(Map.of(
                "originalMessage", "original_message",
                "messageType", "0100"
        ));
        when(GrpcHeadersInfo.getTraceId()).thenReturn("any_id");

        AddendumDataDTO result = addendumDataMappingStrategy.mapper(fields);

        assertNotNull(result);
        assertEquals("0100", result.getAdditionalData().get(0).getValue());
        assertEquals("original_message", result.getAdditionalData().get(1).getValue());
    }

    @Test
    void unMapper_whenAddendumDataMappingStrategy_shouldSetAllDtoInBuilder() {
        Map<String, String> actual = Map.of();
        Map<String, String> result = addendumDataMappingStrategy.unMapper("PEER02", ISO20022.builder().build().getAddendumData());

        Assertions.assertNotNull(result);
        Assertions.assertEquals(actual, result);
    }

    @Test
    void map_whenContextMessageIsNull_shouldReturnDataResponse() {
        CanonicalFields fields = CanonicalFields.of(Map.of(
                "originalMessage", "original_message",
                "messageType", "0110"
        ));
        when(GrpcHeadersInfo.getTraceId()).thenReturn("any_id");

        AddendumDataDTO result = addendumDataMappingStrategy.mapperResponse(fields);

        assertNotNull(result);
        assertEquals("0110", result.getAdditionalData().get(2).getValue());
        assertEquals("original_message", result.getAdditionalData().get(0).getValue());
    }

    @Test
    void shouldThrowMapperFieldsException_WhenRuntimeExceptionOccurs() {
        // GIVEN — force exception via mapperUtil
        when(mapperUtil.getBinDescription(any(), any()))
                .thenThrow(new RuntimeException("Error al mapear AddendumDTO desde ISO8583"));

        CanonicalFields fields = CanonicalFields.of(Map.of("messageType", "0100", "originalMessage", "msg"));

        // WHEN & THEN
        assertThatThrownBy(() -> addendumDataMappingStrategy.mapper(fields))
                .isInstanceOf(MapperFieldsException.class)
                .hasMessage("java.lang.RuntimeException: Error al mapear AddendumDTO desde ISO8583")
                .extracting("code")
                .isEqualTo("PGWP-00121");
    }

    @Test
    void mapper_WhenRejectFlagIs0000_UNSPIs0000() {
        // Arrange
        CanonicalFields input = CanonicalFields.of(Map.of(
                "rejectFlag", "0000",
                "messageType", "0800",
                "originalMessage", "MensajeOriginalHost"
        ));

        // Act
        AddendumDataDTO result = addendumDataMappingStrategy.mapper(input);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getAdditionalData().size());

        String unspValue = getAdditionalDataValue(result.getAdditionalData(), "UNSP");
        String hostValue = getAdditionalDataValue(result.getAdditionalData(), "ISO8583_HOST");

        assertEquals("0000", unspValue);
        assertEquals("MensajeOriginalHost", hostValue);
    }

    @Test
    void mapper_WhenRejectFlagIsNot0000_UNSPIsMessageType() {
        // Arrange
        CanonicalFields input = CanonicalFields.of(Map.of(
                "rejectFlag", "9999",
                "messageType", "0200",
                "originalMessage", "MensajeOriginalHost"
        ));

        // Act
        AddendumDataDTO result = addendumDataMappingStrategy.mapper(input);

        // Assert
        assertNotNull(result);
        String unspValue = getAdditionalDataValue(result.getAdditionalData(), "UNSP");
        assertEquals("0200", unspValue);
    }

    @Test
    void mapper_WhenRejectFlagIsNull_UNSPIsMessageType() {
        // Arrange — rejectFlag absent from map → getRejectFlag() returns null
        Map<String, String> map = new HashMap<>();
        map.put("messageType", "0400");
        map.put("originalMessage", "MensajeOriginalHost");
        CanonicalFields input = CanonicalFields.of(map);

        // Act
        AddendumDataDTO result = addendumDataMappingStrategy.mapper(input);

        // Assert
        assertNotNull(result);
        String unspValue = getAdditionalDataValue(result.getAdditionalData(), "UNSP");
        assertEquals("0400", unspValue);
    }

    @Test
    void mapper_WhenNullInput_ThrowsMapperFieldsException() {
        // Act & Assert
        MapperFieldsException exception = assertThrows(MapperFieldsException.class, () -> {
            addendumDataMappingStrategy.mapper(null);
        });

        assertEquals("PGWP-00121", exception.getCode());
        assertEquals("Error al mapear AddendumDataDTO desde ISO8583", exception.getDescription());
    }

    // ==========================================
    // TESTS PARA EL MÉTODO: mapperResponse()
    // ==========================================

    @Test
    void mapperResponse_WhenRejectFlagIs0000_UNSPIs0000() {
        // Arrange
        CanonicalFields input = CanonicalFields.of(Map.of(
                "rejectFlag", "0000",
                "messageType", "0210",
                "originalMessage", "MensajeOriginalRespuesta",
                "plainTextPCI", "PlainTextPCIValue"
        ));

        // Act
        AddendumDataDTO result = addendumDataMappingStrategy.mapperResponse(input);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getAdditionalData().size());

        String unspValue = getAdditionalDataValue(result.getAdditionalData(), "UNSP");
        String iso8583Value = getAdditionalDataValue(result.getAdditionalData(), "ISO8583");

        assertEquals("0000", unspValue);
        assertEquals("PlainTextPCIValue", iso8583Value);
    }

    @Test
    void mapperResponse_WhenRejectFlagIsNot0000_UNSPIsMessageType() {
        // Arrange
        CanonicalFields input = CanonicalFields.of(Map.of(
                "rejectFlag", "1111",
                "messageType", "0410",
                "originalMessage", "MensajeOriginalRespuesta",
                "plainTextPCI", "PlainTextPCIValue"
        ));

        // Act
        AddendumDataDTO result = addendumDataMappingStrategy.mapperResponse(input);

        // Assert
        assertNotNull(result);
        String unspValue = getAdditionalDataValue(result.getAdditionalData(), "UNSP");
        assertEquals("0410", unspValue);
    }

    // ==========================================
    // MÉTODO AUXILIAR
    // ==========================================

    private String getAdditionalDataValue(List<AdditionalDataDTO> list, String key) {
        return list.stream()
                .filter(data -> key.equals(data.getKey()))
                .map(AdditionalDataDTO::getValue)
                .findFirst()
                .orElse(null);
    }
}
