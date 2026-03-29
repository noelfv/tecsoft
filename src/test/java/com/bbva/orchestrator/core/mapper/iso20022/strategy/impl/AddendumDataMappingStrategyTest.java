package com.bbva.orchestrator.core.mapper.iso20022.strategy.impl;

import com.bbva.gateway.dto.iso20022.AddendumDataDTO;
import com.bbva.gateway.dto.iso20022.AdditionalDataDTO;
import com.bbva.gateway.dto.iso20022.ISO20022;
import com.bbva.gateway.interceptors.GrpcHeadersInfo;
import com.bbva.orchestrator.core.dto.ISO8583;
import com.bbva.orchestrator.core.exception.MapperFieldsException;
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
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddendumDataMappingStrategyTest {

    @Mock
    private ISO8583 mockInput;

    @InjectMocks
    private AddendumDataMappingStrategy addendumDataMappingStrategy;

    private MockedStatic<GrpcHeadersInfo> mockedHeaders;

    private Map<String, String> subFields;

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
        String contextMessage = "context_iso_message";
        when(mockInput.getOriginalMessage()).thenReturn("original_message");
        when(mockInput.getMessageType()).thenReturn("0100");
        when(GrpcHeadersInfo.getTraceId()).thenReturn(traceId);
        //when(mockIso8583ContextService.getISO8583(traceId)).thenReturn(contextMessage);

        AddendumDataDTO result = addendumDataMappingStrategy.mapper(mockInput, Collections.emptyMap());

        assertNotNull(result);
    }

    @Test
    void map_whenContextMessageIsNull_shouldReturnDataWithoutContext() {
        when(mockInput.getOriginalMessage()).thenReturn("original_message");
        when(mockInput.getMessageType()).thenReturn("0100");
        when(GrpcHeadersInfo.getTraceId()).thenReturn("any_id");
        //when(mockIso8583ContextService.getISO8583(anyString())).thenReturn(null);

        AddendumDataDTO result = addendumDataMappingStrategy.mapper(mockInput, Collections.emptyMap());

        assertNotNull(result);
        assertEquals("0100", result.getAdditionalData().get(0).getValue());
        assertEquals("original_message", result.getAdditionalData().get(1).getValue());
    }

    @Test
    void unMapper_whenAddendumDataMappingStrategy_shouldSetAllDtoInBuilder() {

        Map<String, String> actual=Map.of();
        Map<String, String> result = addendumDataMappingStrategy.unMapper("PEER02",ISO20022.builder().build().getAddendumData());

        Assertions.assertNotNull(result);
        Assertions.assertEquals(actual, result);

    }

    @Test
    void map_whenContextMessageIsNull_shouldReturnDataResponse() {
        when(mockInput.getOriginalMessage()).thenReturn("original_message");
        when(mockInput.getMessageType()).thenReturn("0110");
        when(GrpcHeadersInfo.getTraceId()).thenReturn("any_id");
        //when(mockIso8583ContextService.getISO8583(anyString())).thenReturn(null);

        AddendumDataDTO result = addendumDataMappingStrategy.mapperResponse(mockInput);

        assertNotNull(result);
        assertEquals("0110", result.getAdditionalData().get(2).getValue());
        assertEquals("original_message", result.getAdditionalData().get(0).getValue());
    }

    @Test
    void shouldThrowMapperFieldsException_WhenRuntimeExceptionOccurs() {
        // GIVEN
        ISO8583 inputMock = mock(ISO8583.class);

        // Simulamos que el input lanza una excepción al acceder a un dato
        when(inputMock.getMessageType()).thenThrow(new RuntimeException("Error al mapear AddendumDTO desde ISO8583"));

        // WHEN & THEN
        assertThatThrownBy(() -> addendumDataMappingStrategy.mapper(inputMock, subFields))
                .isInstanceOf(MapperFieldsException.class)
                .hasMessage("java.lang.RuntimeException: Error al mapear AddendumDTO desde ISO8583")
                .extracting("code") // Asumiendo que tu excepción tiene un campo 'code'
                .isEqualTo("PGWP-00121");
    }

    @Test
    void mapper_WhenRejectFlagIs0000_UNSPIs0000() {
        // Arrange
        ISO8583 input = ISO8583.builder()
                .rejectFlag("0000")
                .messageType("0800")
                .originalMessage("MensajeOriginalHost")
                .build();

        // Act
        AddendumDataDTO result = addendumDataMappingStrategy.mapper(input, subFields);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getAdditionalData().size());

        String unspValue = getAdditionalDataValue(result.getAdditionalData(), "UNSP");
        String hostValue = getAdditionalDataValue(result.getAdditionalData(), "ISO8583_HOST");

        // Verificamos que al ser "0000", tomó el valor del rejectFlag
        assertEquals("0000", unspValue);
        assertEquals("MensajeOriginalHost", hostValue);
    }

    @Test
    void mapper_WhenRejectFlagIsNot0000_UNSPIsMessageType() {
        // Arrange
        ISO8583 input = ISO8583.builder()
                .rejectFlag("9999") // Diferente de 0000
                .messageType("0200") // Debe tomar este valor
                .originalMessage("MensajeOriginalHost")
                .build();

        // Act
        AddendumDataDTO result = addendumDataMappingStrategy.mapper(input, subFields);

        // Assert
        assertNotNull(result);
        String unspValue = getAdditionalDataValue(result.getAdditionalData(), "UNSP");

        // Verificamos que al NO ser "0000", tomó el valor del messageType
        assertEquals("0200", unspValue);
    }

    @Test
    void mapper_WhenRejectFlagIsNull_UNSPIsMessageType() {
        // Arrange
        ISO8583 input = ISO8583.builder()
                .rejectFlag(null) // Un null es una posibilidad real
                .messageType("0400")
                .originalMessage("MensajeOriginalHost")
                .build();

        // Act
        AddendumDataDTO result = addendumDataMappingStrategy.mapper(input, subFields);

        // Assert
        assertNotNull(result);
        String unspValue = getAdditionalDataValue(result.getAdditionalData(), "UNSP");

        // Verificamos que al ser nulo (no es "0000"), evalúa seguro y toma el messageType
        assertEquals("0400", unspValue);
    }

    @Test
    void mapper_WhenNullInput_ThrowsMapperFieldsException() {
        // Arrange
        // Pasamos un null deliberadamente para forzar un RuntimeException (NullPointerException)

        // Act & Assert
        MapperFieldsException exception = assertThrows(MapperFieldsException.class, () -> {
            addendumDataMappingStrategy.mapper(null, subFields);
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
        ISO8583 input = ISO8583.builder()
                .rejectFlag("0000")
                .messageType("0210")
                .originalMessage("MensajeOriginalRespuesta")
                .plainTextPCI("PlainTextPCIValue")
                .build();

        // Act
        AddendumDataDTO result = addendumDataMappingStrategy.mapperResponse(input);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getAdditionalData().size()); // Este método retorna 3 elementos

        String unspValue = getAdditionalDataValue(result.getAdditionalData(), "UNSP");
        String iso8583Value = getAdditionalDataValue(result.getAdditionalData(), "ISO8583");

        assertEquals("0000", unspValue);
        assertEquals("PlainTextPCIValue", iso8583Value);
    }

    @Test
    void mapperResponse_WhenRejectFlagIsNot0000_UNSPIsMessageType() {
        // Arrange
        ISO8583 input = ISO8583.builder()
                .rejectFlag("1111")
                .messageType("0410") // Debe tomar este valor
                .originalMessage("MensajeOriginalRespuesta")
                .plainTextPCI("PlainTextPCIValue")
                .build();

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

    /**
     * Extrae el valor de la lista de AdditionalDataDTO basado en su key.
     */
    private String getAdditionalDataValue(List<AdditionalDataDTO> list, String key) {
        return list.stream()
                .filter(data -> key.equals(data.getKey()))
                .map(AdditionalDataDTO::getValue)
                .findFirst()
                .orElse(null);
    }
}