package com.bbva.orchestrator.core.mapper.iso20022.strategy.impl;

import com.bbva.gateway.dto.iso20022.AdditionalDataCustomDataLocalDTO;
import com.bbva.gateway.dto.iso20022.CustomDataLocalDTO;
import com.bbva.gateway.dto.iso20022.RequestDTO;
import com.bbva.gateway.interceptors.GrpcHeadersInfo;
import com.bbva.orchestrator.core.dto.ISO8583;
import com.bbva.orchestrator.core.exception.MapperFieldsException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class CustomDataLocalMappingStrategyTest {


    @InjectMocks
    private CustomDataLocalMappingStrategy customDataLocalMappingStrategy;

    private MockedStatic<GrpcHeadersInfo> mockedHeaders;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockedHeaders = mockStatic(GrpcHeadersInfo.class);
    }

    @AfterEach
    void tearDown() {
        mockedHeaders.close();
    }

    @Test
    void mapper_shouldMapCorrectly() {
        // 1. Arrange (Preparar)
        String expectedMessageType = "0200";
        String expectedIso8583Value = "mocked-iso8583-value";

        ISO8583 iso8583Input = ISO8583.builder()
                .messageType(expectedMessageType)
                .build();

        // Simulamos el comportamiento del mock. Cuando se llame a getISO8583 con cualquier String,
        // retornará el valor esperado.
        // 2. Act (Ejecutar)
        CustomDataLocalDTO result = customDataLocalMappingStrategy.mapper(iso8583Input, null);

        // 3. Assert (Validar)
        assertNotNull(result);
        List<AdditionalDataCustomDataLocalDTO> additionalDataList = result.getAdditionalData();
        assertNotNull(additionalDataList);
        assertEquals(3, additionalDataList.size());

        // Validar el primer elemento (MSGTYPE)
        AdditionalDataCustomDataLocalDTO msgTypeElement = additionalDataList.get(1);
        assertEquals("MSGTYPE", msgTypeElement.getRequest().getKey());

        // Validar el segundo elemento (ISO8583)
        AdditionalDataCustomDataLocalDTO iso8583Element = additionalDataList.get(0);
        assertEquals("ISO8583", iso8583Element.getRequest().getKey());

        // Validar el segundo elemento (ISO8583)
        AdditionalDataCustomDataLocalDTO iso8583_host_Element = additionalDataList.get(2);
        assertEquals("DE_48", iso8583_host_Element.getRequest().getKey());

        assertEquals(expectedMessageType, msgTypeElement.getRequest().getValue());

    }

    @Test
    void unMapper_shouldReturnEmptyMap() {
        CustomDataLocalDTO input = CustomDataLocalDTO.builder()
                .additionalData( List.of(AdditionalDataCustomDataLocalDTO.builder()
                        .request(RequestDTO.builder().build())
                        .build()))
                .build();

        var result = customDataLocalMappingStrategy.unMapper("PEER02",input);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void shouldThrowMapperFieldsException_WhenRuntimeExceptionOccurs() {
        // GIVEN
        ISO8583 inputMock = mock(ISO8583.class);
        Map<String, String> subFields = new HashMap<>();

        // Simulamos que el input lanza una excepción al acceder a un dato
        when(inputMock.getMessageType()).thenThrow(new RuntimeException("Error al mapear CustomDataLocalDTO desde ISO8583"));

        // WHEN & THEN
        assertThatThrownBy(() -> customDataLocalMappingStrategy.mapper(inputMock, subFields))
                .isInstanceOf(MapperFieldsException.class)
                .hasMessage("java.lang.RuntimeException: Error al mapear CustomDataLocalDTO desde ISO8583")
                .extracting("code") // Asumiendo que tu excepción tiene un campo 'code'
                .isEqualTo("PGWP-00121");
    }
}