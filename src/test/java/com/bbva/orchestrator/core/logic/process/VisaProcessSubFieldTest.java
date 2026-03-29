package com.bbva.orchestrator.core.logic.process;

import com.bbva.orchestrator.core.commons.CommonsProcessSubField;
import com.bbva.orchestrator.core.dto.ISO8583;
import com.bbva.orchestrator.core.parser.iso8583.strategy.subfields.CompositeVariableFieldParser;
import com.bbva.orchestrator.core.utils.FieldUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VisaProcessSubFieldTest {

    @Mock
    private CommonsProcessSubField commonsProcessSubField;

    @Mock
    private CompositeVariableFieldParser compositeVariableFieldParser;

    @Mock
    private ISO8583 iso8583;

    @InjectMocks
    private VisaProcessSubField visaProcessSubField;

    @Test
    void parseSubfields_ShouldReturnEmptyMap_WhenProcessingNotRequired() {
        String messageType = "0800";
        when(iso8583.getMessageType()).thenReturn(messageType);

        try (MockedStatic<FieldUtil> fieldUtilMock = mockStatic(FieldUtil.class)) {
            fieldUtilMock.when(() -> FieldUtil.requiredProcess(messageType)).thenReturn(false);

            Map<String, String> result = visaProcessSubField.parseSubfields(iso8583);

            assertNotNull(result);
            assertTrue(result.isEmpty());
            // Verificamos que no se llamara al parser variable
            verifyNoInteractions(commonsProcessSubField, compositeVariableFieldParser);
        }
    }

    @Test
    void parseSubfields_ShouldReturnCombinedMap_WhenProcessingRequired() {
        String messageType = "0200";
        String posTerminalData = "ABC123";
        when(iso8583.getMessageType()).thenReturn(messageType);
        when(iso8583.getPosTerminalData()).thenReturn(posTerminalData);

        Map<String, String> commonsMap = new HashMap<>();
        commonsMap.put("48.01", "value1");
        when(commonsProcessSubField.parseSubfields(iso8583)).thenReturn(commonsMap);

        // Configurar el mock Variable
        Map<String, String> subField60Map = new HashMap<>();
        subField60Map.put("60.01", "value60");
        when(compositeVariableFieldParser.buildSubFieldsSpecific("60", posTerminalData)).thenReturn(subField60Map);

        try (MockedStatic<FieldUtil> fieldUtilMock = mockStatic(FieldUtil.class)) {
            fieldUtilMock.when(() -> FieldUtil.requiredProcess(messageType)).thenReturn(true);

            Map<String, String> result = visaProcessSubField.parseSubfields(iso8583);

            assertEquals(2, result.size());
            assertEquals("value1", result.get("48.01"));
            assertEquals("value60", result.get("60.01"));
            verify(commonsProcessSubField).parseSubfields(iso8583);
            verify(compositeVariableFieldParser).buildSubFieldsSpecific("60", posTerminalData);
        }
    }

    @Test
    void parseSubfields_ShouldReturnCommonsMap_WhenSubField60IsEmpty() {
        String messageType = "0200";
        String posTerminalData = "XYZ";
        when(iso8583.getMessageType()).thenReturn(messageType);
        when(iso8583.getPosTerminalData()).thenReturn(posTerminalData);

        Map<String, String> commonsMap = new HashMap<>();
        commonsMap.put("48.02", "value2");
        when(commonsProcessSubField.parseSubfields(iso8583)).thenReturn(commonsMap);

        // Configurar el mock Variable para retornar mapa vacío
        when(compositeVariableFieldParser.buildSubFieldsSpecific("60", posTerminalData)).thenReturn(new HashMap<>());

        try (MockedStatic<FieldUtil> fieldUtilMock = mockStatic(FieldUtil.class)) {
            fieldUtilMock.when(() -> FieldUtil.requiredProcess(messageType)).thenReturn(true);

            Map<String, String> result = visaProcessSubField.parseSubfields(iso8583);

            assertEquals(1, result.size());
            assertEquals("value2", result.get("48.02"));
            verify(commonsProcessSubField).parseSubfields(iso8583);
            verify(compositeVariableFieldParser).buildSubFieldsSpecific("60", posTerminalData);
        }
    }
}