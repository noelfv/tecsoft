package com.bbva.orchestrator.network.visa;

import com.bbva.gateway.interceptors.GrpcHeadersInfo;
import com.bbva.orchestrator.core.dto.ISO8583;
import com.bbva.orchestrator.core.parser.iso8583.strategy.subfields.CompositeVariableFieldParser;
import com.bbva.orchestrator.core.logic.process.VisaProcessSubField;
import com.bbva.orchestrator.core.commons.CommonsProcessSubField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

class VisaISOSubFieldParserTest {

    // CAMBIO 2: La variable debe ser del tipo Variable
    private CompositeVariableFieldParser compositeFieldParser;

    private VisaProcessSubField parser;
    private ISO8583 iso8583;
    private MockedStatic<GrpcHeadersInfo> mockedHeaders;


    @BeforeEach
    void setUp() {
        mockedHeaders = mockStatic(GrpcHeadersInfo.class);

        // CAMBIO 3: Mockear la clase Variable
        compositeFieldParser = Mockito.mock(CompositeVariableFieldParser.class);

        CommonsProcessSubField defaultISOSubFieldParser = Mockito.mock(CommonsProcessSubField.class);

        // AHORA SÍ FUNCIONA: El constructor recibe el tipo Variable que espera
        parser = new VisaProcessSubField(defaultISOSubFieldParser, compositeFieldParser);

        iso8583 = Mockito.mock(ISO8583.class);
    }

    @AfterEach
    void tearDown() {
        mockedHeaders.close();
    }

    @Test
    void testParseSubfields_MessageType0800_ReturnsEmptyMap() {
        Mockito.when(iso8583.getMessageType()).thenReturn("0800");
        Map<String, String> result = parser.parseSubfields(iso8583);
        assertTrue(result.isEmpty());
    }

    @Test
    void testParseSubfields_FieldsAbsent() {
        Mockito.when(iso8583.getMessageType()).thenReturn("0200");
        Mockito.when(iso8583.getProcessingCode()).thenReturn(null);
        Mockito.when(iso8583.getAdditionalAmounts()).thenReturn(null);
        Mockito.when(iso8583.getAdditionalDataRetailer()).thenReturn(null);

        // Nota: buildSubFieldsSpecific existe en ambas clases, así que el stub funciona igual
        Mockito.when(compositeFieldParser.buildSubFieldsSpecific(anyString(), Mockito.isNull())).thenReturn(new HashMap<>());

        Map<String, String> result = parser.parseSubfields(iso8583);
        assertTrue(result.isEmpty());
    }

    @Test
    void testParseSubfields_Field48Empty() {
        Mockito.when(iso8583.getMessageType()).thenReturn("0100");
        Mockito.when(iso8583.getProcessingCode()).thenReturn("PCODE");
        Mockito.when(iso8583.getAdditionalAmounts()).thenReturn("AMOUNTS");
        Mockito.when(iso8583.getAdditionalDataRetailer()).thenReturn("");

        Mockito.when(compositeFieldParser.buildSubFieldsSpecific("03", "PCODE")).thenReturn(new HashMap<>());
        Mockito.when(compositeFieldParser.buildSubFieldsSpecific("54", "AMOUNTS")).thenReturn(new HashMap<>());

        Map<String, String> result = parser.parseSubfields(iso8583);
        assertTrue(result.isEmpty());
    }

    @Test
    void testParseSubfields_Field48Null() {
        Mockito.when(iso8583.getMessageType()).thenReturn("0100");
        Mockito.when(iso8583.getProcessingCode()).thenReturn("PCODE");
        Mockito.when(iso8583.getAdditionalAmounts()).thenReturn("AMOUNTS");
        Mockito.when(iso8583.getAdditionalDataRetailer()).thenReturn(null);

        Mockito.when(compositeFieldParser.buildSubFieldsSpecific("03", "PCODE")).thenReturn(new HashMap<>());
        Mockito.when(compositeFieldParser.buildSubFieldsSpecific("54", "AMOUNTS")).thenReturn(new HashMap<>());

        Map<String, String> result = parser.parseSubfields(iso8583);
        assertTrue(result.isEmpty());
    }
}