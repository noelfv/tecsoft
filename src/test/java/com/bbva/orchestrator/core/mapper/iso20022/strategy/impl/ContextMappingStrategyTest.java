package com.bbva.orchestrator.core.mapper.iso20022.strategy.impl;

import com.bbva.gateway.dto.iso20022.AdditionalDataDTO;
import com.bbva.gateway.dto.iso20022.ContextDTO;
import com.bbva.gateway.dto.iso20022.PINDataDTO;
import com.bbva.gateway.dto.iso20022.PointOfServiceContextDTO;
import com.bbva.gateway.dto.iso20022.ReconciliationDTO;
import com.bbva.gateway.dto.iso20022.ResultDetailsDTO;
import com.bbva.gateway.dto.iso20022.SaleContextDTO;
import com.bbva.gateway.dto.iso20022.SettlementServiceDTO;
import com.bbva.gateway.dto.iso20022.SettlementServiceDatesDTO;
import com.bbva.gateway.dto.iso20022.TransactionContextDTO;
import com.bbva.gateway.dto.iso20022.ValueDTO;
import com.bbva.gateway.dto.iso20022.VerificationDTO;
import com.bbva.gateway.dto.iso20022.VerificationInformationDTO;
import com.bbva.gateway.dto.iso20022.VerificationResultDTO;
import com.bbva.gateway.interceptors.GrpcHeadersInfo;
import com.bbva.orchestrator.configuration.ApplicationDataCache;
import com.bbva.orchestrator.configuration.ApplicationDataLocalCache;
import com.bbva.orchestrator.core.dto.ISO8583;
import com.bbva.orchestrator.core.exception.MapperFieldsException;
import com.bbva.orchestrator.core.utils.MapperUtil;
import com.bbva.orchlib.configuration.BusinessDataLoad;
import com.bbva.orchlib.configuration.BusinessDataLocalLoad;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContextMappingStrategyTest {

    @Mock
    private MapperUtil fieldService;
    @Mock
    private MapperUtil processMonitoringService;
    @Mock
    private ISO8583 mockInput;

    @InjectMocks
    private ContextMappingStrategy contextMappingStrategy;

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
    void map_shouldBuildCompleteContextDTO() {
        when(mockInput.getSettlementDate()).thenReturn("0814");
        when(mockInput.getMerchantType()).thenReturn("5999");
        when(mockInput.getPinData()).thenReturn("PIN_DATA");
        when(mockInput.getCampaignData()).thenReturn("CAMPAIGN_DATA");


        lenient().when(processMonitoringService.operationTypeValue(any())).thenReturn("AUTH");
        lenient().when(processMonitoringService.channelValue(any(), any(),any())).thenReturn("POS");

        Map<String, String> subFileds = Map.of(
                "22.01", "00",
                "60.01", "2",
                "62.10", "1"
        );
        ContextDTO result = contextMappingStrategy.mapper(mockInput, subFileds);

        assertNotNull(result);

        assertNotNull(result.getTransactionContext());
        assertEquals("5999", result.getTransactionContext().getMerchantCategoryCode());
        assertNull(result.getTransactionContext().getCaptureDate());
        assertNull(result.getTransactionContext().getReconciliation().getDate());
        assertEquals("0814", result.getTransactionContext().getSettlementService().getSettlementServiceDates().getSettlementDate());
        assertEquals(4, result.getTransactionContext().getAdditionalData().size());

        assertNotNull(result.getPointOfServiceContext());
        assertEquals("UNSP", result.getPointOfServiceContext().getCardDataEntryMode());

        assertNotNull(result.getVerification());
        assertEquals("PIN_DATA", result.getVerification().get(0).getVerificationInformation().get(0).getValue().getPinData().getEncryptedPINBlock());

        assertNotNull(result.getSaleContext());
        assertEquals("CAMPAIGN_DATA", result.getSaleContext().getAdditionalData().get(0).getValue());
    }


    @Test
    void unMapper_simplestTest() {

        ContextDTO mockContext = Mockito.mock(ContextDTO.class, Mockito.RETURNS_DEEP_STUBS);

        Map<String, String> result = contextMappingStrategy.unMapper("PEER02",mockContext);

        assertNull(result.get("merchantType"));
    }

    @Test
    void map_shouldBuildCompleteContextDTO_response() {

        Map<String, String> subFileds = Map.of();
        ContextDTO result = contextMappingStrategy.mapperResponse(mockInput);

        assertNotNull(result);

        assertNotNull(result.getTransactionContext().getTransactionInitiator(), "0000");
    }

    @Test
    void shouldThrowMapperFieldsException_WhenRuntimeExceptionOccurs() {
        // GIVEN
        ISO8583 inputMock = mock(ISO8583.class);
        Map<String, String> subFields = new HashMap<>();

        // Simulamos que el input lanza una excepción al acceder a un dato
        when(inputMock.getCaptureDate()).thenThrow(new RuntimeException("Error al mapear ContextDTO desde ISO8583"));

        // WHEN & THEN
        assertThatThrownBy(() -> contextMappingStrategy.mapper(inputMock, subFields))
                .isInstanceOf(MapperFieldsException.class)
                .hasMessage("java.lang.RuntimeException: Error al mapear ContextDTO desde ISO8583")
                .extracting("code") // Asumiendo que tu excepción tiene un campo 'code'
                .isEqualTo("PGWP-00121");
    }
}