package com.bbva.orchestrator.core.mapper.iso20022.strategy.impl;

import com.bbva.gateway.dto.iso20022.*;
import com.bbva.gateway.interceptors.GrpcHeadersInfo;
import com.bbva.orchestrator.configuration.ApplicationDataCache;
import com.bbva.orchestrator.configuration.ApplicationDataLocalCache;
import com.bbva.orchestrator.core.enums.CardholderVerificationCapability;
import com.bbva.orchestrator.core.exception.MapperFieldsException;
import com.bbva.orchestrator.core.mapper.model.CanonicalFields;
import com.bbva.orchestrator.core.utils.FieldUtil;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class EnvironmentMappingStrategyTest {

    @Mock
    private MapperUtil fieldService;

    @InjectMocks
    private EnvironmentMappingStrategy environmentStrategy;
    private MockedStatic<GrpcHeadersInfo> mockedHeaders;
    private MockedStatic<FieldUtil> mockedFieldUtils;
    private MockedStatic<CardholderVerificationCapability> mockedCardholderVerificationCapability;

    private static final String DEFAULT_EMPTY_VALUE = "";

    @BeforeEach
    void setUp() {
        mockedHeaders = mockStatic(GrpcHeadersInfo.class);
        mockedFieldUtils = mockStatic(FieldUtil.class);
        mockedCardholderVerificationCapability = mockStatic(CardholderVerificationCapability.class);
    }

    @AfterEach
    void tearDown() {
        mockedHeaders.close();
        mockedFieldUtils.close();
        mockedCardholderVerificationCapability.close();
    }

    @Test
    void map_shouldBuildCompleteEnvironmentDTO() {
        when(fieldService.convertFormatExpiryDate("1225")).thenReturn("2512");

        mockedCardholderVerificationCapability.when(() -> CardholderVerificationCapability.mapCardReadingCapability_Capability(any()))
                .thenReturn(Collections.emptyMap());
        Map<String, String> res = Map.of("OtherType", "value");
        mockedCardholderVerificationCapability.when(() -> CardholderVerificationCapability.mapPosTerminalLocation(any(), any(), any()))
                .thenReturn(res);

        CanonicalFields fields = CanonicalFields.of(Map.of(
                "primaryAccountNumber", "1234",
                "dateExpiration", "1225",
                "61.03", "1",
                "61.02", "2"
        ));

        EnvironmentDTO result = environmentStrategy.mapper(fields);

        assertNotNull(result);
        assertEquals("1234", result.getCard().getPan());
        assertEquals("2512", result.getCard().getExpiryDate());
    }

    @Test
    void unMapper_simplestTest() {
        EnvironmentDTO mockEnviroment = Mockito.mock(EnvironmentDTO.class, Mockito.RETURNS_DEEP_STUBS);

        Map<String, String> result = environmentStrategy.unMapper("PEER02", mockEnviroment);

        assertNull(result.get("primaryAccountNumber"));
    }

    @Test
    void shouldThrowMapperFieldsException_WhenRuntimeExceptionOccurs() {
        when(fieldService.convertFormatExpiryDate(any()))
                .thenThrow(new RuntimeException("Error al mapear EnvironmentDTO desde ISO8583"));

        CanonicalFields fields = CanonicalFields.of(Map.of("dateExpiration", "1225"));

        assertThatThrownBy(() -> environmentStrategy.mapper(fields))
                .isInstanceOf(MapperFieldsException.class)
                .hasMessage("java.lang.RuntimeException: Error al mapear EnvironmentDTO desde ISO8583")
                .extracting("code")
                .isEqualTo("PGWP-00121");
    }

    @Test
    void testUnMapper() {
        BusinessDataLoad businessDataLoad = new BusinessDataLoad();
        BusinessDataLocalLoad businessDataLocalLoad = new BusinessDataLocalLoad();

        ApplicationDataCache applicationDataCache = new ApplicationDataCache(businessDataLoad);
        ApplicationDataLocalCache applicationDataLocalCache = new ApplicationDataLocalCache(businessDataLocalLoad);

        String networkName = "PEER02";
        EnvironmentDTO environmentDTO = EnvironmentDTO.builder()
                .acceptor(AcceptorDTO.builder()
                        .id("ABC123TESTMTF19")
                        .localData(LocalDataDTO.builder()
                                .address(AddressDTO.builder().
                                        postalCode("63129-5210")
                                        .build())
                                .build())
                        .nameAndLocation("Merchant name          Merchant city PER")
                        .build())
                .acquirer(AcquirerDTO.builder()
                        .id("999901")
                        .country("")
                        .additionalId(AdditionalIdDTO.builder()
                                .key("postalCode")
                                .value("ABCD")
                                .build())
                        .build())
                .card(CardDTO.builder()
                        .pan("5193488224717069")
                        .expiryDate("2027-04-30")
                        .cardSequenceNumber("")
                        .serviceCode("")
                        .track1("")
                        .track2(Track2DTO.builder().textValue("").build())
                        .build())
                .issuer(IssuerDTO.builder()
                        .assigner("102510000660160463129-5210")
                        .build())
                .sender(SenderDTO.builder()
                        .id("009685")
                        .additionalId(AdditionalIdDTO.builder()
                                .key("additionalDataRetailer")
                                .value("E3F2F3F0F2F0F1F4F2F0F7F0F1F0F3F2F1F2F4F5F0F1F0F7F1F0F3F0F5E5F7F5F3F2F0F1F0F3F0F4F0F0F2F0F2F0F0F0F3F0F3F0F4F0F0F4F0F2F0F0F0F5F0F2F0F0F7F7F0F0F9F2F0F3F4F2F3")
                                .build())
                        .build())
                .terminal(TerminalDTO.builder()
                        .capabilities(CapabilitiesDTO.builder()
                                .cardCaptureCapable(false)
                                .cardholderVerificationCapabilities(List.of(CardholderVerificationCapabilityDTO.builder()
                                                .capability("UNSP")
                                        .build()))
                                .cardReadingCapabilities(List.of(CardReadingCapabilityDTO.builder()
                                                .capability("KEEN")
                                        .build()))
                            .build())
                        .key("OTHN")
                        .offPremisesIndicator(true)
                        .otherType("Cardholder terminal")
                        .terminalId(TerminalIdDTO.builder()
                                .id("MTF TEST")
                                .assigner("")
                                .country("604")
                                .build())
                        .build())
                .build();

        EnvironmentMappingStrategy strategy = new EnvironmentMappingStrategy(new MapperUtil(applicationDataCache, applicationDataLocalCache));

        Map<String, String> result = strategy.unMapper(networkName, environmentDTO);

        assertNotNull(result);
    }

    @Test
    void map_shouldBuildCompleteEnvironmentDTO_response() {
        when(fieldService.convertFormatExpiryDate("1225")).thenReturn("2512");

        mockedCardholderVerificationCapability.when(() -> CardholderVerificationCapability.mapCardReadingCapability_Capability(any()))
                .thenReturn(Collections.emptyMap());
        Map<String, String> res = Map.of("OtherType", "value");
        mockedCardholderVerificationCapability.when(() -> CardholderVerificationCapability.mapPosTerminalLocation(any(), any(), any()))
                .thenReturn(res);

        CanonicalFields fields = CanonicalFields.of(Map.of(
                "primaryAccountNumber", "1234",
                "dateExpiration", "1225",
                "61.03", "1",
                "61.02", "2"
        ));

        EnvironmentDTO result = environmentStrategy.mapper(fields);

        assertNotNull(result);
        assertEquals("1234", result.getCard().getPan());
        assertEquals("2512", result.getCard().getExpiryDate());
    }
}
