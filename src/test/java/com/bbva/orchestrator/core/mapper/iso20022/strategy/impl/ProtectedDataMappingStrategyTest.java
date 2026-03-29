package com.bbva.orchestrator.core.mapper.iso20022.strategy.impl;

import com.bbva.gateway.dto.iso20022.*;
import com.bbva.orchestrator.core.exception.MapperFieldsException;
import com.bbva.orchestrator.core.mapper.model.CanonicalFields;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProtectedDataMappingStrategyTest {

    @Mock
    private MapperUtil fieldService;

    @InjectMocks
    private ProtectedDataMappingStrategy protectedDataMappingStrategy;

    @Test
    void mapper_whenCryptographicMessageExists_shouldReturnListWithData() {
        String cryptoMessage = "test_crypto_message";

        CanonicalFields fields = CanonicalFields.of(Map.of("cryptographicServiceMessage", cryptoMessage));
        List<ProtectedDataDTO> resultList = protectedDataMappingStrategy.mapper(fields);

        assertNotNull(resultList);
        assertEquals(1, resultList.size());
        ProtectedDataDTO protectedData = resultList.get(0);
        assertNotNull(protectedData.getEnvelopedData());
        assertEquals(cryptoMessage, protectedData.getEnvelopedData().getRecipient().get(0).getKek().getKekId().getKeyId());
    }

    @Test
    void mapper_whenCryptographicMessageIsNull_shouldReturnEmptyList() {
        CanonicalFields fields = CanonicalFields.of(new HashMap<>());
        List<ProtectedDataDTO> resultList = protectedDataMappingStrategy.mapper(fields);

        assertNotNull(resultList);
        assertTrue(resultList.isEmpty());
    }

    @Test
    void unMapper_whenDataExists_shouldReturnMapWithValue() {
        String valorEsperado = "mi-clave-secreta-123";
        KEKIdDTO kekId = KEKIdDTO.builder().keyId(valorEsperado).build();
        KEKDTO kek = KEKDTO.builder().kekId(kekId).build();
        RecipientDTO recipient = RecipientDTO.builder().kek(kek).build();
        EnvelopedDataDTO envelopedData = EnvelopedDataDTO.builder().recipient(Collections.singletonList(recipient)).build();
        ProtectedDataDTO protectedData = ProtectedDataDTO.builder().envelopedData(envelopedData).build();
        List<ProtectedDataDTO> listaDeEntrada = Collections.singletonList(protectedData);

        when(fieldService.getFieldValue(eq(kekId), any(), any())).thenReturn(valorEsperado);

        Map<String, String> resultado = protectedDataMappingStrategy.unMapper("PEER02", listaDeEntrada);

        assertNotNull(resultado);
        assertEquals(valorEsperado, resultado.get("cryptographicServiceMessage"));
    }

    @Test
    void unMapper_whenListIsEmpty_shouldReturnDefaultValue() {
        List<ProtectedDataDTO> listaVacia = Collections.emptyList();
        String valorPorDefecto = "";

        when(fieldService.getFieldValue(eq(null), any(), any())).thenReturn(valorPorDefecto);

        Map<String, String> resultado = protectedDataMappingStrategy.unMapper("PEER02", listaVacia);

        assertNotNull(resultado);
        assertEquals(valorPorDefecto, resultado.get("cryptographicServiceMessage"));
    }

    @Test
    void unMapper_whenNULL_shouldReturnDefaultValue() {
        String valorPorDefecto = "";

        when(fieldService.getFieldValue(eq(null), any(), any())).thenReturn(valorPorDefecto);

        Map<String, String> resultado = protectedDataMappingStrategy.unMapper("PEER02", null);

        assertNotNull(resultado);
        assertEquals(valorPorDefecto, resultado.get("cryptographicServiceMessage"));
    }

    @Test
    void shouldThrowMapperFieldsException_WhenRuntimeExceptionOccurs() {
        // null input causes NPE → MapperFieldsException
        assertThatThrownBy(() -> protectedDataMappingStrategy.mapper(null))
                .isInstanceOf(MapperFieldsException.class)
                .extracting("code")
                .isEqualTo("PGWP-00121");
    }
}
