package com.bbva.orchestrator.core;

import com.bbva.gateway.dto.iso20022.*;
import com.bbva.gateway.interceptors.GrpcHeadersInfo;
import com.bbva.gateway.utils.LogsTraces;
import com.bbva.orchestrator.core.builders.ISO20022Builder;
import com.bbva.orchestrator.core.logic.factory.FieldLogicFactory;
import com.bbva.orchestrator.core.logic.factory.NetworkDelegateFieldLogic;
import com.bbva.orchestrator.core.mapper.factory.ISO20022DelegateMapper;
import com.bbva.orchestrator.core.mapper.factory.MapperFactory;
import com.bbva.orchestrator.core.operation.OperationHandler;
import com.bbva.orchestrator.core.operation.factory.OperationHandlerFactory;
import com.bbva.orchestrator.core.parser.factory.ISO8583DelegateParser;
import com.bbva.orchestrator.core.parser.factory.ParserFactory;
import com.bbva.orchestrator.core.transformer.DelegateTransformer;
import com.bbva.orchestrator.core.transformer.factory.TransformerFactory;

import com.bbva.orchlib.utils.RulesLocalUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrchestatorFlowProcessTest {

    @Mock
    private TransformerFactory transformerFactory;
    @Mock
    private OperationHandlerFactory operationHandlerFactory;
    @Mock
    private ISO20022Builder iso20022Builder;
    @Mock
    private MapperFactory mapperFactory;
    @Mock
    private ParserFactory parserFactory;
    @Mock
    private FieldLogicFactory fieldLogicFactory;

    // Collaborators returned by factory mocks (not injected into OrchestratorFlowProcess)
    @Mock
    private DelegateTransformer delegateTransformer;
    @Mock
    private OperationHandler mockOperation;
    @Mock
    private ISO8583DelegateParser delegateParser;
    @Mock
    private ISO20022DelegateMapper delegateMapper;
    @Mock
    private NetworkDelegateFieldLogic delegateFieldLogic;

    @InjectMocks
    private OrchestratorFlowProcess orchestratorFlowProcess;

    private String sampleOriginalMessage;
    private ISO20022 mockIso20022;

    @BeforeEach
    void setUp() {
        lenient().when(parserFactory.getDelegateParser()).thenReturn(delegateParser);
        lenient().when(mapperFactory.getDelegateMapper()).thenReturn(delegateMapper);

        sampleOriginalMessage = "0100ABCD...";
        mockIso20022 = ISO20022.builder().build();
    }

    @Test
    void convert8583to20022_OK() {
        try (MockedStatic<GrpcHeadersInfo> mockedGrpc = mockStatic(GrpcHeadersInfo.class)) {
            mockedGrpc.when(GrpcHeadersInfo::getNetwork).thenReturn("PEER02");

            Map<String, String> fieldsValues = Map.of("messageType", "0100", "networkName", "PEER02");

            when(transformerFactory.getDelegateTransformer("PEER02")).thenReturn(delegateTransformer);
            when(delegateTransformer.toMap(sampleOriginalMessage)).thenReturn(fieldsValues);
            when(operationHandlerFactory.handle(fieldsValues)).thenReturn(mockOperation);
            when(iso20022Builder.build(mockOperation)).thenReturn(mockIso20022);

            ISO20022 result = orchestratorFlowProcess.convert8583to20022(sampleOriginalMessage);

            assertNotNull(result);
            assertSame(mockIso20022, result, "El objeto ISO20022 devuelto debe ser el generado por el builder.");

            verify(transformerFactory).getDelegateTransformer("PEER02");
            verify(delegateTransformer).toMap(sampleOriginalMessage);
            verify(operationHandlerFactory).handle(fieldsValues);
            verify(iso20022Builder).build(mockOperation);
        }
    }

    @Test
    void shouldThrowExceptionWhenTransformerFails() {
        String originalMessage = "0100...";
        try (MockedStatic<GrpcHeadersInfo> mockedGrpc = mockStatic(GrpcHeadersInfo.class)) {
            mockedGrpc.when(GrpcHeadersInfo::getNetwork).thenReturn("TEST_NETWORK");
            when(transformerFactory.getDelegateTransformer("TEST_NETWORK")).thenReturn(delegateTransformer);
            when(delegateTransformer.toMap(originalMessage)).thenThrow(new RuntimeException("Error de transformacion"));

            assertThrows(RuntimeException.class, () -> orchestratorFlowProcess.convert8583to20022(originalMessage));

            verify(operationHandlerFactory, never()).handle(anyMap());
        }
    }

    @Test
    void shouldGenerateNewMessageForPapResponse() {
        final String NETWORK_NAME = "VISA_NETWORK";
        final String FINAL_TRAMA_EXPECTED = "TRAMA_RESPUESTA_GENERADA_8583";

        mockIso20022.setAddendumData(createAddendumData(Map.of("UNSP", "0100")));
        mockIso20022.setNetworkName(NETWORK_NAME);

        ProcessingResultDTO processingResult = ProcessingResultDTO.builder().build();
        processingResult.setResultData(ResultDataDTO.builder().build());
        mockIso20022.setProcessingResult(processingResult);

        Map<String, String> unmappedFields = Map.of("0", "0100", "39", "00");
        when(delegateMapper.unMapper(mockIso20022)).thenReturn(unmappedFields);

        Map<String, String> fieldsWithLogic = Map.of("0", "0110", "39", "00");
        when(fieldLogicFactory.getDelegateFieldLogic(NETWORK_NAME)).thenReturn(delegateFieldLogic);
        when(delegateFieldLogic.applyLogicFields(unmappedFields)).thenReturn(fieldsWithLogic);

        when(delegateParser.unParser(fieldsWithLogic)).thenReturn(FINAL_TRAMA_EXPECTED);

        try (MockedStatic<LogsTraces> mockedLogs = mockStatic(LogsTraces.class)) {
            mockIso20022.setMonitoring(MonitoringDTO.builder().isNextGen(true).build());
            String result = orchestratorFlowProcess.convert20022to8583(mockIso20022);

            Assertions.assertThat(result).isEqualTo(FINAL_TRAMA_EXPECTED);

            verify(mapperFactory).getDelegateMapper();
            verify(delegateMapper).unMapper(mockIso20022);
            verify(fieldLogicFactory).getDelegateFieldLogic(NETWORK_NAME);
            verify(delegateFieldLogic).applyLogicFields(unmappedFields);
            verify(parserFactory).getDelegateParser();
            verify(delegateParser).unParser(fieldsWithLogic);
        }
    }

    private AddendumDataDTO createAddendumData(Map<String, String> data) {
        AddendumDataDTO addendumData = AddendumDataDTO.builder().build();
        List<AdditionalDataDTO> additionalDataList = data.entrySet().stream()
                .map(entry -> {
                    AdditionalDataDTO additionalData = AdditionalDataDTO.builder().build();
                    additionalData.setKey(entry.getKey());
                    additionalData.setValue(entry.getValue());
                    return additionalData;
                })
                .collect(java.util.stream.Collectors.toList());
        addendumData.setAdditionalData(additionalDataList);
        return addendumData;
    }

    @Test
    void convert20022to8583_FlowPassThrough_OK() {
        String expectedTrama = "TRAMA_DESDE_HOST_8583";
        MonitoringDTO monitoring = MonitoringDTO.builder().isNextGen(false).build();

        List<AdditionalDataDTO> additionalData = List.of(
                AdditionalDataDTO.builder().key("ISO8583_HOST").value(expectedTrama).build()
        );
        AddendumDataDTO addendumData = AddendumDataDTO.builder().additionalData(additionalData).build();

        mockIso20022.setMonitoring(monitoring);
        mockIso20022.setAddendumData(addendumData);
        mockIso20022.setNetworkName("VISA");

        try (MockedStatic<LogsTraces> mockedLogs = mockStatic(LogsTraces.class);
             MockedStatic<RulesLocalUtils> mockedRules = mockStatic(RulesLocalUtils.class)) {

            mockedRules.when(RulesLocalUtils::getLastOrchestration).thenReturn("RULE_01");

            String result = orchestratorFlowProcess.convert20022to8583(mockIso20022);

            assertEquals(expectedTrama, result);
            mockedLogs.verify(() -> LogsTraces.writeInfo(anyString()), times(1));
        }
    }

    @Test
    void convert20022to8583_PassThrough_KeyNotFound_ReturnsNull() {
        mockIso20022.setMonitoring(MonitoringDTO.builder().isNextGen(false).build());
        mockIso20022.setAddendumData(AddendumDataDTO.builder().additionalData(new ArrayList<>()).build());

        try (MockedStatic<LogsTraces> mockedLogs = mockStatic(LogsTraces.class)) {
            String result = orchestratorFlowProcess.convert20022to8583(mockIso20022);
            assertNull(result);
        }
    }

    @Test
    void convert20022to8583_AddendumDataNull_ReturnsNull() {
        mockIso20022.setMonitoring(MonitoringDTO.builder().isNextGen(false).build());
        mockIso20022.setAddendumData(null);

        try (MockedStatic<LogsTraces> mockedLogs = mockStatic(LogsTraces.class)) {
            String result = orchestratorFlowProcess.convert20022to8583(mockIso20022);
            assertNull(result);
        }
    }

    @Test
    void convert20022to8583_AdditionalDataListNull_ReturnsNull() {
        mockIso20022.setMonitoring(MonitoringDTO.builder().isNextGen(false).build());
        AddendumDataDTO addendumData = AddendumDataDTO.builder().additionalData(null).build();
        mockIso20022.setAddendumData(addendumData);

        try (MockedStatic<LogsTraces> mockedLogs = mockStatic(LogsTraces.class)) {
            String result = orchestratorFlowProcess.convert20022to8583(mockIso20022);
            assertNull(result);
        }
    }
}
