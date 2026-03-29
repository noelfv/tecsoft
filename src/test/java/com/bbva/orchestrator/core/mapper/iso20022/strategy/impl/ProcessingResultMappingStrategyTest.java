package com.bbva.orchestrator.core.mapper.iso20022.strategy.impl;

import com.bbva.gateway.dto.iso20022.AdditionalInformationDTO;
import com.bbva.gateway.dto.iso20022.ProcessingResultDTO;
import com.bbva.gateway.dto.iso20022.ResultDataDTO;
import com.bbva.orchestrator.core.exception.MapperFieldsException;
import com.bbva.orchestrator.core.mapper.model.CanonicalFields;
import com.bbva.orchestrator.core.utils.MapperUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessingResultMappingStrategyTest {

    @Mock
    private MapperUtil mapperUtil;

    @InjectMocks
    private ProcessingResultMappingStrategy strategy;

    private static final String NETWORK_NAME = "testNetwork";


    @Test
    @DisplayName("mapper() should return null when input MTI is not an output MTI")
    void mapper_whenMessageTypeIsNotOutputMti_shouldReturnNull() {
        when(mapperUtil.isOutputMti("0100")).thenReturn(false);

        CanonicalFields fields = CanonicalFields.of(Map.of("messageType", "0100"));
        ProcessingResultDTO result = strategy.mapper(fields);

        assertNull(result);
    }

    @Test
    @DisplayName("mapper() should map resultData fields for output MTI")
    void mapper_whenMessageTypeIsOutputMti_shouldMapResultData() {
        when(mapperUtil.isOutputMti("0110")).thenReturn(true);
        when(mapperUtil.convertResponseCodeToLabelData("testNetwork", "00")).thenReturn("APPR");
        when(mapperUtil.getAdditionalInfoValue("00")).thenReturn("someAdditionalValue");

        CanonicalFields fields = CanonicalFields.of(Map.of(
                "messageType", "0110",
                "networkName", "testNetwork",
                "responseCode", "00"
        ));
        ProcessingResultDTO result = strategy.mapper(fields);

        assertNotNull(result);
        assertNotNull(result.getResultData());
        assertEquals("APPR", result.getResultData().getResult());
        assertEquals("APPR", result.getResultData().getOtherResult());
        assertEquals("00", result.getResultData().getOtherResultDetails());
    }

    @Test
    @DisplayName("mapper() should map approvalCode from authorizationIdentificationResponse")
    void mapper_whenMessageTypeIsOutputMti_shouldMapApprovalCode() {
        when(mapperUtil.isOutputMti("0110")).thenReturn(true);
        when(mapperUtil.convertResponseCodeToLabelData("testNetwork", "00")).thenReturn("APPR");
        when(mapperUtil.getAdditionalInfoValue("00")).thenReturn("v");

        CanonicalFields fields = CanonicalFields.of(Map.of(
                "messageType", "0110",
                "networkName", "testNetwork",
                "responseCode", "00",
                "authorizationIdentificationResponse", "ABC123"
        ));
        ProcessingResultDTO result = strategy.mapper(fields);

        assertNotNull(result);
        assertEquals("ABC123", result.getApprovalCode());
    }

    @Test
    @DisplayName("mapper() should include additionalInformation with key transaction and mapped value")
    void mapper_whenMessageTypeIsOutputMti_shouldMapAdditionalInformationTransaction() {
        when(mapperUtil.isOutputMti("0110")).thenReturn(true);
        when(mapperUtil.convertResponseCodeToLabelData("testNetwork", "00")).thenReturn("APPR");
        when(mapperUtil.getAdditionalInfoValue("00")).thenReturn("additional");

        CanonicalFields fields = CanonicalFields.of(Map.of(
                "messageType", "0110",
                "networkName", "testNetwork",
                "responseCode", "00"
        ));
        ProcessingResultDTO result = strategy.mapper(fields);

        assertNotNull(result);
        assertNotNull(result.getAdditionalInformation());
        assertEquals(1, result.getAdditionalInformation().size());
        assertEquals("transaction", result.getAdditionalInformation().get(0).getKey());
        assertEquals("additional", result.getAdditionalInformation().get(0).getValue());
    }

    @Test
    @DisplayName("mapper() should allow null responseCode and still build DTO values accordingly")
    void mapper_whenResponseCodeIsNull_shouldStillBuildProcessingResult() {
        when(mapperUtil.isOutputMti("0110")).thenReturn(true);
        when(mapperUtil.convertResponseCodeToLabelData("testNetwork", null)).thenReturn(null);
        when(mapperUtil.getAdditionalInfoValue(null)).thenReturn(null);

        CanonicalFields fields = CanonicalFields.of(Map.of(
                "messageType", "0110",
                "networkName", "testNetwork"
        ));
        ProcessingResultDTO result = strategy.mapper(fields);

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
        when(mapperUtil.isOutputMti("0110")).thenReturn(true);
        when(mapperUtil.convertResponseCodeToLabelData(null, "00")).thenReturn("APPR");
        when(mapperUtil.getAdditionalInfoValue("00")).thenReturn("x");

        CanonicalFields fields = CanonicalFields.of(Map.of(
                "messageType", "0110",
                "responseCode", "00"
        ));
        ProcessingResultDTO result = strategy.mapper(fields);

        assertNotNull(result);
        assertNotNull(result.getResultData());
        assertEquals("APPR", result.getResultData().getOtherResult());
    }

    @Test
    @DisplayName("unMapper() should return empty map when input is null")
    void unMapper_whenInputIsNull_shouldReturnEmptyMap() {
        Map<String, String> result = strategy.unMapper(NETWORK_NAME, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("unMapper() should map approval code when response code is '00'")
    void unMapper_whenResultIsApproved_shouldMapApprovalCode() {
        ResultDataDTO resultData = ResultDataDTO.builder().build();
        resultData.setResult("APPR");

        ProcessingResultDTO input = ProcessingResultDTO.builder().build();
        input.setResultData(resultData);
        input.setApprovalCode("123456");

        when(mapperUtil.convertResponseCodeToLabelData(NETWORK_NAME, "APPR")).thenReturn("00");

        Map<String, String> result = strategy.unMapper(NETWORK_NAME, input);

        assertNotNull(result);
        assertEquals(6, result.size());
        assertEquals("00", result.get("responseCode"));
        assertEquals("123456", result.get("authorizationIdentificationResponse"));
    }

    @Test
    void unMapper_shouldMapField4887AsNull_NolabelCode() {
        String label = "label_not_found";
        ResultDataDTO resultData = ResultDataDTO.builder().build();
        resultData.setOtherResult(label);
        resultData.setResultDetails("Error: Invalid responseCode: " + label);

        AdditionalInformationDTO otherInfo = AdditionalInformationDTO.builder().build();
        otherInfo.setKey("some_other_key");
        otherInfo.setValue("some_value");

        ProcessingResultDTO input = ProcessingResultDTO.builder().build();
        input.setResultData(resultData);
        input.setAdditionalInformation(List.of(otherInfo));

        when(mapperUtil.convertResponseCodeToLabelData(anyString(), anyString())).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                strategy.unMapper(NETWORK_NAME, input));

        String expectedMessage = "Error: Invalid responseCode: " + label;
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void unMapper_shouldMapField4887AsNull_NolabelCodeEmpty() {
        String label = "label_not_found";
        ResultDataDTO resultData = ResultDataDTO.builder().build();
        resultData.setOtherResult(label);
        resultData.setResultDetails("Error: Invalid responseCode: " + label);

        AdditionalInformationDTO otherInfo = AdditionalInformationDTO.builder().build();
        otherInfo.setKey("some_other_key");
        otherInfo.setValue("some_value");

        ProcessingResultDTO input = ProcessingResultDTO.builder().build();
        input.setResultData(resultData);
        input.setAdditionalInformation(List.of(otherInfo));

        when(mapperUtil.convertResponseCodeToLabelData(anyString(), anyString())).thenReturn("");

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                strategy.unMapper(NETWORK_NAME, input));

        String expectedMessage = "Error: Invalid responseCode: " + label;
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("unMapper() should NOT map approval code when response code is not '00'")
    void unMapper_whenResultIsDeclined_shouldNotMapApprovalCode() {
        ResultDataDTO resultData = ResultDataDTO.builder().build();
        resultData.setOtherResult("DECL");

        ProcessingResultDTO input = ProcessingResultDTO.builder().build();
        input.setResultData(resultData);

        when(mapperUtil.convertResponseCodeToLabelData(NETWORK_NAME, "DECL")).thenReturn("05");

        Map<String, String> result = strategy.unMapper(NETWORK_NAME, input);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("05", result.get("responseCode"));
        assertNull(result.get("authorizationIdentificationResponse"));
    }

    @Test
    @DisplayName("unMapper() should map field 48.87 when CVV result is present in additional information")
    void unMapper_whenCvvResultIsPresent_shouldMapField4887() {
        ResultDataDTO resultData = ResultDataDTO.builder().build();
        resultData.setResult("APPR");

        AdditionalInformationDTO cvvInfo = AdditionalInformationDTO.builder().build();
        cvvInfo.setKey("cvv_validation_result");
        cvvInfo.setValue("M");

        ProcessingResultDTO input = ProcessingResultDTO.builder().build();
        input.setResultData(resultData);
        input.setAdditionalInformation(List.of(cvvInfo));
        input.setApprovalCode("BWZEGF");

        when(mapperUtil.convertResponseCodeToLabelData(anyString(), anyString())).thenReturn("51");

        Map<String, String> result = strategy.unMapper(NETWORK_NAME, input);

        assertNotNull(result);
        assertEquals("M", result.get("48.87"));
        assertEquals("51", result.get("responseCode"));
    }

    @Test
    @DisplayName("unMapper() should map field 48.87 as null when CVV result is NOT present")
    void unMapper_whenCvvResultIsNotPresent_shouldMapField4887AsNull() {
        ResultDataDTO resultData = ResultDataDTO.builder().build();
        resultData.setOtherResult("APPR");

        AdditionalInformationDTO otherInfo = AdditionalInformationDTO.builder().build();
        otherInfo.setKey("some_other_key");
        otherInfo.setValue("some_value");

        ProcessingResultDTO input = ProcessingResultDTO.builder().build();
        input.setResultData(resultData);
        input.setAdditionalInformation(List.of(otherInfo));

        when(mapperUtil.convertResponseCodeToLabelData(anyString(), anyString())).thenReturn("00");

        Map<String, String> result = strategy.unMapper(NETWORK_NAME, input);

        assertNotNull(result);
        assertNull(result.get("48.87"));
    }

    @Test
    @DisplayName("unMapper() should map field 48.87 as null when additional information list is null")
    void unMapper_whenAdditionalInformationIsNull_shouldMapField4887AsNull() {
        ResultDataDTO resultData = ResultDataDTO.builder().build();
        resultData.setOtherResult("APPR");

        ProcessingResultDTO input = ProcessingResultDTO.builder().build();
        input.setResultData(resultData);
        input.setAdditionalInformation(null);

        when(mapperUtil.convertResponseCodeToLabelData(anyString(), anyString())).thenReturn("00");

        Map<String, String> result = strategy.unMapper(NETWORK_NAME, input);

        assertNotNull(result);
        assertNull(result.get("48.87"));
    }

    @Test
    void shouldThrowMapperFieldsException_WhenMappingFailsInsideTryBlock() {
        when(mapperUtil.isOutputMti("0210")).thenReturn(true);
        when(mapperUtil.convertResponseCodeToLabelData(anyString(), anyString()))
                .thenThrow(new RuntimeException("Error simulado en dependencia utilitaria"));

        CanonicalFields fields = CanonicalFields.of(Map.of(
                "messageType", "0210",
                "networkName", "VISA",
                "responseCode", "00"
        ));

        assertThatThrownBy(() -> strategy.mapper(fields))
                .isInstanceOf(MapperFieldsException.class)
                .hasMessage("java.lang.RuntimeException: Error simulado en dependencia utilitaria")
                .satisfies(exception -> {
                    MapperFieldsException customEx = (MapperFieldsException) exception;
                    assertThat(customEx.getCode()).isEqualTo("PGWP-00121");
                    assertThat(customEx.getCause())
                            .isInstanceOf(RuntimeException.class)
                            .hasMessage("Error simulado en dependencia utilitaria");
                });
    }
}
