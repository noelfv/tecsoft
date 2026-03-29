package com.bbva.orchestrator.core.builders;

import com.bbva.gateway.dto.iso20022.*;
import com.bbva.gateway.interceptors.GrpcHeadersInfo;
import com.bbva.gateway.utils.LogsTraces;
import com.bbva.orchestrator.configuration.ApplicationDataCache;
import com.bbva.orchestrator.core.mapper.model.CanonicalFields;
import com.bbva.orchestrator.core.enums.FilterOperator;
import com.bbva.orchestrator.core.enums.TransactionType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas completas para MonitoringBuilder")
class MonitoringBuilderTest {

    @Mock
    private ApplicationDataCache applicationDataCache;

    @InjectMocks
    private MonitoringBuilder monitoringService;

    private TransactionDTO transaction;
    private EnvironmentDTO environment;
    private ContextDTO context;
    private MockedStatic<GrpcHeadersInfo> grpcHeadersInfo;


    @BeforeEach
    void setUp() {
        grpcHeadersInfo = mockStatic(GrpcHeadersInfo.class);

        grpcHeadersInfo.when(GrpcHeadersInfo::getNetwork).thenReturn("networkTest");
        grpcHeadersInfo.when(GrpcHeadersInfo::getTraceId).thenReturn("trace123");
        grpcHeadersInfo.when(GrpcHeadersInfo::getPort).thenReturn("portTest");

        transaction = TransactionDTO.builder()
                .transactionId(TransactionIdDTO.builder().transactionReference("2134").build())
                .build();
        environment = EnvironmentDTO.builder()
                .terminal(TerminalDTO.builder().key("TERM01").build())
                .build();
        context = ContextDTO.builder()
                .pointOfServiceContext(PointOfServiceContextDTO.builder().ecommerceIndicator(false).build())
                .build();
    }


    @AfterEach
    void tearDown() {
        grpcHeadersInfo.close();
    }

    @Nested
    @DisplayName("Pruebas para el método build()")
    class BuildTests {

        @Test
        @DisplayName("Debería construir monitoreo para MTI 0100 exitosamente")
        void shouldBuildMonitoringForMti0100() {
            CanonicalFields iso8583_ = CanonicalFields.of(Map.of(
                    "messageType", "0100",
                    "networkName", "VISA",
                    "primaryAccountNumber", "4548811234567890",
                    "cardAcceptorNameLocation", "MI TIENDA GENIAL         LIMA         PE",
                    "acquiringInstitutionIdentificationCode", "111111",
                    "merchantType", "5411"
            ));

            transaction = TransactionDTO.builder().transactionType("SALE")
                    .transactionId(TransactionIdDTO.builder().transactionReference("1234").build())
                    .build();

            when(applicationDataCache.getBinDescription(anyString(), anyString())).thenReturn("VISA CLASSIC");
            when(applicationDataCache.getCustomValue(anyString(), anyString())).thenReturn("Supermercados");

            try (MockedStatic<FilterOperator> mockedFilter = mockStatic(FilterOperator.class);
                 MockedStatic<TransactionType> mockedTransaction = mockStatic(TransactionType.class)) {

                mockedFilter.when(() -> FilterOperator.getFilterOperator(any())).thenReturn("VENTA");
                mockedTransaction.when(() -> TransactionType.getTransactionType(any())).thenReturn("Venta Normal");

                MonitoringDTO result = monitoringService.build(iso8583_, transaction, environment, context);


                assertThat(result.getBinCode()).isEqualTo("454881");
                assertThat(result.getMerchantNameAceptor()).isEqualTo("MI TIENDA GENIAL");
            }
        }

        @Test
        @DisplayName("Debería retornar IsNextGen(false) para MTI no monitoreado (ej. 0800)")
        void shouldHandleNonMonitoredMti() {
            CanonicalFields iso8583_ = CanonicalFields.of(Map.of("messageType", "0800"));

            try (MockedStatic<LogsTraces> mockedLogs = mockStatic(LogsTraces.class)) {
                MonitoringDTO result = monitoringService.build(iso8583_, transaction, environment, context);
                assertThat(result.getIsNextGen()).isFalse();
                mockedLogs.verify(() -> LogsTraces.writeInfo(contains("No requiere generar bloque monitoreo")), times(1));
            }
        }

        @Test
        @DisplayName("Cobertura de Error en extractBinCode (PAN nulo)")
        void shouldHandleBinCodeError() {
            CanonicalFields iso8583_ = CanonicalFields.of(Map.of(
                    "messageType", "0100",
                    "primaryAccountNumber", "4548810000000000",
                    "cardAcceptorNameLocation", "NOMBRE VALIDO PARA EVITAR OTRO ERROR"
            ));




            MonitoringDTO result = monitoringService.build(iso8583_, transaction, environment, context);
            assertThat(result.getMerchantNameAceptor()).isEqualTo("NOMBRE VALIDO PARA EVI");

        }

