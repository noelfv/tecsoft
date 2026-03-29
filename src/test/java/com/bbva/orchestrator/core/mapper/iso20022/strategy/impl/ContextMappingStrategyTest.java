package com.bbva.orchestrator.core.mapper.iso20022.strategy.impl;

import com.bbva.gateway.dto.iso20022.ContextDTO;
import com.bbva.gateway.interceptors.GrpcHeadersInfo;
import com.bbva.orchestrator.core.exception.MapperFieldsException;
import com.bbva.orchestrator.core.mapper.model.CanonicalFields;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContextMappingStrategyTest {

    @Mock
    private MapperUtil mapperUtil;

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
        Map<String, String> map = new HashMap<>();
        map.put("settlementDate", "0814");
        map.put("merchantType", "5999");
        map.put("pinData", "PIN_DATA");
        map.put("campaignData", "CAMPAIGN_DATA");
        map.put("22.01", "00");
        map.put("60.01", "2");
        map.put("62.10", "1");
        CanonicalFields fields = CanonicalFields.of(map);

        lenient().when(mapperUtil.channelECommerceIndicator(any(), any(), any())).thenReturn(false);
        lenient().when(mapperUtil.channelTPVIndicator(any(CanonicalFields.class))).thenReturn("UNSP");
        lenient().when(mapperUtil.entryModeValue(any(CanonicalFields.class), any())).thenReturn("UNSP");
        lenient().when(mapperUtil.operationTypeValue(any())).thenReturn("AUTH");
        lenient().when(mapperUtil.channelValue(any(), any(), any())).thenReturn("POS");
        lenient().when(mapperUtil.ownerValue(any())).thenReturn("ONUS");

        ContextDTO result = contextMappingStrategy.mapper(fields);

        assertNotNull(result);
        assertNotNull(result.getTransactionContext());
        assertEquals("5999", result.getTransactionContext().getMerchantCategoryCode());
        assertEquals("0814", result.getTransactionContext().getSettlementService().getSettlementServiceDates().getSettlementDate());
        assertEquals(4, result.getTransactionContext().getAdditionalData().size());

        assertNotNull(result.getPointOfServiceContext());

        assertNotNull(result.getVerification());
        assertEquals("PIN_DATA", result.getVerification().get(0).getVerificationInformation().get(0).getValue().getPinData().getEncryptedPINBlock());

        assertNotNull(result.getSaleContext());
        assertEquals("CAMPAIGN_DATA", result.getSaleContext().getAdditionalData().get(0).getValue());
    }

    @Test
    void unMapper_simplestTest() {
        ContextDTO mockContext = Mockito.mock(ContextDTO.class, Mockito.RETURNS_DEEP_STUBS);

        Map<String, String> result = contextMappingStrategy.unMapper("PEER02", mockContext);

        assertNull(result.get("merchantType"));
    }

    @Test
    void map_shouldBuildCompleteContextDTO_response() {
        CanonicalFields fields = CanonicalFields.of(Map.of());
        ContextDTO result = contextMappingStrategy.mapperResponse(fields);

        assertNotNull(result);
        assertNotNull(result.getTransactionContext().getTransactionInitiator(), "0000");
    }

    @Test
    void shouldThrowMapperFieldsException_WhenRuntimeExceptionOccurs() {
        // GIVEN — force exception via mapperUtil
        when(mapperUtil.channelECommerceIndicator(any(), any(), any()))
                .thenThrow(new RuntimeException("Error al mapear ContextDTO desde ISO8583"));

        CanonicalFields fields = CanonicalFields.of(Map.of());

        // WHEN & THEN
        assertThatThrownBy(() -> contextMappingStrategy.mapper(fields))
                .isInstanceOf(MapperFieldsException.class)
                .hasMessage("java.lang.RuntimeException: Error al mapear ContextDTO desde ISO8583")
                .extracting("code")
                .isEqualTo("PGWP-00121");
    }
}
