package com.bbva.orchestrator.core;

import com.bbva.gateway.dto.iso20022.*;
import com.bbva.gateway.interceptors.GrpcHeadersInfo;
import com.bbva.gateway.utils.LogsTraces;
import com.bbva.orchestrator.core.dto.ISO8583;
import com.bbva.orchestrator.core.builders.ISO8583Builder;

import com.bbva.orchestrator.core.logic.factory.FieldLogicFactory;
import com.bbva.orchestrator.core.logic.factory.NetworkDelegateFieldLogic;
import com.bbva.orchestrator.core.mapper.factory.ISO20022DelegateMapper;
import com.bbva.orchestrator.core.mapper.factory.MapperFactory;
import com.bbva.orchestrator.core.parser.factory.ISO8583DelegateParser;
import com.bbva.orchestrator.core.parser.factory.ParserFactory;

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
    private ParserFactory parserFactory;

    @Mock
    private MapperFactory mapperFactory;

    @Mock
    private ISO8583DelegateParser delegateParser;

    @Mock
    private ISO20022DelegateMapper delegateMapper;

    @Mock
    private FieldLogicFactory fieldLogicFactory;

    @Mock
    private NetworkDelegateFieldLogic delegateFieldLogic;
    @InjectMocks
    private OrchestratorFlowProcess orchestratorFlowProcess;

    // --- Variables de prueba ---
    private String sampleOriginalMessage;
    private Map<String, String> sampleFieldsValues;
    private Map<String, String> sampleSubFieldsValues;
    private ISO8583 mockIso8583;
    private ISO20022 mockIso20022;

    @BeforeEach
    void setUp() {
        lenient().when(parserFactory.getDelegateParser()).thenReturn(delegateParser);
        lenient().when(mapperFactory.getDelegateMapper()).thenReturn(delegateMapper);
        // Inicializamos los datos de prueba que se usarán en varios tests
        sampleOriginalMessage = "0100ABCD...";
        sampleFieldsValues = new HashMap<>();
        sampleFieldsValues.put("0", "0100");
        sampleFieldsValues.put("2", "1234567890123456");

        sampleSubFieldsValues = new HashMap<>();
        sampleSubFieldsValues.put("P-22", "051");

        mockIso8583 = ISO8583.builder().networkName("PEER02").build(); // O mock(ISO8583.class) si es complejo

        mockIso20022 = ISO20022.builder().build(); // O mock(ISO20022.class)
    }


    private ISO20022 buildISO20022(String msgType, String flowType, String iso8583Host) {
        List<AdditionalDataDTO> additionalDataList = new ArrayList<>();

        if (msgType != null) {
            additionalDataList.add(AdditionalDataDTO.builder().key("UNSP").value(msgType).build());
        }
        if (flowType != null) {
            additionalDataList.add(AdditionalDataDTO.builder().key("FLOWTYPE").value(flowType).build());
        }
        if (iso8583Host != null) {
            additionalDataList.add(AdditionalDataDTO.builder().key("ISO8583_HOST").value(iso8583Host).build());
        }

        AddendumDataDTO addendumData = AddendumDataDTO.builder()
                .additionalData(additionalDataList)
                .build();

        return ISO20022.builder()
                .addendumData(addendumData)
                .build();
    }

    @Test
    void convert8583to20022_OK() {

        // --- Arrange (Preparar) ---
        // Usamos try-with-resources para asegurar que los mocks estáticos se cierren
        try (
                MockedStatic<GrpcHeadersInfo> mockedGrpc = mockStatic(GrpcHeadersInfo.class);
                MockedStatic<ISO8583Builder> mockedBuilder = mockStatic(ISO8583Builder.class)
        ) {
            // 1. Simular llamadas estáticas
            mockedGrpc.when(GrpcHeadersInfo::getNetwork).thenReturn("PEER02");
            mockedBuilder.when(() -> ISO8583Builder.buildISO8583(anyString(), anyMap())).thenReturn(mockIso8583);

            // 2. Simular el comportamiento de las factories y sus delegados
            when(parserFactory.getDelegateParser("PEER02")).thenReturn(delegateParser);
            when(delegateParser.parser(sampleOriginalMessage)).thenReturn(sampleFieldsValues);

            when(fieldLogicFactory.getDelegateFieldLogic("PEER02")).thenReturn(delegateFieldLogic);
            when(delegateFieldLogic.parseSubfields(mockIso8583)).thenReturn(sampleSubFieldsValues);

            when(mapperFactory.getDelegateMapper()).thenReturn(delegateMapper);
            when(delegateMapper.mapper(mockIso8583, sampleSubFieldsValues)).thenReturn(mockIso20022);

            // --- Act (Actuar) ---
            ISO20022 result = orchestratorFlowProcess.convert8583to20022(sampleOriginalMessage);

            // --- Assert (Afirmar) ---
            // 1. Verificar que el resultado es el esperado
            assertNotNull(result);
            assertSame(mockIso20022, result, "El objeto ISO20022 devuelto debe ser el generado por el mapper.");

            // 2. Verificar que todos los colaboradores fueron llamados en el orden correcto y con los argumentos correctos
            verify(parserFactory).getDelegateParser("PEER02");
            verify(delegateParser).parser(sampleOriginalMessage);
            verify(fieldLogicFactory).getDelegateFieldLogic("PEER02");
            verify(delegateFieldLogic).parseSubfields(mockIso8583);
            verify(mapperFactory).getDelegateMapper();
            verify(delegateMapper).mapper(mockIso8583, sampleSubFieldsValues);
        }
    }

    @Test
    void shouldThrowExceptionWhenParserFails() {
        // Arrange
        String originalMessage = "0100...";
        try (MockedStatic<GrpcHeadersInfo> mockedGrpc = mockStatic(GrpcHeadersInfo.class)) {
            mockedGrpc.when(GrpcHeadersInfo::getNetwork).thenReturn("TEST_NETWORK");
            when(parserFactory.getDelegateParser("TEST_NETWORK")).thenReturn(delegateParser);
            when(delegateParser.parser(originalMessage)).thenThrow(new RuntimeException("Error de parseo"));

            // Act & Assert
            assertThrows(RuntimeException.class, () -> orchestratorFlowProcess.convert8583to20022(originalMessage));

            // Verificamos que el flujo se detuvo
            verify(fieldLogicFactory, never()).getDelegateFieldLogic(anyString());
        }
    }

    // Dentro de la clase OrchestatorFlowProcessTest

    @Test
    void shouldGenerateNewMessageForPapResponse() {
        // --- Arrange (Preparar) ---

        // 1. Datos de prueba
        final String NETWORK_NAME = "VISA_NETWORK";
        final String FINAL_TRAMA_EXPECTED = "TRAMA_RESPUESTA_GENERADA_8583";

        // 2. Configurar el ISO20022 de entrada para forzar el bloque 'else' (respuesta PAP)

        // Tipo de Mensaje: 0100 (para que requiredUnparser devuelva true)
        mockIso20022.setAddendumData(createAddendumData(Map.of("UNSP", "0100")));
        mockIso20022.setNetworkName(NETWORK_NAME);

        // ProcessingResult: Configurar un resultado NO nulo para que isProcessingResultNullOrEmpty devuelva false
        ProcessingResultDTO processingResult = ProcessingResultDTO.builder().build();
        // Es CRUCIAL que resultData NO sea null (o que sea un objeto)
        processingResult.setResultData(ResultDataDTO.builder().build());
        mockIso20022.setProcessingResult(processingResult);

        // 3. Mocks de la lógica interna del bloque 'else'

        // a) Mapeo inverso (unMapper)
        Map<String, String> unmappedFields = Map.of("0", "0100", "39", "00");
        when(delegateMapper.unMapper(mockIso20022)).thenReturn(unmappedFields);

        // b) Lógica de campos (applyLogicFields)
        Map<String, String> fieldsWithLogic = Map.of("0", "0110", "39", "00"); // Simula el cambio de 0100 a 0110
        when(fieldLogicFactory.getDelegateFieldLogic(NETWORK_NAME)).thenReturn(delegateFieldLogic);
        when(delegateFieldLogic.applyLogicFields(unmappedFields)).thenReturn(fieldsWithLogic);

        // c) Un-parser final
        when(delegateParser.unParser(fieldsWithLogic)).thenReturn(FINAL_TRAMA_EXPECTED);


        try (MockedStatic<LogsTraces> mockedLogs = mockStatic(LogsTraces.class)) {
            // --- Act (Actuar) ---
            mockIso20022.setMonitoring(MonitoringDTO.builder().isNextGen(true).build());
            String result = orchestratorFlowProcess.convert20022to8583(mockIso20022);

            // --- Assert (Afirmar) ---

            // 1. Verificar el resultado final
            Assertions.assertThat(result).isEqualTo(FINAL_TRAMA_EXPECTED);

            // 2. Verificar las llamadas a los colaboradores del bloque 'else'
            verify(mapperFactory).getDelegateMapper();
            verify(delegateMapper).unMapper(mockIso20022);
            verify(fieldLogicFactory).getDelegateFieldLogic(NETWORK_NAME);
            verify(delegateFieldLogic).applyLogicFields(unmappedFields);
            verify(parserFactory).getDelegateParser();
            verify(delegateParser).unParser(fieldsWithLogic);

        }
    }

    private AddendumDataDTO createAddendumData(Map<String, String> data) {
        AddendumDataDTO addendumData =AddendumDataDTO.builder().build();
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
        // --- Arrange ---
        String expectedTrama = "TRAMA_DESDE_HOST_8583";
        // Configuramos isNextGen en false para entrar al flujo PassThrough
        MonitoringDTO monitoring = MonitoringDTO.builder().isNextGen(false).build();

        // Creamos el DTO con la clave ISO8583_HOST que busca el método flowPassThrough
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

            // --- Act ---
            String result = orchestratorFlowProcess.convert20022to8583(mockIso20022);

            // --- Assert ---
            assertEquals(expectedTrama, result);
            mockedLogs.verify(() -> LogsTraces.writeInfo(anyString()), times(1));
        }
    }

    @Test
    void convert20022to8583_PassThrough_KeyNotFound_ReturnsNull() {
        // --- Arrange ---
        mockIso20022.setMonitoring(MonitoringDTO.builder().isNextGen(false).build());
        // AddendumData vacío para probar el filtrado del stream y el orElse
        mockIso20022.setAddendumData(AddendumDataDTO.builder().additionalData(new ArrayList<>()).build());

        try (MockedStatic<LogsTraces> mockedLogs = mockStatic(LogsTraces.class)) {
            // --- Act ---
            String result = orchestratorFlowProcess.convert20022to8583(mockIso20022);

            // --- Assert ---
            assertNull(result);
        }
    }

    @Test
    void convert20022to8583_AddendumDataNull_ReturnsNull() {
        // --- Arrange ---
        mockIso20022.setMonitoring(MonitoringDTO.builder().isNextGen(false).build());
        mockIso20022.setAddendumData(null); // Caso: input == null en findValueByKey

        try (MockedStatic<LogsTraces> mockedLogs = mockStatic(LogsTraces.class)) {
            // --- Act ---
            String result = orchestratorFlowProcess.convert20022to8583(mockIso20022);

            // --- Assert ---
            assertNull(result);
        }
    }

    @Test
    void convert20022to8583_AdditionalDataListNull_ReturnsNull() {
        // --- Arrange ---
        mockIso20022.setMonitoring(MonitoringDTO.builder().isNextGen(false).build());
        // Caso: input.getAdditionalData() == null en findValueByKey
        AddendumDataDTO addendumData = AddendumDataDTO.builder().additionalData(null).build();
        mockIso20022.setAddendumData(addendumData);

        try (MockedStatic<LogsTraces> mockedLogs = mockStatic(LogsTraces.class)) {
            // --- Act ---
            String result = orchestratorFlowProcess.convert20022to8583(mockIso20022);

            // --- Assert ---
            assertNull(result);
        }
    }
}