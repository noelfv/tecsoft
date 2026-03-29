package com.bbva.orchestrator.core.mapper.iso20022.strategy.impl;

import com.bbva.gateway.dto.iso20022.*;
import com.bbva.gateway.interceptors.GrpcHeadersInfo;
import com.bbva.orchestrator.core.dto.ISO8583;
import com.bbva.orchestrator.core.exception.MapperFieldsException;
import com.bbva.orchestrator.core.utils.FieldUtil;
import com.bbva.orchestrator.core.utils.MapperUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.jayway.jsonpath.internal.path.PathCompiler.fail;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionMappingStrategyTest {

    @Mock
    private MapperUtil fieldService;
    @Mock
    private ISO8583 mockInput;

    @InjectMocks
    private TransactionMappingStrategy transactionMappingStrategy;

    private MockedStatic<FieldUtil> mockedFieldUtils;
    private MockedStatic<GrpcHeadersInfo> mockedHeaders;


    @BeforeEach
    void setUp() {
        mockedHeaders = mockStatic(GrpcHeadersInfo.class);
        mockedFieldUtils = mockStatic(FieldUtil.class);
    }
    @AfterEach
    void tearDown() {
        mockedHeaders.close();
        mockedFieldUtils.close();
    }

    @Test
    void map_shouldBuildCompleteTransactionDTO() {
        String processingCode = "012345";
        String originalData = "FUNC123456DATETIME  ACQUIRERSENDER ID"; // 42 chars

        when(mockInput.getProcessingCode()).thenReturn(processingCode);
        when(mockInput.getOriginalDataElements()).thenReturn(originalData);
        when(mockInput.getTransactionAmount()).thenReturn("10050");
        when(mockInput.getAccountIdentification1()).thenReturn("ACC123");
        when(mockInput.getAccountIdentification2()).thenReturn("ACC123");
        when(mockInput.getRedemptionPoints()).thenReturn("500");

        when(fieldService.isNullOrEmptySubstring(originalData, 0, 4)).thenReturn("0100");
        when(fieldService.isNullOrEmptySubstring(originalData, 4, 10)).thenReturn("123456");
        when(fieldService.isNullOrEmptySubstring(originalData, 10, 20)).thenReturn("DATETIME");
        when(fieldService.isNullOrEmptySubstring(originalData, 20, 31)).thenReturn("ACQUIRER");
        when(fieldService.isNullOrEmptySubstring(originalData, 31, 42)).thenReturn("SENDER ID");

        when(fieldService.convertAmountDouble(anyString())).thenReturn(100.50);
        when(fieldService.createTransactionReference(any())).thenReturn("TRX-123TRX-123TRX-123TRX-123TRX-123TRX-123TRX-123TRX");

        Map<String, String> subFields = new HashMap<>();
        subFields.put("ADDITIONAL_AMOUNT_DOUBLE", "25.00");
        subFields.put("03.01", "01");
        subFields.put("03.02", "23");
        subFields.put("03.03", "45");

        TransactionDTO result = transactionMappingStrategy.mapper(mockInput, subFields);

        assertNotNull(result);
        assertEquals("01", result.getTransactionType());
        assertEquals("23", result.getAccountFrom().getAccountType());
        assertEquals("ACC123", result.getAccountTo().getAccountId());
        assertEquals(100.50, result.getTransactionAmounts().getTransactionAmount().getAmount());
        assertEquals("AUTQ", result.getTransactionId().getOriginalDataElements().getMessageFunction());
        assertEquals("500", findValueInList(result.getAdditionalData(), "redemptionPoints"));
    }

    @Test
    void addAdditionalData_whenValueIsPresent_shouldAddToList() {
        // Arrange
        TransactionMappingStrategy strategy = new TransactionMappingStrategy(fieldService);
        List<AdditionalDataDTO> additionalDataList = new ArrayList<>();
        Map<String, String> subFields = new HashMap<>();

        // Se asegura de que la clave tenga un valor válido.
        subFields.put("SOME_KEY", "some_value");

        try {
            java.lang.reflect.Method method = TransactionMappingStrategy.class.getDeclaredMethod(
                    "addAdditionalData", List.class, Map.class, String.class, String.class);
            method.setAccessible(true);

            // Act
            // Se llama al método privado con un valor válido.
            method.invoke(strategy, additionalDataList, subFields, "SOME_KEY", "targetKeyName");

            // Assert
            // Se verifica que la lista ya no está vacía.
            assertEquals(1, additionalDataList.size());

            // Se verifica que el elemento añadido es correcto.
            AdditionalDataDTO addedDto = additionalDataList.get(0);
            assertEquals("targetKeyName", addedDto.getKey());
            assertEquals("some_value", addedDto.getValue());

        } catch (Exception e) {
            fail("El test falló debido a una excepción: " + e.getMessage());
        }
    }

    @Test
    void unMapper_shouldMapTransactionDataToMapValues() {
        // 1. Arrange: Construir el objeto de entrada ISO20022 con datos de prueba.
        TransactionAmountDTO transactionAmount = TransactionAmountDTO.builder().amount(150.75).build();
        ReconciliationAmountDTO reconciliationAmount = ReconciliationAmountDTO.builder().amount(151.00).effectiveExchangeRate("1.25").build();
        CardholderBillingAmountDTO cardholderBillingAmount = CardholderBillingAmountDTO.builder().amount(152.50).effectiveExchangeRate("1.26").build();

        TransactionAmountsDTO transactionAmounts = TransactionAmountsDTO.builder()
                .transactionAmount(transactionAmount)
                .reconciliationAmount(reconciliationAmount)
                .cardholderBillingAmount(cardholderBillingAmount)
                .build();

        TransactionIdDTO transactionId = TransactionIdDTO.builder()
                .systemTraceAuditNumber("STAN654321")
                .transmissionDateTime("2025-08-20T17:02:05Z")
                .build();

        TransactionDTO transaction = TransactionDTO.builder()
                .transactionAmounts(transactionAmounts)
                .transactionId(transactionId)
                .build();

        ISO20022 mockIso20022Input = mock(ISO20022.class);
        when(mockIso20022Input.getTransaction()).thenReturn(transaction);

        // 2. Act: Ejecutar el método que se está probando
        Map<String, String> resultMap = transactionMappingStrategy.unMapper("PEER02",mockIso20022Input.getTransaction());

        // 3. Assert: Verificar que el mapa resultante sea correcto
        assertNotNull(resultMap);
    }

    @Test
    void getAdditionalData_shouldReturnValueForKey() throws Exception {
        TransactionMappingStrategy strategy = new TransactionMappingStrategy(fieldService);
        List<AdditionalDataDTO> list = new ArrayList<>();
        list.add(AdditionalDataDTO.builder().key("testKey").value("testValue").build());
        list.add(AdditionalDataDTO.builder().key("otherKey").value("otherValue").build());
        java.lang.reflect.Method method = TransactionMappingStrategy.class.getDeclaredMethod("getAdditionalData", String.class, List.class);
        method.setAccessible(true);
        String result = (String) method.invoke(strategy, "testKey", list);
        assertEquals("testValue", result);
    }

    @Test
    void getAdditionalData_shouldReturnNullIfKeyNotFound() throws Exception {
        TransactionMappingStrategy strategy = new TransactionMappingStrategy(fieldService);
        List<AdditionalDataDTO> list = new ArrayList<>();
        list.add(AdditionalDataDTO.builder().key("otherKey").value("otherValue").build());
        java.lang.reflect.Method method = TransactionMappingStrategy.class.getDeclaredMethod("getAdditionalData", String.class, List.class);
        method.setAccessible(true);
        String result = (String) method.invoke(strategy, "missingKey", list);
        assertNull(result);
    }

    @Test
    void getAdditionalData_shouldReturnNullIfListIsNull() throws Exception {
        TransactionMappingStrategy strategy = new TransactionMappingStrategy(fieldService);
        java.lang.reflect.Method method = TransactionMappingStrategy.class.getDeclaredMethod("getAdditionalData", String.class, List.class);
        method.setAccessible(true);
        String result = (String) method.invoke(strategy, "anyKey", null);
        Assertions.assertNull(result);
    }

    @Test
    void processAdditionalAmount_shouldHandleAllCases() throws Exception {
        TransactionMappingStrategy strategy = new TransactionMappingStrategy(fieldService);
        Map<String, String> mapValues = new HashMap<>();
        java.lang.reflect.Method method = TransactionMappingStrategy.class.getDeclaredMethod(
                "processAdditionalAmount", Map.class, List.class);
        method.setAccessible(true);

        // Caso 1: Lista nula
        method.invoke(strategy, mapValues, (List<AdditionalAmountDTO>) null);
        assertTrue(mapValues.isEmpty());

        // Caso 2: Lista sin el DTO con clave "additionalAmounts"
        List<AdditionalAmountDTO> listSinClave = new ArrayList<>();
        listSinClave.add(AdditionalAmountDTO.builder().key("otraClave").amount(AmountDTO.builder().amount(10.0).build()).build());
        method.invoke(strategy, mapValues, listSinClave);
        assertTrue(mapValues.isEmpty());

        // Caso 3: DTO con clave pero valor vacío
        AdditionalAmountDTO dtoVacio = AdditionalAmountDTO.builder()
                .key("BLNCHECK")
                .amount(AmountDTO.builder().amount(10.0).build())
                .build();
        List<AdditionalAmountDTO> listVacio = List.of(dtoVacio);
        when(fieldService.getFieldValueDouble(any(AmountDTO.class), any(), anyString())).thenReturn("");
        method.invoke(strategy, mapValues, listVacio);
        assertTrue(mapValues.isEmpty());

        // Caso 4: DTO con clave y valor válido
        AdditionalAmountDTO dtoValido = AdditionalAmountDTO.builder()
                .key("BLNCHECK")
                .amount(AmountDTO.builder().amount(25.0).build())
                .build();
        List<AdditionalAmountDTO> listValido = List.of(dtoValido);
        when(fieldService.getFieldValueDouble(any(AmountDTO.class), any(), anyString())).thenReturn("2500.00");
        method.invoke(strategy, mapValues, listValido);
        assertEquals("2500.00", mapValues.get("ADDITIONAL_AMOUNT_DOUBLE"));
    }

    private String findValueInList(List<AdditionalDataDTO> list, String key) {
        return list.stream()
                .filter(dto -> key.equals(dto.getKey()))
                .map(AdditionalDataDTO::getValue)
                .findFirst()
                .orElse(null);
    }
    @Test
    public void executeUUID(){
        String semilla = "1234567899123456789912345678991234567899";
        String reference35 = generateTransactionReferenceFromSeed(semilla);
        System.out.println("reference -> " + reference35 + " length: " + reference35.length());
    }

    public String generateTransactionReferenceFromSeed(String seed){
        byte[] bytes = seed.getBytes(StandardCharsets.UTF_8);
        byte[] truncate = Arrays.copyOf(bytes, 16);

        UUID uuid = UUID.nameUUIDFromBytes(truncate);

        return uuid.toString().substring(0,35);
    }

    @Test
    void shouldThrowMapperFieldsException_WhenRuntimeExceptionOccurs() {
        // GIVEN
        ISO8583 inputMock = mock(ISO8583.class);
        Map<String, String> subFields = new HashMap<>();

        // Simulamos que el input lanza una excepción al acceder a un dato
        when(inputMock.getNetworkName()).thenThrow(new RuntimeException("Error al mapear desde ISO8583"));

        // WHEN & THEN
        assertThatThrownBy(() -> transactionMappingStrategy.mapper(inputMock, subFields))
                .isInstanceOf(MapperFieldsException.class)
                .hasMessage("java.lang.RuntimeException: Error al mapear desde ISO8583")
                .extracting("code") // Asumiendo que tu excepción tiene un campo 'code'
                .isEqualTo("PGWP-00121");
    }
}