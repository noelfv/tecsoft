package com.bbva.orchestrator.core.mapper.iso20022.strategy.impl;

import com.bbva.gateway.dto.iso20022.*;
import com.bbva.orchestrator.core.dto.ISO8583;
import com.bbva.orchestrator.core.exception.MapperFieldsException;
import com.bbva.orchestrator.core.utils.MapperUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProtectedDataMappingStrategyTest {

    // --- Dependencias ---
    @Mock
    private ISO8583 mockInput; // Mock para la entrada del mapper

    @Mock
    private MapperUtil fieldService; // Mock para la dependencia del unMapper

    @InjectMocks
    private ProtectedDataMappingStrategy protectedDataMappingStrategy; // La clase que probamos

    // --- Pruebas para el método mapper ---

    @Test
    void mapper_whenCryptographicMessageExists_shouldReturnListWithData() {
        // Preparación
        String cryptoMessage = "test_crypto_message";
        when(mockInput.getCryptographicServiceMessage()).thenReturn(cryptoMessage);

        // Ejecución
        List<ProtectedDataDTO> resultList = protectedDataMappingStrategy.mapper(mockInput, Collections.emptyMap());

        // Verificación
        assertNotNull(resultList);
        assertEquals(1, resultList.size());
        ProtectedDataDTO protectedData = resultList.get(0);
        assertNotNull(protectedData.getEnvelopedData());
        assertEquals(cryptoMessage, protectedData.getEnvelopedData().getRecipient().get(0).getKek().getKekId().getKeyId());
    }

    @Test
    void mapper_whenCryptographicMessageIsNull_shouldReturnEmptyList() {
        // Preparación
        when(mockInput.getCryptographicServiceMessage()).thenReturn(null);

        // Ejecución
        List<ProtectedDataDTO> resultList = protectedDataMappingStrategy.mapper(mockInput, Collections.emptyMap());

        // Verificación
        assertNotNull(resultList);
        assertTrue(resultList.isEmpty());
    }

    // --- Pruebas para el método unMapper ---

    @Test
    void unMapper_whenDataExists_shouldReturnMapWithValue() {
        // Preparación: Construimos objetos reales para evitar el problema con streams y mocks.
        String valorEsperado = "mi-clave-secreta-123";
        KEKIdDTO kekId = KEKIdDTO.builder().keyId(valorEsperado).build();
        KEKDTO kek = KEKDTO.builder().kekId(kekId).build();
        RecipientDTO recipient = RecipientDTO.builder().kek(kek).build();
        EnvelopedDataDTO envelopedData = EnvelopedDataDTO.builder().recipient(Collections.singletonList(recipient)).build();
        ProtectedDataDTO protectedData = ProtectedDataDTO.builder().envelopedData(envelopedData).build();
        List<ProtectedDataDTO> listaDeEntrada = Collections.singletonList(protectedData);

        // Configuramos el mock del servicio.
        when(fieldService.getFieldValue(eq(kekId), any(), any())).thenReturn(valorEsperado);

        // Ejecución
        Map<String, String> resultado = protectedDataMappingStrategy.unMapper("PEER02",listaDeEntrada);

        // Verificación
        assertNotNull(resultado);
        assertEquals(valorEsperado, resultado.get("cryptographicServiceMessage"));
    }

    @Test
    void unMapper_whenListIsEmpty_shouldReturnDefaultValue() {
        // Preparación
        List<ProtectedDataDTO> listaVacia = Collections.emptyList();
        String valorPorDefecto = ""; // Asumiendo que DEFAULT_EMPTY_VALUE es ""

        // Configuramos el mock para cuando el objeto encontrado sea nulo.
        when(fieldService.getFieldValue(eq(null), any(), any())).thenReturn(valorPorDefecto);

        // Ejecución
        Map<String, String> resultado = protectedDataMappingStrategy.unMapper("PEER02",listaVacia);

        // Verificación
        assertNotNull(resultado);
        assertEquals(valorPorDefecto, resultado.get("cryptographicServiceMessage"));
    }

    @Test
    void unMapper_whenNULL_shouldReturnDefaultValue() {
        // Preparación
        String valorPorDefecto = ""; // Asumiendo que DEFAULT_EMPTY_VALUE es ""

        // Configuramos el mock para cuando el objeto encontrado sea nulo.
        when(fieldService.getFieldValue(eq(null), any(), any())).thenReturn(valorPorDefecto);

        // Ejecución
        Map<String, String> resultado = protectedDataMappingStrategy.unMapper("PEER02",null);

        // Verificación
        assertNotNull(resultado);
        assertEquals(valorPorDefecto, resultado.get("cryptographicServiceMessage"));
    }

    @Test
    void shouldThrowMapperFieldsException_WhenRuntimeExceptionOccurs() {
        // GIVEN
        ISO8583 inputMock = mock(ISO8583.class);
        Map<String, String> subFields = new HashMap<>();

        // Simulamos que el input lanza una excepción al acceder a un dato
        when(inputMock.getCryptographicServiceMessage()).thenThrow(new RuntimeException("Error al mapear desde ISO8583"));

        // WHEN & THEN
        assertThatThrownBy(() -> protectedDataMappingStrategy.mapper(inputMock, subFields))
                .isInstanceOf(MapperFieldsException.class)
                .hasMessage("java.lang.RuntimeException: Error al mapear desde ISO8583")
                .extracting("code") // Asumiendo que tu excepción tiene un campo 'code'
                .isEqualTo("PGWP-00121");
    }
}