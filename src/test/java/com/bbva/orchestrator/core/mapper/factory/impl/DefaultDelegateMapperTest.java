package com.bbva.orchestrator.core.mapper.factory.impl;

import com.bbva.gateway.dto.iso20022.*;
import com.bbva.gateway.interceptors.GrpcHeadersInfo;
import com.bbva.gateway.utils.LogsTraces;
import com.bbva.orchestrator.core.enums.MessageFunction;
import com.bbva.orchestrator.core.exception.MapperFieldsException;
import com.bbva.orchestrator.core.mapper.iso20022.strategy.impl.*;
import com.bbva.orchestrator.core.mapper.model.CanonicalFields;
import com.bbva.orchestrator.core.builders.MonitoringBuilder;
import com.bbva.orchestrator.core.utils.MapperUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DefaultDelegateMapperTest {

    @InjectMocks
    private DefaultDelegateMapper defaultDelegateMapper;
    @Mock
    private EnvironmentMappingStrategy environmentStrategy;
    @Mock
    private TransactionMappingStrategy transactionStrategy;
    @Mock
    private ContextMappingStrategy contextStrategy;
    @Mock
    private SupplementaryDataMappingStrategy supplementaryDataStrategy;
    @Mock
    private SecurityTrailerMappingStrategy securityTrailerStrategy;
    @Mock
    private ProtectedDataMappingStrategy protectedDataStrategy;
    @Mock
    private TraceDataMappingStrategy traceDataStrategy;
    @Mock
    private AddendumDataMappingStrategy addendumDataStrategy;
    @Mock
    private CustomDataLocalMappingStrategy customDataLocalStrategy;
    @Mock
    private ProcessingResultMappingStrategy processingResultMappingStrategy;
    @Mock
    private MonitoringBuilder monitoringService;
    @Mock
    private MapperUtil mapperUtil;

    private MockedStatic<GrpcHeadersInfo> mockedHeaders;

    private CanonicalFields canonicalInput;

    private static TransactionDTO txnWithRef(String ref) {
        return TransactionDTO.builder()
                .transactionId(TransactionIdDTO.builder()
                        .transactionReference(ref)
                        .build())
                .build();
    }

    @BeforeEach
    void setUp() {
        mockedHeaders = mockStatic(GrpcHeadersInfo.class);

        canonicalInput = CanonicalFields.of(Map.of(
                "networkName", "PEER02",
                "messageType", "0200",
                "originalMessage", "originalMessage",
                "primaryAccountNumber", "123456789"
        ));

        mockedHeaders.when(GrpcHeadersInfo::getNetwork).thenReturn("networkTest");
        mockedHeaders.when(GrpcHeadersInfo::getTraceId).thenReturn("trace123");
        mockedHeaders.when(GrpcHeadersInfo::getPort).thenReturn("portTest");
    }

    @AfterEach
    void tearDown() {
        mockedHeaders.close();
    }

    @Test
    void testMapper_successfulMapping_returnsISO20022Object() {
        // Arrange
        when(environmentStrategy.mapper(any(CanonicalFields.class))).thenReturn(EnvironmentDTO.builder().build());
        when(transactionStrategy.mapper(any(CanonicalFields.class))).thenReturn(txnWithRef("REF-001"));
        when(contextStrategy.mapper(any(CanonicalFields.class))).thenReturn(ContextDTO.builder().build());
        when(supplementaryDataStrategy.mapper(any(CanonicalFields.class))).thenReturn(List.of(SupplementaryDataDTO.builder().build()));
        when(traceDataStrategy.mapper(any(CanonicalFields.class))).thenReturn(List.of(TraceDataDTO.builder().build()));
        when(protectedDataStrategy.mapper(any(CanonicalFields.class))).thenReturn(List.of(ProtectedDataDTO.builder().build()));
        when(securityTrailerStrategy.mapper(any(CanonicalFields.class))).thenReturn(SecurityTrailerDTO.builder().build());
        when(addendumDataStrategy.mapper(any(CanonicalFields.class))).thenReturn(AddendumDataDTO.builder().build());
        when(customDataLocalStrategy.mapper(any(CanonicalFields.class))).thenReturn(CustomDataLocalDTO.builder().build());

        // Act
        ISO20022 result = defaultDelegateMapper.mapper(canonicalInput);

        // Assert
        assertNotNull(result);
        assertEquals("PEER02", result.getNetworkName());

        Mockito.verify(environmentStrategy).mapper(any(CanonicalFields.class));
        Mockito.verify(transactionStrategy).mapper(any(CanonicalFields.class));
    }

    @Test
    void testMapper_mapperLocalException_returnsFallbackResponse() {
        // Arrange
        doThrow(new MapperFieldsException("Mapper local exception")).when(environmentStrategy).mapper(any(CanonicalFields.class));

        // Act
        ISO20022 result = defaultDelegateMapper.mapper(canonicalInput);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getEnvironment());
        assertNotNull(result.getAddendumData());
        assertEquals("123456789", result.getEnvironment().getCard().getPan());
        assertEquals("originalMessage", result.getAddendumData().getAdditionalData().get(0).getValue());
    }

    @Test
    void testMapper_mapperLocalException_returnsFallbackResponse_0100() {
        // Arrange
        doThrow(new MapperFieldsException("Mapper local exception")).when(environmentStrategy).mapper(any(CanonicalFields.class));

        CanonicalFields canonicalInput_ = CanonicalFields.of(Map.of(
                "networkName", "PEER02",
                "messageType", "0100",
                "originalMessage", "originalMessage",
                "primaryAccountNumber", "123456789"
        ));

        // Act
        ISO20022 result = defaultDelegateMapper.mapper(canonicalInput_);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getEnvironment());
        assertNotNull(result.getAddendumData());
        assertEquals("123456789", result.getEnvironment().getCard().getPan());
        assertEquals("originalMessage", result.getAddendumData().getAdditionalData().get(0).getValue());
    }

    @Test
    void testMapper_unexpectedException_returnsFallbackResponse() {
        // Arrange
        doThrow(new RuntimeException("Unexpected exception")).when(environmentStrategy).mapper(any(CanonicalFields.class));

        // Act
        ISO20022 result = defaultDelegateMapper.mapper(canonicalInput);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getEnvironment());
        assertNotNull(result.getAddendumData());
        assertEquals("123456789", result.getEnvironment().getCard().getPan());
        assertEquals("originalMessage", result.getAddendumData().getAdditionalData().get(0).getValue());
    }

    @Test
    void testUnMapper_successfulUnMapping_returnsMap() {
        // Arrange
        ISO20022 input = ISO20022.builder()
                .addendumData(AddendumDataDTO.builder().build())
                .environment(EnvironmentDTO.builder().build())
                .build();

        when(addendumDataStrategy.unMapper(any(), any(AddendumDataDTO.class))).thenReturn(Map.of("key1", "value1"));
        when(environmentStrategy.unMapper(any(), any(EnvironmentDTO.class))).thenReturn(Map.of("key2", "value2"));

        lenient().when(processingResultMappingStrategy.unMapper(any(), any())).thenReturn(new HashMap<>());

        // Act
        Map<String, String> result = defaultDelegateMapper.unMapper(input);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("key1"));
        assertTrue(result.containsKey("key2"));
        assertEquals("value1", result.get("key1"));
        assertEquals("value2", result.get("key2"));
    }

    @Test
    void testMapper_withNullOptionalFields_buildsCorrectly() {
        // Arrange
        when(environmentStrategy.mapper(any(CanonicalFields.class))).thenReturn(EnvironmentDTO.builder().build());
        when(transactionStrategy.mapper(any(CanonicalFields.class))).thenReturn(txnWithRef("REF-002"));

        when(contextStrategy.mapper(any(CanonicalFields.class))).thenReturn(null);
        when(protectedDataStrategy.mapper(any(CanonicalFields.class))).thenReturn(null);
        when(securityTrailerStrategy.mapper(any(CanonicalFields.class))).thenReturn(null);
        when(supplementaryDataStrategy.mapper(any(CanonicalFields.class))).thenReturn(null);

        when(traceDataStrategy.mapper(any(CanonicalFields.class))).thenReturn(List.of(TraceDataDTO.builder().build()));
        when(addendumDataStrategy.mapper(any(CanonicalFields.class))).thenReturn(AddendumDataDTO.builder().build());
        when(customDataLocalStrategy.mapper(any(CanonicalFields.class))).thenReturn(CustomDataLocalDTO.builder().build());

        lenient().when(monitoringService.build(any(), any(), any(), any())).thenReturn(MonitoringDTO.builder().build());

        // Act
        ISO20022 result = defaultDelegateMapper.mapper(canonicalInput);

        // Assert
        assertNotNull(result);
        assertNull(result.getContext());
        assertNull(result.getProtectedData());
        assertNull(result.getSecurityTrailer());
        assertNull(result.getSupplementaryData());
        assertNotNull(result.getEnvironment());
    }

    @Test
    void testMapper_withResponseMessageType_addsProcessingResult() {
        // Arrange
        CanonicalFields canonicalInput2 = CanonicalFields.of(Map.of(
                "messageType", "0120",
                "originalMessage", "originalMessage",
                "primaryAccountNumber", "123456789"
        ));

        when(environmentStrategy.mapper(any(CanonicalFields.class))).thenReturn(EnvironmentDTO.builder().build());
        when(transactionStrategy.mapper(any(CanonicalFields.class))).thenReturn(txnWithRef("REF-003"));
        when(contextStrategy.mapper(any(CanonicalFields.class))).thenReturn(ContextDTO.builder().build());
        when(supplementaryDataStrategy.mapper(any(CanonicalFields.class))).thenReturn(List.of(SupplementaryDataDTO.builder().build()));
        when(traceDataStrategy.mapper(any(CanonicalFields.class))).thenReturn(List.of(TraceDataDTO.builder().build()));
        when(protectedDataStrategy.mapper(any(CanonicalFields.class))).thenReturn(List.of(ProtectedDataDTO.builder().build()));
        when(securityTrailerStrategy.mapper(any(CanonicalFields.class))).thenReturn(SecurityTrailerDTO.builder().build());
        when(addendumDataStrategy.mapper(any(CanonicalFields.class))).thenReturn(AddendumDataDTO.builder().build());
        when(customDataLocalStrategy.mapper(any(CanonicalFields.class))).thenReturn(CustomDataLocalDTO.builder().build());

        lenient().when(monitoringService.build(any(), any(), any(), any())).thenReturn(MonitoringDTO.builder().build());

        // Act
        ISO20022 result = defaultDelegateMapper.mapper(canonicalInput2);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testMapper_builderBlocks_fullCoverage() {
        // Arrange
        EnvironmentDTO env = EnvironmentDTO.builder().build();
        TransactionDTO txn = txnWithRef("TXN-FULL");
        ContextDTO ctx = ContextDTO.builder().build();
        List<SupplementaryDataDTO> supp = List.of(SupplementaryDataDTO.builder().build());
        List<TraceDataDTO> trace = List.of(TraceDataDTO.builder().build());
        List<ProtectedDataDTO> prot = List.of(ProtectedDataDTO.builder().build());
        SecurityTrailerDTO sec = SecurityTrailerDTO.builder().build();
        AddendumDataDTO add = AddendumDataDTO.builder().build();
        CustomDataLocalDTO custom = CustomDataLocalDTO.builder().build();
        MonitoringDTO monitoring = MonitoringDTO.builder().build();

        lenient().when(environmentStrategy.mapper(any(CanonicalFields.class))).thenReturn(env);
        lenient().when(transactionStrategy.mapper(any(CanonicalFields.class))).thenReturn(txn);
        lenient().when(contextStrategy.mapper(any(CanonicalFields.class))).thenReturn(ctx);
        lenient().when(supplementaryDataStrategy.mapper(any(CanonicalFields.class))).thenReturn(supp);
        lenient().when(traceDataStrategy.mapper(any(CanonicalFields.class))).thenReturn(trace);
        lenient().when(protectedDataStrategy.mapper(any(CanonicalFields.class))).thenReturn(prot);
        lenient().when(securityTrailerStrategy.mapper(any(CanonicalFields.class))).thenReturn(sec);
        lenient().when(addendumDataStrategy.mapper(any(CanonicalFields.class))).thenReturn(add);
        lenient().when(customDataLocalStrategy.mapper(any(CanonicalFields.class))).thenReturn(custom);
        lenient().when(monitoringService.build(any(), any(), any(), any())).thenReturn(monitoring);

        CanonicalFields input = CanonicalFields.of(Map.of("messageType", "0200"));

        // Act
        ISO20022 result = defaultDelegateMapper.mapper(input);

        // Assert
        assertNotNull(result);
        verify(environmentStrategy).mapper(any(CanonicalFields.class));
        verify(transactionStrategy).mapper(any(CanonicalFields.class));
        verify(contextStrategy).mapper(any(CanonicalFields.class));
        verify(supplementaryDataStrategy).mapper(any(CanonicalFields.class));
        verify(traceDataStrategy).mapper(any(CanonicalFields.class));
        verify(protectedDataStrategy).mapper(any(CanonicalFields.class));
        verify(securityTrailerStrategy).mapper(any(CanonicalFields.class));
        verify(addendumDataStrategy).mapper(any(CanonicalFields.class));
        verify(customDataLocalStrategy).mapper(any(CanonicalFields.class));
    }

    @Test
    void testMapper_builderBlocks_conditionalCoverage() {
        // Arrange
        EnvironmentDTO env = EnvironmentDTO.builder().build();
        TransactionDTO txn = TransactionDTO.builder()
                .transactionId(TransactionIdDTO.builder()
                        .transactionReference("TXN-REF-123")
                        .build())
                .build();
        ContextDTO ctx = ContextDTO.builder()
                .saleContext(SaleContextDTO.builder().build())
                .build();
        List<SupplementaryDataDTO> supp = List.of(SupplementaryDataDTO.builder().build());
        List<TraceDataDTO> trace = List.of(TraceDataDTO.builder().build());
        List<ProtectedDataDTO> prot = List.of(ProtectedDataDTO.builder().build());
        SecurityTrailerDTO sec = SecurityTrailerDTO.builder().build();
        AddendumDataDTO add = AddendumDataDTO.builder().build();
        CustomDataLocalDTO custom = CustomDataLocalDTO.builder().build();
        MonitoringDTO monitoring = MonitoringDTO.builder().build();

        when(environmentStrategy.mapper(any(CanonicalFields.class))).thenReturn(env);
        when(transactionStrategy.mapper(any(CanonicalFields.class))).thenReturn(txn);
        when(contextStrategy.mapper(any(CanonicalFields.class))).thenReturn(ctx);
        when(supplementaryDataStrategy.mapper(any(CanonicalFields.class))).thenReturn(supp);
        when(traceDataStrategy.mapper(any(CanonicalFields.class))).thenReturn(trace);
        when(protectedDataStrategy.mapper(any(CanonicalFields.class))).thenReturn(prot);
        when(securityTrailerStrategy.mapper(any(CanonicalFields.class))).thenReturn(sec);
        when(addendumDataStrategy.mapper(any(CanonicalFields.class))).thenReturn(add);
        when(customDataLocalStrategy.mapper(any(CanonicalFields.class))).thenReturn(custom);
        when(monitoringService.build(any(), any(), any(), any())).thenReturn(monitoring);

        try (MockedStatic<LogsTraces> mockedLogs = mockStatic(LogsTraces.class)) {
            mockedLogs.when(() -> LogsTraces.writeInfo(anyString())).thenAnswer(invocation -> null);

            CanonicalFields input = CanonicalFields.of(Map.of("messageType", "0100"));

            // Act
            ISO20022 result = defaultDelegateMapper.mapper(input);

            // Assert
            assertAll("Verificación de mapeo completo",
                    () -> assertNotNull(result.getContext(), "Context no debería ser nulo"),
                    () -> assertNotNull(result.getProtectedData(), "ProtectedData no debería ser nulo"),
                    () -> assertNotNull(result.getSecurityTrailer(), "SecurityTrailer no debería ser nulo"),
                    () -> assertNotNull(result.getSupplementaryData(), "SupplementaryData no debería ser nulo"),
                    () -> assertNotNull(result.getEnvironment(), "Environment no debería ser nulo"),
                    () -> assertNotNull(result.getTransaction(), "Transaction no debería ser nulo"),
                    () -> assertNotNull(result.getTraceData(), "TraceData no debería ser nulo"),
                    () -> assertNotNull(result.getAddendumData(), "AddendumData no debería ser nulo"),
                    () -> assertNotNull(result.getCustomDataLocal(), "CustomDataLocal no debería ser nulo"),
                    () -> assertNotNull(result.getMonitoring(), "Monitoring no debería ser nulo")
            );

            mockedLogs.verify(() -> LogsTraces.writeInfo(anyString()), atLeastOnce());
        }
    }

    @Test
    void testMapper_HostResponseFlow_0110_ShouldCallOnlyRequiredStrategies() {
        // Arrange
        CanonicalFields canonicalHost = CanonicalFields.of(Map.of(
                "networkName", "PEER02",
                "messageType", "0110",
                "originalMessage", "origMsg",
                "primaryAccountNumber", "123456789",
                "processingCode", "000000"
        ));

        TransactionDTO txn = txnWithRef("REF123");

        when(environmentStrategy.mapperResponse(any(CanonicalFields.class))).thenReturn(EnvironmentDTO.builder().build());
        when(transactionStrategy.mapperResponse(any(CanonicalFields.class))).thenReturn(txn);
        when(contextStrategy.mapperResponse(any(CanonicalFields.class))).thenReturn(ContextDTO.builder().build());
        when(addendumDataStrategy.mapperResponse(any(CanonicalFields.class))).thenReturn(AddendumDataDTO.builder().build());
        when(monitoringService.build(any(CanonicalFields.class), any(), any(), any())).thenReturn(MonitoringDTO.builder().build());
        when(processingResultMappingStrategy.mapper(any(CanonicalFields.class))).thenReturn(ProcessingResultDTO.builder().build());

        try (MockedStatic<LogsTraces> mockedLogs = mockStatic(LogsTraces.class)) {

            // Act
            ISO20022 result = defaultDelegateMapper.mapper(canonicalHost);

            // Assert
            assertNotNull(result);

            verify(addendumDataStrategy).mapperResponse(any(CanonicalFields.class));
            verify(contextStrategy).mapperResponse(any(CanonicalFields.class));

            verify(supplementaryDataStrategy, never()).mapper(any(CanonicalFields.class));
            verify(protectedDataStrategy, never()).mapper(any(CanonicalFields.class));
            verify(securityTrailerStrategy, never()).mapper(any(CanonicalFields.class));
        }
    }

    @Test
    void testMapper_MainFlow_0420_ShouldAddProcessingResult() {
        // Arrange
        CanonicalFields input = CanonicalFields.of(Map.of(
                "messageType", "0420",
                "networkName", "NET"
        ));

        when(environmentStrategy.mapper(any(CanonicalFields.class))).thenReturn(EnvironmentDTO.builder().build());
        when(transactionStrategy.mapper(any(CanonicalFields.class))).thenReturn(txnWithRef("REF"));
        when(processingResultMappingStrategy.mapper(any(CanonicalFields.class))).thenReturn(ProcessingResultDTO.builder().build());
        when(contextStrategy.mapper(any(CanonicalFields.class))).thenReturn(null);

        try (MockedStatic<LogsTraces> mockedLogs = mockStatic(LogsTraces.class)) {
            // Act
            ISO20022 result = defaultDelegateMapper.mapper(input);

            // Assert
            assertNotNull(result.getProcessingResult());
            verify(processingResultMappingStrategy).mapper(any(CanonicalFields.class));
        }
    }

    @Test
    void testMapper_NullMessageType_ShouldReturnFallbackResponse() {
        // Arrange — messageType absent so getMessageType() returns null
        Map<String, String> mapInvalid = new HashMap<>();
        mapInvalid.put("primaryAccountNumber", "9999");
        mapInvalid.put("originalMessage", "raw");
        CanonicalFields canonicalInvalid = CanonicalFields.of(mapInvalid);

        // Act
        ISO20022 result = defaultDelegateMapper.mapper(canonicalInvalid);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getEnvironment());
        assertEquals("9999", result.getEnvironment().getCard().getPan());
        assertNull(result.getAddendumData().getAdditionalData().get(1).getValue());
    }

    @Test
    void testMapper_ShortMessageType_ShouldNotBeHostResponse() {
        // Arrange
        CanonicalFields inputShort = CanonicalFields.of(Map.of(
                "messageType", "010",
                "networkName", "NET"
        ));

        when(environmentStrategy.mapper(any(CanonicalFields.class))).thenReturn(EnvironmentDTO.builder().build());
        when(transactionStrategy.mapper(any(CanonicalFields.class))).thenReturn(txnWithRef("REF-SHORT"));

        try (MockedStatic<LogsTraces> mockedLogs = mockStatic(LogsTraces.class)) {
            // Act
            ISO20022 result = defaultDelegateMapper.mapper(inputShort);

            // Assert
            assertNotNull(result);
            verify(transactionStrategy).mapper(any(CanonicalFields.class));
        }
    }
}
