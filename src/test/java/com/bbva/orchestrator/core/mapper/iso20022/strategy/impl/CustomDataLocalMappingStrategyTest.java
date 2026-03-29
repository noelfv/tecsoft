package com.bbva.orchestrator.core.mapper.iso20022.strategy.impl;

import com.bbva.gateway.dto.iso20022.AdditionalDataCustomDataLocalDTO;
import com.bbva.gateway.dto.iso20022.CustomDataLocalDTO;
import com.bbva.gateway.dto.iso20022.RequestDTO;
import com.bbva.gateway.interceptors.GrpcHeadersInfo;
import com.bbva.orchestrator.core.exception.MapperFieldsException;
import com.bbva.orchestrator.core.mapper.model.CanonicalFields;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mockStatic;

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
        // Arrange
        String expectedMessageType = "0200";
        CanonicalFields fields = CanonicalFields.of(Map.of("messageType", expectedMessageType));

        // Act
        CustomDataLocalDTO result = customDataLocalMappingStrategy.mapper(fields);

        // Assert
        assertNotNull(result);
        List<AdditionalDataCustomDataLocalDTO> additionalDataList = result.getAdditionalData();
        assertNotNull(additionalDataList);
        assertEquals(3, additionalDataList.size());

        AdditionalDataCustomDataLocalDTO msgTypeElement = additionalDataList.get(1);
        assertEquals("MSGTYPE", msgTypeElement.getRequest().getKey());

        AdditionalDataCustomDataLocalDTO iso8583Element = additionalDataList.get(0);
        assertEquals("ISO8583", iso8583Element.getRequest().getKey());

        AdditionalDataCustomDataLocalDTO iso8583_host_Element = additionalDataList.get(2);
        assertEquals("DE_48", iso8583_host_Element.getRequest().getKey());

        assertEquals(expectedMessageType, msgTypeElement.getRequest().getValue());
    }

    @Test
    void unMapper_shouldReturnEmptyMap() {
        CustomDataLocalDTO input = CustomDataLocalDTO.builder()
                .additionalData(List.of(AdditionalDataCustomDataLocalDTO.builder()
                        .request(RequestDTO.builder().build())
                        .build()))
                .build();

        var result = customDataLocalMappingStrategy.unMapper("PEER02", input);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void shouldThrowMapperFieldsException_WhenNullInput() {
        // WHEN & THEN — null input causes NPE → MapperFieldsException
        assertThatThrownBy(() -> customDataLocalMappingStrategy.mapper(null))
                .isInstanceOf(MapperFieldsException.class)
                .extracting("code")
                .isEqualTo("PGWP-00121");
    }
}
