package com.bbva.orchestrator.core.mapper.iso20022.strategy.impl;

import com.bbva.gateway.dto.iso20022.AdditionalInformationDTO;
import com.bbva.gateway.dto.iso20022.ProcessingResultDTO;
import com.bbva.gateway.dto.iso20022.ResultDataDTO;
import com.bbva.orchestrator.core.dto.ISO8583;
import com.bbva.orchestrator.core.exception.MapperFieldsException;
import com.bbva.orchestrator.core.utils.MapperUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessingResultMappingStrategyTest {

    @Mock
    private MapperUtil mapperUtil;

    @Mock
    private ISO8583 mockInput;

    @InjectMocks
    private ProcessingResultMappingStrategy strategy;

    private static final String NETWORK_NAME = "testNetwork";


    @Test
    @DisplayName("mapper() should return null when input MTI is not an output MTI")
    void mapper_whenMessageTypeIsNotOutputMti_shouldReturnNull() {
        when(mockInput.getMessageType()).thenReturn("0100");
        when(mapperUtil.isOutputMti("0100")).thenReturn(false);

        ProcessingResultDTO result = strategy.mapper(mockInput, Map.of());

        assertNull(result);
    }

    @Test
    @DisplayName("mapper() should map resultData fields for output MTI")
    void mapper_whenMessageTypeIsOutputMti_shouldMapResultData() {
        when(mockInput.getMessageType()).thenReturn("0110");
        when(mockInput.getNetworkName()).thenReturn("testNetwork");
        when(mockInput.getResponseCode()).thenReturn("00");

        when(mapperUtil.isOutputMti("0110")).thenReturn(true);
        when(mapperUtil.convertResponseCodeToLabelData("testNetwork", "00")).thenReturn("APPR");
        when(mapperUtil.getAdditionalInfoValue("00")).thenReturn("someAdditionalValue");

        ProcessingResultDTO result = strategy.mapper(mockInput, Map.of());

        assertNotNull(result);
        assertNotNull(result.getResultData());
        assertEquals("APPR", result.getResultData().getResult());
        assertEquals("APPR", result.getResultData().getOtherResult());
        assertEquals("00", result.getResultData().getOtherResultDetails());
    }

    @Test
    @DisplayName("mapper() should map approvalCode from authorizationIdentificationResponse")
    void mapper_whenMessageTypeIsOutputMti_shouldMapApprovalCode() {
        when(mockInput.getMessageType()).thenReturn("0110");
        when(mockInput.getNetworkName()).thenReturn("testNetwork");
        when(mockInput.getResponseCode()).thenReturn("00");
        when(mockInput.getAuthorizationIdentificationResponse()).thenReturn("ABC123");

        when(mapperUtil.isOutputMti("0110")).thenReturn(true);
        when(mapperUtil.convertResponseCodeToLabelData("testNetwork", "00")).thenReturn("APPR");
        when(mapperUtil.getAdditionalInfoValue("00")).thenReturn("v");

        ProcessingResultDTO result = strategy.mapper(mockInput, Map.of());

        assertNotNull(result);
        assertEquals("ABC123", result.getApprovalCode());
    }

    @Test
    @DisplayName("mapper() should include additionalInformation with key transaction and mapped value")
    void mapper_whenMessageTypeIsOutputMti_shouldMapAdditionalInformationTransaction() {
        when(mockInput.getMessageType()).thenReturn("0110");
        when(mockInput.getNetworkName()).thenReturn("testNetwork");
        when(mockInput.getResponseCode()).thenReturn("00");

        when(mapperUtil.isOutputMti("0110")).thenReturn(true);
        when(mapperUtil.convertResponseCodeToLabelData("testNetwork", "00")).thenReturn("APPR");
        when(mapperUtil.getAdditionalInfoValue("00")).thenReturn("additional");

        ProcessingResultDTO result = strategy.mapper(mockInput, Map.of());

        assertNotNull(result);
        assertNotNull(result.getAdditionalInformation());
        assertEquals(1, result.getAdditionalInformation().size());
        assertEquals("transaction", result.getAdditionalInformation().get(0).getKey());
        assertEquals("additional", result.getAdditionalInformation().get(0).getValue());
    }

    @Test
    @DisplayName("mapper() should allow null responseCode and still build DTO values accordingly")
    void mapper_whenResponseCodeIsNull_shouldStillBuildProcessingResult() {
        when(mockInput.getMessageType()).thenReturn("0110");
        when(mockInput.getNetworkName()).thenReturn("testNetwork");
        when(mockInput.getResponseCode()).thenReturn(null);

        when(mapperUtil.isOutputMti("0110")).thenReturn(true);
        when(mapperUtil.convertResponseCodeToLabelData("testNetwork", null)).thenReturn(null);
        when(mapperUtil.getAdditionalInfoValue(null)).thenReturn(null);

        ProcessingResultDTO result = strategy.mapper(mockInput, Map.of());

        assertNotNull(result);
        assertNotNull(result.getResultData());
        assertEquals("APPR", result.getResultData().getResult());
        assertNull(result.getResultData().getOtherResult());
        assertNull(result.getResultData().getOtherResultDetails());
        assertNotNull(result.getAdditionalInformation());
        assertEquals(1, result.getAdditionalInformation().size());
        assertEquals("transaction", result.getAdditionalInformation().get(0).getKey());
        assertNull(result.getAdditionalInformation().get(0).getValue());
    }

    @Test
    @DisplayName("mapper() should allow null networkName and delegate conversion with null network")
    void mapper_whenNetworkNameIsNull_shouldDelegateConversionWithNullNetworkName() {
        when(mockInput.getMessageType()).thenReturn("0110");
        when(mockInput.getNetworkName()).thenReturn(null);
        when(mockInput.getResponseCode()).thenReturn("00");

        when(mapperUtil.isOutputMti("0110")).thenReturn(true);
        when(mapperUtil.convertResponseCodeToLabelData(null, "00")).thenReturn("APPR");
        when(mapperUtil.getAdditionalInfoValue("00")).thenReturn("x");

        ProcessingResultDTO result = strategy.mapper(mockInput, Map.of());

        assertNotNull(result);
        assertNotNull(result.getResultData());
        assertEquals("APPR", result.getResultData().getOtherResult());
    }

    @Test
    @DisplayName("unMapper() should return empty map when input is null")
    void unMapper_whenInputIsNull_shouldReturnEmptyMap() {
        // When
        Map<String, String> result = strategy.unMapper(NETWORK_NAME, null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("unMapper() should map approval code when response code is '00'")
    void unMapper_whenResultIsApproved_shouldMapApprovalCode() {
        // Given
        ResultDataDTO resultData = ResultDataDTO.builder().build();
        resultData.setResult("APPR");

        ProcessingResultDTO input = ProcessingResultDTO.builder().build();
        input.setResultData(resultData);
        input.setApprovalCode("123456");

        // Mocking the util method
        when(mapperUtil.convertResponseCodeToLabelData(NETWORK_NAME, "APPR")).thenReturn("00");

        // When
        Map<String, String> result = strategy.unMapper(NETWORK_NAME, input);

        // Then
        assertNotNull(result);
        assertEquals(6, result.size()); // responde_code, authorizationIdentificationResponse, and 48.87
        assertEquals("00", result.get("responseCode"));
        assertEquals("123456", result.get("authorizationIdentificationResponse"));
    }

    @Test
    void unMapper_shouldMapField4887AsNull_NolabelCode() {
        String label = "label_not_found";
        // Given
        ResultDataDTO resultData = ResultDataDTO.builder().build();
        resultData.setOtherResult(label);
        resultData.setResultDetails("Error: Invalid responseCode: " + label);

        AdditionalInformationDTO otherInfo = AdditionalInformationDTO.builder().build();
        otherInfo.setKey("some_other_key");
        otherInfo.setValue("some_value");

        ProcessingResultDTO input = ProcessingResultDTO.builder().build();
        input.setResultData(resultData);
        input.setAdditionalInformation(List.of(otherInfo)); // List does not contain the target key

        when(mapperUtil.convertResponseCodeToLabelData(anyString(), anyString())).thenReturn(null);

        // Verificamos que se lanza la excepción RuntimeException
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            // Ejecuta el método que debe fallar
            strategy.unMapper(NETWORK_NAME, input);
        });

        // Opcional: Verifica que el mensaje de la excepción es el esperado
        String expectedMessage = "Error: Invalid responseCode: " + label;
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void unMapper_shouldMapField4887AsNull_NolabelCodeEmpty() {
        String label = "label_not_found";
        // Given
        ResultDataDTO resultData = ResultDataDTO.builder().build();
        resultData.setOtherResult(label);
        resultData.setResultDetails("Error: Invalid responseCode: " + label);

        AdditionalInformationDTO otherInfo = AdditionalInformationDTO.builder().build();
        otherInfo.setKey("some_other_key");
        otherInfo.setValue("some_value");

        ProcessingResultDTO input = ProcessingResultDTO.builder().build();
        input.setResultData(resultData);
        input.setAdditionalInformation(List.of(otherInfo)); // List does not contain the target key

        when(mapperUtil.convertResponseCodeToLabelData(anyString(), anyString())).thenReturn("");

        // Verificamos que se lanza la excepción RuntimeException
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            // Ejecuta el método que debe fallar
            strategy.unMapper(NETWORK_NAME, input);
        });

        // Opcional: Verifica que el mensaje de la excepción es el esperado
        String expectedMessage = "Error: Invalid responseCode: " + label;
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("unMapper() should NOT map approval code when response code is not '00'")
    void unMapper_whenResultIsDeclined_shouldNotMapApprovalCode() {
        // Given
        ResultDataDTO resultData =  ResultDataDTO.builder().build();
        resultData.setOtherResult("DECL");

        ProcessingResultDTO input = ProcessingResultDTO.builder().build();
        input.setResultData(resultData);

        when(mapperUtil.convertResponseCodeToLabelData(NETWORK_NAME, "DECL")).thenReturn("05");

        // When
        Map<String, String> result = strategy.unMapper(NETWORK_NAME, input);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size()); // responde_code and 48.87
        assertEquals("05", result.get("responseCode"));
        assertNull(result.get("authorizationIdentificationResponse"));
    }

    @Test
    @DisplayName("unMapper() should map field 48.87 when CVV result is present in additional information")
    void unMapper_whenCvvResultIsPresent_shouldMapField4887() {
        // Given
        ResultDataDTO resultData =  ResultDataDTO.builder().build();
        resultData.setResult("APPR");

        AdditionalInformationDTO cvvInfo = AdditionalInformationDTO.builder().build();
        cvvInfo.setKey("cvv_validation_result");
        cvvInfo.setValue("M");

        ProcessingResultDTO input = ProcessingResultDTO.builder().build();
        input.setResultData(resultData);
        input.setAdditionalInformation(List.of(cvvInfo));
        input.setApprovalCode("BWZEGF");

        when(mapperUtil.convertResponseCodeToLabelData(anyString(), anyString())).thenReturn("51");

        // When
        Map<String, String> result = strategy.unMapper(NETWORK_NAME, input);

        // Then
        assertNotNull(result);
        assertEquals("M", result.get("48.87"));
        assertEquals("51", result.get("responseCode"));
    }

    @Test
    @DisplayName("unMapper() should map field 48.87 as null when CVV result is NOT present")
    void unMapper_whenCvvResultIsNotPresent_shouldMapField4887AsNull() {
        // Given
        ResultDataDTO resultData = ResultDataDTO.builder().build();
        resultData.setOtherResult("APPR");

        AdditionalInformationDTO otherInfo = AdditionalInformationDTO.builder().build();
        otherInfo.setKey("some_other_key");
        otherInfo.setValue("some_value");

        ProcessingResultDTO input = ProcessingResultDTO.builder().build();
        input.setResultData(resultData);
        input.setAdditionalInformation(List.of(otherInfo)); // List does not contain the target key

        when(mapperUtil.convertResponseCodeToLabelData(anyString(), anyString())).thenReturn("00");

        // When
        Map<String, String> result = strategy.unMapper(NETWORK_NAME, input);

        // Then
        assertNotNull(result);
        assertNull(result.get("48.87"));
    }

    @Test
    @DisplayName("unMapper() should map field 48.87 as null when additional information list is null")
    void unMapper_whenAdditionalInformationIsNull_shouldMapField4887AsNull() {
        // Given
        ResultDataDTO resultData = ResultDataDTO.builder().build();
        resultData.setOtherResult("APPR");

        ProcessingResultDTO input = ProcessingResultDTO.builder().build();
        input.setResultData(resultData);
        input.setAdditionalInformation(null); // The list itself is null

        when(mapperUtil.convertResponseCodeToLabelData(anyString(), anyString())).thenReturn("00");

        // When
        Map<String, String> result = strategy.unMapper(NETWORK_NAME, input);

        // Then
        assertNotNull(result);
        assertNull(result.get("48.87"));
    }

    @Test
    void shouldThrowMapperFieldsException_WhenMappingFailsInsideTryBlock() {
        // GIVEN
        ISO8583 inputMock = mock(ISO8583.class);
        Map<String, String> subFields = new HashMap<>();
        // 1. Configuramos los mocks para que PASEN el primer filtro 'if'
        // El input no es null y el tipo de mensaje es válido para procesar
        when(inputMock.getMessageType()).thenReturn("0210");
        when(mapperUtil.isOutputMti("0210")).thenReturn(true);

        // 2. Simulamos datos necesarios para llegar a la línea que fallará
        when(inputMock.getNetworkName()).thenReturn("VISA");
        when(inputMock.getResponseCode()).thenReturn("00");

        // 3. FORZAMOS EL ERROR: Hacemos que 'mapperUtil' falle al intentar convertir el código
        // Esto simula un error en la lógica de negocio interna o un NullPointerException inesperado
        when(mapperUtil.convertResponseCodeToLabelData(anyString(), anyString()))
                .thenThrow(new RuntimeException("Error simulado en dependencia utilitaria"));

        // WHEN & THEN
        assertThatThrownBy(() -> strategy.mapper(inputMock, subFields))
                .isInstanceOf(MapperFieldsException.class)
                .hasMessage("java.lang.RuntimeException: Error simulado en dependencia utilitaria")
                .satisfies(exception -> {
                    MapperFieldsException customEx = (MapperFieldsException) exception;

                    // Verificamos el código de error PGWP-00121
                    assertThat(customEx.getCode()).isEqualTo("PGWP-00121");

                    // Verificamos que la causa original se preservó
                    assertThat(customEx.getCause())
                            .isInstanceOf(RuntimeException.class)
                            .hasMessage("Error simulado en dependencia utilitaria");
                });
    }
}