        @Test
        void shouldHandleMerchantNameError() {
            CanonicalFields iso8583_ = CanonicalFields.of(Map.of(
                    "messageType", "0100",
                    "primaryAccountNumber", "4548810000000000",
                    "cardAcceptorNameLocation", "CORTO"
            ));

            MonitoringDTO result = monitoringService.build(iso8583_, transaction, environment, context);
            assertThat(result.getMerchantNameAceptor()).isEqualTo("MerchantName-NotFound");
        }

        @Test
        void shouldHandleMerchantNameEmpty() {
            CanonicalFields iso8583_ = CanonicalFields.of(Map.of(
                    "messageType", "0100",
                    "primaryAccountNumber", "4548810000000000"
            ));

            MonitoringDTO result = monitoringService.build(iso8583_, transaction, environment, context);
            assertThat(result.getMerchantNameAceptor()).isEqualTo("MerchantName-NotFound");
        }

        @Test
        @DisplayName("Debería asignar Approved/Denied para MTI_OUTPUT")
        void shouldHandleMtiOutputStatuses() {
            // Caso Approved (00)
            CanonicalFields isoApproved = CanonicalFields.of(Map.of("messageType", "0110", "responseCode", "00"));
            assertThat(monitoringService.build(isoApproved, transaction, environment, context).getTransactionStatus()).isEqualTo("Approved");

            // Caso Denied (51)
            CanonicalFields isoDenied = CanonicalFields.of(Map.of("messageType", "0110", "responseCode", "51"));
            assertThat(monitoringService.build(isoDenied, transaction, environment, context).getTransactionStatus()).isEqualTo("Denied");
        }
    }

    @Nested
    @DisplayName("Pruebas de Métodos de Utilidad")
    class UtilityTests {

        @Test
        @DisplayName("channelFilterDescription - Casos Varios")
        void testChannelFilter() {
            assertThat(MonitoringBuilder.channelFilterDescription(true, "KEY")).isEqualTo("ECOMMER");
            assertThat(MonitoringBuilder.channelFilterDescription(false, null)).isEqualTo("OTHER");
        }

        @Test
        @DisplayName("getCurrentDatePeru - Formato")
        void testGetDate() {
            String date = MonitoringBuilder.getCurrentDatePeru();
            assertThat(date).contains("-05:00");
        }
    }

    @Test
    @DisplayName("Lógica P2P completa con aislamiento de Logs")
    void shouldProcessP2PLogicFull() {
        CanonicalFields iso8583_ = CanonicalFields.of(Map.of(
                "messageType", "0100",
                "acquiringInstitutionIdentificationCode", "420829",
                "cardAcceptorNameLocation", "YAPE-Marilu Mejia O      Visa Direct  PE",
                "cardAcceptorIdentificationCode", "BANK123",
                "primaryAccountNumber", "4548810000000000"
        ));

        try (MockedStatic<LogsTraces> mockedLogs = mockStatic(LogsTraces.class)) {
            when(applicationDataCache.getCustomValue(anyString(), anyString())).thenReturn("BANCO_TEST");

            MonitoringDTO result = monitoringService.build(iso8583_, transaction, environment, context);

            assertThat(result.getP2pType()).isEqualTo(null);
            assertThat(result.getOriginBankDescription()).isEqualTo(null);
        }
    }

    @Test
    @DisplayName("Debería procesar correctamente la lógica P2P cuando el BIN Adquirente coincide")
    void shouldProcessP2PLogicSuccessfully() {
        // 1. Preparamos los datos de entrada
        CanonicalFields iso8583_ = CanonicalFields.of(Map.of(
                "messageType", "0100",
                "acquiringInstitutionIdentificationCode", "420829",
                "cardAcceptorNameLocation", "YAPE-Marilu Mejia O      Visa Direct  PE",
                "cardAcceptorIdentificationCode", "000123  ",
                "primaryAccountNumber", "4548810000000000",
                "merchantType", "5411",
                "networkName", "VISA"
        ));

        transaction = TransactionDTO.builder().transactionType("SALE")
                .transactionId(TransactionIdDTO.builder().transactionReference("1234").build())
                .build();

        // 2. Definimos los comportamientos de los Mocks
        when(applicationDataCache.getBinDescription(anyString(), anyString())).thenReturn("VISA DEBITO");
        when(applicationDataCache.getCustomValue("merchant_type", "5411")).thenReturn("SUPERMERCADO");

        // Este mock es el que valida la línea de originBankDescription dentro del IF P2P
        when(applicationDataCache.getCustomValue("bank_p2p", "000123")).thenReturn("BANCO DE PRUEBA");

        // 3. Ejecutamos el método
        MonitoringDTO result = monitoringService.build(iso8583_, transaction, environment, context);

        // 4. Verificaciones (Asserts)
        assertThat(result).isNotNull();

        // Verificación de lógica P2P
        assertThat(result.getP2pType()).isEqualTo("YAPE"); // Los primeros 4 caracteres
        assertThat(result.getOriginBankCode()).isEqualTo("000123"); // Verificamos el .trim()
        assertThat(result.getOriginBankDescription()).isEqualTo("BANCO DE PRUEBA");

        // Verificaciones generales para asegurar que no se rompió el resto del objeto
        assertThat(result.getBinCode()).isEqualTo("454881");
    }
}