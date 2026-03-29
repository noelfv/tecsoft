package com.bbva.orchestrator.core.utils;

import com.bbva.gateway.dto.iso20022.AdditionalIdDTO;
import com.bbva.gateway.utils.LogsTraces;
import com.bbva.orchestrator.configuration.ApplicationDataCache;
import com.bbva.orchestrator.configuration.ApplicationDataLocalCache;
import com.bbva.orchestrator.core.mapper.model.CanonicalFields;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MapperUtilTest {

    @Mock
    private ApplicationDataCache applicationDataCache;

    @Mock
    private ApplicationDataLocalCache applicationDataLocalCache;

    @InjectMocks
    private MapperUtil service;

    private MockedStatic<LogsTraces> mockedLogsTraces;

    @BeforeEach
    void setUp() {
        mockedLogsTraces = mockStatic(LogsTraces.class);
    }

    @AfterEach
    void tearDown() {
        mockedLogsTraces.close();
    }

    @Test
    void testParseDouble_Valid() {
        assertEquals(123.45, service.convertAmountDouble("123.45"));
    }

    @Test
    void testParseDouble_Invalid() {
        assertNull(service.convertAmountDouble("invalid"));
    }

    @Test
    void testIsNullOrEmptySubstring_Valid() {
        assertEquals("test", service.isNullOrEmptySubstring("testString", 0, 4));
    }

    @Test
    void testIsNullOrEmptySubstring_Invalid() {
        assertNull(service.isNullOrEmptySubstring(null, 0, 4));
    }

    @Test
    void testConvertFormatDateTime_Valid() {
        assertEquals("2026-01-01T00:00:00.000Z", service.convertFormatDateTime("0101000000"));
    }

    @Test
    void testConversionRateValidation_Valid() {
        assertEquals(1.23, service.conversionRateValidation("1.23"));
    }

    @Test
    void testConversionRateValidation_Invalid() {
        assertNull(service.conversionRateValidation("invalid"));
    }

    @Test
    void testConvertFormatExpiryDate_Valid() {
        assertEquals("2025-04-30", service.convertFormatExpiryDate("2504"));
    }

    @Test
    void testConvertFormatExpiryDate_Invalid() {
        assertNull(service.convertFormatExpiryDate("invalid"));
    }

    @Test
    void testChannelTPVIndicator_Invalid() {
        CanonicalFields fields = CanonicalFields.of(Map.of());
        assertNotNull(service.channelTPVIndicator(fields));
    }

    @Test
    void shouldReturnPOSTWhenSubfieldsAreValid() {
        Map<String, String> map = new HashMap<>();
        map.put("networkName", "peer01");
        map.put("merchantType", "CUALQUIERA");
        map.put("22.01", "05");
        map.put("60.01", "3");
        map.put("60.02", "1");
        CanonicalFields fields = CanonicalFields.of(map);

        String result = service.channelTPVIndicator(fields);

        assertEquals("POST", result);
    }

    @Test
    void shouldReturnATMTWhenConditionsMatch() {
        Map<String, String> map = new HashMap<>();
        map.put("networkName", "peer01");
        map.put("merchantType", "6011");
        map.put("03.01", "01");
        CanonicalFields fields = CanonicalFields.of(map);

        String result = service.channelTPVIndicator(fields);

        assertEquals("ATMT", result);
    }

    @Test
    void shouldReturnOTHPWhenConditionsMatch() {
        Map<String, String> map = new HashMap<>();
        map.put("networkName", "peer01");
        map.put("merchantType", "6010");
        map.put("03.01", "01");
        CanonicalFields fields = CanonicalFields.of(map);

        String result = service.channelTPVIndicator(fields);

        assertEquals("OTHP", result);
    }

    @Test
    void shouldReturnUNSPDefault() {
        Map<String, String> map = new HashMap<>();
        map.put("networkName", "peer01");
        map.put("merchantType", "XXXX");
        map.put("03.01", "99");
        CanonicalFields fields = CanonicalFields.of(map);

        String result = service.channelTPVIndicator(fields);

        assertEquals("UNSP", result);
    }

    @Test
    void shouldReturnUNSPWhenMerchantTypeIsNull() {
        Map<String, String> map = new HashMap<>();
        map.put("networkName", "peer01");
        // merchantType absent → getOrDefault returns null
        map.put("03.01", "01");
        CanonicalFields fields = CanonicalFields.of(map);

        String result = service.channelTPVIndicator(fields);

        assertEquals("UNSP", result);
    }

    @Test
    void shouldSkipTPVWhenAFieldIsMissing() {
        Map<String, String> map = new HashMap<>();
        map.put("networkName", "peer01");
        map.put("merchantType", "6011");
        map.put("22.01", "05");
        map.put("60.01", "3");
        // Falta 60.02, por lo tanto no entra en TPV
        map.put("03.01", "01");
        CanonicalFields fields = CanonicalFields.of(map);

        String result = service.channelTPVIndicator(fields);

        assertEquals("ATMT", result);
    }

    @Test
    void generateSpecialProgrammeQualificationDetailNameValid(){
        assertEquals("mastercard_promotion_code", service.generateSpecialProgrammeQualificationDetailName("peer02"));
        assertEquals("mastercard_promotion_code", service.generateSpecialProgrammeQualificationDetailName("PEER02"));
        assertNull( service.generateSpecialProgrammeQualificationDetailName("peer01"));
        assertNull(service.generateSpecialProgrammeQualificationDetailName(null));
    }
    @Test
    void testDefaultIfEmpty() {
        assertEquals("default", service.defaultIfEmpty("", "default"));
    }

    @Test
    void testGetFieldValue_Valid() {
        assertEquals("value", service.getFieldValue("value", String::toString, "default"));
    }

    @Test
    void testGetFieldValue_Null() {
        assertEquals("default", service.getFieldValue(null, String::toString, "default"));
    }

    @Test
    void testGetAdditionalDataValue_MatchingKey() {
        AdditionalIdDTO additionalData = mock(AdditionalIdDTO.class);
        when(additionalData.getKey()).thenReturn("key");
        when(additionalData.getValue()).thenReturn("value");

        String result = service.getAdditionalDataValue(additionalData, "key", "default");
        assertEquals("value", result);
    }

    @Test
    void testGetAdditionalDataValue_NonMatchingKey() {
        AdditionalIdDTO additionalData = mock(AdditionalIdDTO.class);
        when(additionalData.getKey()).thenReturn("key");
//        when(additionalData.getValue()).thenReturn("value");

        String result = service.getAdditionalDataValue(additionalData, "wrongKey", "default");
        assertEquals("default", result);
    }

    @Test
    void testGetAdditionalDataValue_NullAdditionalData() {
        String result = service.getAdditionalDataValue(null, "key", "default");
        assertEquals("default", result);
    }

    @Test
    void testGetAdditionalDataValue_EmptyValue() {
        AdditionalIdDTO additionalData = mock(AdditionalIdDTO.class);
        when(additionalData.getKey()).thenReturn("key");
        when(additionalData.getValue()).thenReturn("");

        String result = service.getAdditionalDataValue(additionalData, "key", "default");
        assertEquals("default", result);
    }

    @Test
    void testFindValueInAdditionalData_MatchingKey() {
        AdditionalIdDTO additionalData = mock(AdditionalIdDTO.class);
        when(additionalData.getKey()).thenReturn("key");
        when(additionalData.getValue()).thenReturn("value");

        List<AdditionalIdDTO> additionalDataList = List.of(additionalData);
        String result = service.findValueInAdditionalData(additionalDataList, "key", "default");
        assertEquals("value", result);
    }

    @Test
    void testFindValueInAdditionalData_NonMatchingKey() {
        AdditionalIdDTO additionalData = mock(AdditionalIdDTO.class);
        when(additionalData.getKey()).thenReturn("key");
//        when(additionalData.getValue()).thenReturn("value");

        List<AdditionalIdDTO> additionalDataList = List.of(additionalData);
        String result = service.findValueInAdditionalData(additionalDataList, "wrongKey", "default");
        assertEquals("default", result);
    }

    @Test
    void testFindValueInAdditionalData_NullList() {
        String result = service.findValueInAdditionalData(null, "key", "default");
        assertEquals("default", result);
    }

    @Test
    void testFindValueInAdditionalData_EmptyList() {
        List<AdditionalIdDTO> additionalDataList = List.of();
        String result = service.findValueInAdditionalData(additionalDataList, "key", "default");
        assertEquals("default", result);
    }

    @Test
    void testReConvertFormatDateTime_Valid() {
        // "2025-01-01T00:00:00.000Z" -> "0101000000"
        assertEquals("0101000000", service.reConvertFormatDateTime("2025-01-01T00:00:00.000Z"));
    }

    @Test
    void testReConvertFormatDateTime_Invalid() {
        assertNull(service.reConvertFormatDateTime("invalid"));
    }

    @Test
    void testReConvertFormatExpiryDate_Valid() {
        // "2025-04" -> "2504"
        assertEquals("2504", service.reConvertFormatExpiryDate("2025-04-30"));
    }

    @Test
    void testReConvertFormatExpiryDate_Invalid() {
        assertNull(service.reConvertFormatExpiryDate("invalid"));
    }

    @Test
    void testCreateTransactionReference_PEER01() {
        CanonicalFields fields = CanonicalFields.of(Map.of(
                "systemTraceAuditNumber", "11",
                "acquiringInstitutionIdentificationCode", "32",
                "retrievalReferenceNumber", "37",
                "cardAcceptorTerminalIdentification", "41",
                "transmissionDateTime", "00"
        ));
        String result = service.createTransactionReference(fields);
        assertEquals("0011323741", result);
    }

    @Test
    void testCreateTransactionReference_PEER02() {
        CanonicalFields fields = CanonicalFields.of(Map.of(
                "systemTraceAuditNumber", "11",
                "acquiringInstitutionIdentificationCode", "32",
                "retrievalReferenceNumber", "37",
                "cardAcceptorTerminalIdentification", "41",
                "transmissionDateTime", "00"
        ));

        String result = service.createTransactionReference(fields);

        assertEquals("0011323741", result);
    }

    @Test
    void testGetObjectValue_Valid() {
        assertEquals(123, service.getObjectValue("123", Integer::valueOf, 0));
    }

    @Test
    void testGetFieldValueDouble_Valid() {
        class Dummy { Double getVal() { return 1.5; } }
        Dummy dummy = new Dummy();
        assertEquals("1.5", service.getFieldValueDouble(dummy, Dummy::getVal, "default"));
    }

    @Test
    void testGetFieldValueDouble_Null() {
        class Dummy { Double getVal() { return null; } }
        Dummy dummy = new Dummy();
        assertEquals("default", service.getFieldValueDouble(dummy, Dummy::getVal, "default"));
    }

    @Test
    void testConvertCurrencyIdToCurrencyCode() {
        when(applicationDataCache.getCurrencyCode("604")).thenReturn("PEN");
        assertEquals("PEN", service.convertCurrencyIdToCurrencyCode("604"));
    }

    @Test
    void testConvertCurrencyCodeToCurrencyId() {
        when(applicationDataCache.getCurrencyCode("PEN")).thenReturn("604");
        assertEquals("604", service.convertCurrencyCodeToCurrencyId("PEN"));
    }

    @Test
    void testGetBinDescription() {
        when(applicationDataCache.getBinDescription("network", "bin")).thenReturn("desc");
        assertEquals("desc", service.getBinDescription("network", "bin"));
    }

    @Test
    void testConvertResultDataToResponseCode() {
        when(applicationDataLocalCache.getCustomValue("net", "response_code", "resultData")).thenReturn("respCode");
        assertEquals("respCode", service.convertLabelDataToResponseCode("net", "resultData"));
    }

    @Test
    void testConvertResponseCodeToResultData() {
        when(applicationDataLocalCache.getCustomValue("net", "response_code", "respCode")).thenReturn("resultData");
        assertEquals("resultData", service.convertResponseCodeToLabelData("net", "respCode"));
    }

    @Test
    void testConvertEffectiveExchangeRate() {
        assertEquals("208.1404", service.convertEffectiveExchangeRate("42081404"));
    }

    @Test
    void testConvertConversionRate() {
        assertEquals("42081404", service.convertConversionRate("208.1404"));
    }

    @Test
    void testOperationTypeValue() {
        // Given
        String messageType = "0100";
        String transactionType = "00";
        String expected = "PURCHASE";

        // When
        String result = service.operationTypeValue(transactionType);

        // Then
        assertEquals(expected, result);
    }

    @Test
    void testChannelValue_returnsEcommerceWhenIndicatorIsTrue() {
        // When
        String result = service.channelValue( true,"ECOMMER" ,"ANY_TERMINAL");
        // Then
        assertEquals("ECOMMER", result);
    }

    @Test
    void test_validValue() {

        String resultEmpty = service.validValue("  ");
        String resultNull = service.validValue(null);
        String resultValid = service.validValue(" valid ");

        assertNull(resultEmpty);
        assertNull(resultNull);
        assertEquals(" valid ", resultValid);
    }

    @Test
    void testChannelValue_returnsAtmWhenTerminalKeyStartsWithATM() {
        // When
        String result = service.channelValue( false,"VALUE", "ATM12345");
        // Then
        assertEquals("ATM", result);
    }

    @Test
    void testChannelValue_returnsAtmWhenTerminalKeyTPVIndicator_ATMT() {
        // When
        String result = service.channelValue( false,"ATMT", "ATM12345");
        // Then
        assertEquals("ATM", result);
    }

    @Test
    void testChannelValue_returnsPosAsDefault() {
        // When
        String resultPos = service.channelValue(false, "POST" ,"TERM01");
        String resultNulls = service.channelValue( null, "UNSP",null);

        // Then
        assertEquals("POS", resultPos);
        assertEquals("POS", resultNulls);
    }

    @Test
    void testConvertAmountString() {
        // Given
        Double amount = 123.45;
        // The method should remove the decimal point and pad with leading zeros to a length of 12
        String expected = "000000012345";

        // When
        String result = service.convertAmountString(amount);

        // Then
        assertEquals(expected, result);
    }



    @Test
    void testSafeBooleanValueOf() {
        // When & Then
        assertTrue(service.safeBooleanValueOf("true"));
        assertFalse(service.safeBooleanValueOf("false"));
        assertFalse(service.safeBooleanValueOf("any other string"));
        assertNull(service.safeBooleanValueOf(null));
    }

    @Test
    void testChannelECommerceIndicator_peer02_True() {
        Map<String, String> subFields = new HashMap<>();
        subFields.put("48.01", "T");
        subFields.put("22.01", "10");
        subFields.put("48.42", "0103212");
        subFields.put("61.04", "4");
        subFields.put("61.05", "1");
        subFields.put("61.10", "6");

        Boolean result = service.channelECommerceIndicator("peer02", subFields, null);
        assertTrue(result);
    }

    @Test
    void testChannelECommerceIndicator_peer02_False() {
        Map<String, String> subFields = new HashMap<>();

        Boolean result = service.channelECommerceIndicator("peer02", subFields, null);
        assertFalse(result);
    }

    @Test
    void testChannelECommerceIndicator_notPeer02() {
        Map<String, String> subFields = Map.of("key", "value");
        Boolean result = service.channelECommerceIndicator("peer01", subFields, null);
        assertNotNull(result);
    }

    @Test
    void testChannelECommerceIndicator_peer01_True() {
        Map<String, String> subFields = new HashMap<>();
        subFields.put("60.08", "05");

        Boolean result = service.channelECommerceIndicator("peer01", subFields, "59");
        assertTrue(result);
    }

    @Test
    void testChannelECommerceIndicator_peer01_False() {
        Map<String, String> subFields = new HashMap<>();
        subFields.put("60.08", "05");

        Boolean result = service.channelECommerceIndicator("peer01", subFields, "61");
        assertFalse(result);
    }

    @Test
    void testChannelECommerceIndicator_peer01_False_nullvalue() {
        Map<String, String> subFields = new HashMap<>();
        subFields.put("60.08", "05");

        Boolean result = service.channelECommerceIndicator("peer01", subFields, null);
        assertFalse(result);
    }

    @Test
    void testChannelECommerceIndicator_peer01_False_nullvalue_1() {
        Map<String, String> subFields = new HashMap<>();
        subFields.put("60.08", null);

        Boolean result = service.channelECommerceIndicator("peer01", subFields, "12");
        assertFalse(result);
    }

    @Test
    void testChannelECommerceIndicator_peer01_False_badSubfield() {
        Map<String, String> subFields = new HashMap<>();
        subFields.put("60.08", "10");

        Boolean result = service.channelECommerceIndicator("peer01", subFields, "59");
        assertFalse(result);
    }

    @Test
    void testOperationTypeValue_Cases() {
        // Casos definidos
        assertEquals("WTHDMON", service.operationTypeValue("01"));
        assertEquals("REFUND", service.operationTypeValue("20"));

        // Caso null
        assertNull(service.operationTypeValue(null));

        // Caso default (valor no mapeado)
        assertNull(service.operationTypeValue("99"));
    }

    @Test
    void testGenerateTransactionReferenceFromSeed() {
        String seed = "test-seed-value-for-uuid-generation";
        String result = service.generateTransactionReferenceFromSeed(seed);

        assertNotNull(result);
        // UUID tiene 36 caracteres, el método corta en 35
        assertEquals(35, result.length());
    }

    @Test
    void testOwnerValue() {
        assertEquals("OFFUS", service.ownerValue("RETV"));
        assertEquals("ONUS", service.ownerValue("ATM"));
        assertEquals("ONUS", service.ownerValue("POS"));
    }

    @Test
    void testEntryModeValue_Peer02() {
        CanonicalFields fields = CanonicalFields.of(Map.of("networkName", "peer02"));
        // Nota: MastercardAxisOperator.entryModeIndicator puede retornar null, cubre la línea.
        service.entryModeValue(fields, "010");
    }

    @Test
    void testEntryModeValue_OtherNetwork() {
        CanonicalFields fields = CanonicalFields.of(Map.of("networkName", "peer01"));
        service.entryModeValue(fields, "010");
    }

    @Test
    void testElectronicCommerceIndicators_Peer02() {
        Map<String, String> subFields = new HashMap<>();
        // Ejecuta la rama del if
        service.electronicCommerceIndicators("peer02", subFields);
    }

    @Test
    void testElectronicCommerceIndicators_OtherNetwork() {
        Map<String, String> subFields = new HashMap<>();
        // Ejecuta la rama del else
        service.electronicCommerceIndicators("peer01", subFields);
    }

    @Test
    void testSecurityLevelECI_Peer02() {
        // Ejecuta la rama del if
        service.securityLevelECI("peer02", "0534321");
    }

    @Test
    void testSecurityLevelECI_OtherNetwork() {
        // Ejecuta la rama del else
        service.securityLevelECI("peer01", "05");
    }

    @Test
    void testSafeSubstring_NullOrEmpty() {
        assertNull(service.safeSubstring(null, 0, 5));
        assertEquals("", service.safeSubstring("", 0, 5));
    }

    @Test
    void testSafeSubstring_NegativeBeginIndex() {
        // Debe corregir beginIndex a 0
        assertEquals("Tes", service.safeSubstring("Test", -1, 3));
    }

    @Test
    void testSafeSubstring_EndIndexOverflow() {
        // Debe corregir endIndex al length
        assertEquals("Test", service.safeSubstring("Test", 0, 100));
    }

    @Test
    void testSafeSubstring_BeginGreaterOrEqualEnd() {
        // Debe retornar vacío
        assertEquals("", service.safeSubstring("Test", 3, 2)); // Begin > End
        assertEquals("", service.safeSubstring("Test", 2, 2)); // Begin == End
    }

    @Test
    void testSafeSubstring_NormalCase() {
        assertEquals("es", service.safeSubstring("Test", 1, 3));
    }

    @Test
    void testChannelTPVIndicator_Peer02() {
        CanonicalFields fields = CanonicalFields.of(Map.of("networkName", "peer02", "merchantType", "5411"));
        service.channelTPVIndicator(fields);
    }

    @Test
    void testChannelTPVIndicator_OtherNetwork() {
        CanonicalFields fields = CanonicalFields.of(Map.of("networkName", "peer01", "merchantType", "5411"));
        service.channelTPVIndicator(fields);
    }

    // ===================== isOutputMti =====================

    @Test
    void testIsOutputMti_True() {
        assertTrue(service.isOutputMti("0110"));
        assertTrue(service.isOutputMti("0130"));
        assertTrue(service.isOutputMti("0410"));
        assertTrue(service.isOutputMti("0430"));
        assertTrue(service.isOutputMti("0120"));
        assertTrue(service.isOutputMti("0420"));
        assertTrue(service.isOutputMti("0312"));
    }

    @Test
    void testIsOutputMti_False() {
        assertFalse(service.isOutputMti("0100"));
        assertFalse(service.isOutputMti("0200"));
    }

    @Test
    void testIsOutputMti_Null() {
        assertFalse(service.isOutputMti(null));
    }

    // ===================== getAdditionalInfoValue =====================

    @Test
    void testGetAdditionalInfoValue_Approved() {
        assertEquals("Approved", service.getAdditionalInfoValue("00"));
        assertEquals("Approved", service.getAdditionalInfoValue("10"));
        assertEquals("Approved", service.getAdditionalInfoValue("11"));
        assertEquals("Approved", service.getAdditionalInfoValue("87"));
        assertEquals("Approved", service.getAdditionalInfoValue("08"));
        assertEquals("Approved", service.getAdditionalInfoValue("85"));
        assertEquals("Approved", service.getAdditionalInfoValue("N0"));
    }

    @Test
    void testGetAdditionalInfoValue_Denied() {
        assertEquals("Denied", service.getAdditionalInfoValue("05"));
        assertEquals("Denied", service.getAdditionalInfoValue("51"));
        assertEquals("Denied", service.getAdditionalInfoValue("99"));
    }

    // ===================== convertResponseCodeToLabelData - rama NO_FOUND =====================

    @Test
    void testConvertResponseCodeToLabelData_WhenNull_ReturnsNoFound() {
        when(applicationDataLocalCache.getCustomValue("net", "response_code", "99"))
                .thenReturn(null);
        assertEquals("NO_FOUND_99", service.convertResponseCodeToLabelData("net", "99"));
    }

    // ===================== operationTypeValue - ramas faltantes =====================

    @Test
    void testOperationTypeValue_P2P_Transfer() {
        assertEquals("P2P_TRANSFER", service.operationTypeValue("10"));
        assertEquals("P2P_TRANSFER", service.operationTypeValue("26"));
    }

    // ===================== channelValue - ramas faltantes =====================

    @Test
    void testChannelValue_ATMTIndicatorWithNullTerminalKey() {
        // tpvIndicator = ATMT pero terminalKey = null → no entra en rama ATM → POS
        String result = service.channelValue(false, "ATMT", null);
        assertEquals("POS", result);
    }

    @Test
    void testChannelValue_NullEcommerceNotAtm() {
        // isEcommerce = null, terminalKey no empieza por ATM, tpvIndicator no es ATMT
        String result = service.channelValue(null, "POST", "TERM01");
        assertEquals("POS", result);
    }

    // ===================== createTransactionReference - con valores null =====================

    @Test
    void testCreateTransactionReference_WithNullFields() {
        // All fields absent → all getters return null
        CanonicalFields fields = CanonicalFields.of(new HashMap<>());
        String result = service.createTransactionReference(fields);
        assertEquals("", result);
    }

    @Test
    void testCreateTransactionReference_WithEmptyFields() {
        Map<String, String> map = new HashMap<>();
        map.put("systemTraceAuditNumber", "   ");
        map.put("acquiringInstitutionIdentificationCode", "   ");
        map.put("retrievalReferenceNumber", "   ");
        map.put("cardAcceptorTerminalIdentification", "   ");
        map.put("transmissionDateTime", "   ");
        CanonicalFields fields = CanonicalFields.of(map);
        String result = service.createTransactionReference(fields);
        assertEquals("", result);
    }

    // ===================== defaultIfEmpty - rama con valor no vacío =====================

    @Test
    void testDefaultIfEmpty_WithNonEmptyValue() {
        assertEquals("real", service.defaultIfEmpty("real", "default"));
    }

    @Test
    void testDefaultIfEmpty_WithNull() {
        assertEquals("default", service.defaultIfEmpty(null, "default"));
    }

    // ===================== getObjectValue - rama null =====================

    @Test
    void testGetObjectValue_Null() {
        Integer result = service.getObjectValue(null, s -> Integer.valueOf((String) s), 0);
        assertEquals(0, result);
    }

}