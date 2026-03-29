package com.bbva.orchestrator.core.mapper.iso20022.strategy.impl;

import com.bbva.gateway.dto.iso20022.*;
import com.bbva.gateway.interceptors.GrpcHeadersInfo;
import com.bbva.orchestrator.configuration.ApplicationDataCache;
import com.bbva.orchestrator.configuration.ApplicationDataLocalCache;
import com.bbva.orchestrator.core.dto.ISO8583;
import com.bbva.orchestrator.core.enums.CardholderVerificationCapability;
import com.bbva.orchestrator.core.exception.MapperFieldsException;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class EnvironmentMappingStrategyTest {

    @Mock
    private MapperUtil fieldService;
    @Mock
    private ISO8583 mockInput;

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
        when(mockInput.getMessageType()).thenReturn("0100");
        when(mockInput.getPrimaryAccountNumber()).thenReturn("1234");
        when(mockInput.getDateExpiration()).thenReturn("1225");
        when(fieldService.convertFormatExpiryDate("1225")).thenReturn("2512");

        // Usar valores específicos en lugar de any()
        Map<String, String> res = Map.of("OtherType", "value");
        Map<String, String> subfields = Map.of("61.03", "1", "61.02", "2");

        // Usar valores específicos en lugar de any()
        mockedCardholderVerificationCapability.when(() -> CardholderVerificationCapability.mapPosTerminalLocation(anyString(), anyString(), anyString()))
                .thenReturn(res);

        EnvironmentDTO result = environmentStrategy.mapper(mockInput, subfields);

        assertNotNull(result);
        assertEquals("1234", result.getCard().getPan());
        assertEquals("2512", result.getCard().getExpiryDate());
    }

    @Test
    void unMapper_simplestTest() {

        EnvironmentDTO mockEnviroment = Mockito.mock(EnvironmentDTO.class, Mockito.RETURNS_DEEP_STUBS);

        Map<String, String> result = environmentStrategy.unMapper("PEER02",mockEnviroment);

        assertNull(result.get("primaryAccountNumber"));
    }

    @Test
    void shouldThrowMapperFieldsException_WhenRuntimeExceptionOccurs() {
        // GIVEN
        ISO8583 inputMock = mock(ISO8583.class);
        Map<String, String> subFields = new HashMap<>();

        // Simulamos que el input lanza una excepción al acceder a un dato
        when(inputMock.getTrackTwoData()).thenThrow(new RuntimeException("Error al mapear EnvironmentDTO desde ISO8583"));

        // WHEN & THEN
        assertThatThrownBy(() -> environmentStrategy.mapper(inputMock, subFields))
                .isInstanceOf(MapperFieldsException.class)
                .hasMessage("java.lang.RuntimeException: Error al mapear EnvironmentDTO desde ISO8583")
                .extracting("code") // Asumiendo que tu excepción tiene un campo 'code'
                .isEqualTo("PGWP-00121");
    }

    @Test
    void testUnMapper() {

        BusinessDataLoad businessDataLoad = new BusinessDataLoad();
        BusinessDataLocalLoad businessDataLocalLoad = new BusinessDataLocalLoad();

        ApplicationDataCache applicationDataCache = new ApplicationDataCache(businessDataLoad);
        ApplicationDataLocalCache applicationDataLocalCache = new ApplicationDataLocalCache(businessDataLocalLoad);

        // Arrange
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

        // Act
        Map<String, String> result = strategy.unMapper(networkName, environmentDTO);

        // Assert
        assertNotNull(result);
    }

    @Test
    void map_shouldBuildCompleteEnvironmentDTO_response() {
        when(mockInput.getMessageType()).thenReturn("0110");
        when(mockInput.getPrimaryAccountNumber()).thenReturn("1234");
        when(mockInput.getDateExpiration()).thenReturn("1225");
        when(fieldService.convertFormatExpiryDate("1225")).thenReturn("2512");

        // Usar valores específicos en lugar de any()
        Map<String, String> res = Map.of("OtherType", "value");
        Map<String, String> subfields = Map.of("61.03", "1", "61.02", "2");

        // Usar valores específicos en lugar de any()
        mockedCardholderVerificationCapability.when(() -> CardholderVerificationCapability.mapPosTerminalLocation(anyString(), anyString(), anyString()))
                .thenReturn(res);

        EnvironmentDTO result = environmentStrategy.mapper(mockInput, subfields);

        assertNotNull(result);
        assertEquals("1234", result.getCard().getPan());
        assertEquals("2512", result.getCard().getExpiryDate());
    }
}