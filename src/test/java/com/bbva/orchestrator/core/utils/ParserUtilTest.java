package com.bbva.orchestrator.core.utils;

import com.bbva.orchestrator.core.exception.ParserFieldsException;
import com.bbva.orchestrator.core.fields.definitions.IFieldDefinition;
import com.bbva.orchestrator.core.parser.iso8583.ParsedFieldResult;
import com.bbva.orchestrator.core.parser.iso8583.handlers.NetworkHandlerField;
import org.junit.jupiter.api.*;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ParserUtilTest {

    // --- Pruebas existentes (correctas y mantenidas) ---

    @Test
    void testCreateMessageError() {
        assertEquals("Error en campo 5: No hay mapeo disponible", ParserUtil.createMessageError(5));
    }

    @Nested
    @DisplayName("Pruebas para el método processFixedLengthSubField()")
    class ProcessFixedLengthSubFieldTests {
        @Test
        @DisplayName("Debería procesar correctamente un subcampo de longitud fija")
        void testProcessFixedLengthSubField_Success() {
            IFieldDefinition fieldDef = mock(IFieldDefinition.class);
            NetworkHandlerField handler = mock(NetworkHandlerField.class);
            when(handler.decodeLengthField(fieldDef)).thenReturn(3);
            when(handler.decode("ABC", fieldDef.getTypeData())).thenReturn("VAL");

            ParsedFieldResult result = ParserUtil.processFixedLengthSubField("ABCDEF", fieldDef, handler, "E1");

            assertEquals("VAL", result.value());
            assertEquals(3, result.consumedLengthInChars());
        }

        @Test
        @DisplayName("Debería lanzar ParserFieldsException si ocurre un error en el procesamiento")
        void testProcessFixedLengthSubField_Failure() {
            IFieldDefinition fieldDef = mock(IFieldDefinition.class);
            NetworkHandlerField handler = mock(NetworkHandlerField.class);
            when(handler.decodeLengthField(any())).thenThrow(new RuntimeException("Decoding Error"));

            assertThrows(ParserFieldsException.class, () -> {
                ParserUtil.processFixedLengthSubField("ABCDEF", fieldDef, handler, "E1");
            });
        }
    }

    @Test
    void testBuildFixedLengthSubField() {
        IFieldDefinition fieldDef = mock(IFieldDefinition.class);
        NetworkHandlerField handler = mock(NetworkHandlerField.class);
        when(handler.encode("VAL", fieldDef.getTypeData())).thenReturn("ENCODED");

        String result = ParserUtil.buildFixedLengthSubField("VAL", fieldDef, handler);

        assertEquals("ENCODED", result);
    }

    @Test
    void testMaskSensitiveFields_primaryAccountNumber() {
        Map<String, String> map = new HashMap<>();
        map.put("primaryAccountNumber", "1234567890123456");
        map.put("trackOneData", "ABCDEFG");
        map.put("trackTwoData", "HIJKLMN");

        try (var mocked = mockStatic(ParserUtil.class, CALLS_REAL_METHODS)) {
            mocked.when(() -> ParserUtil.obfuscate("1234567890123456", "left", "*", 6, 6))
                    .thenReturn("123456******3456");
            Map<String, String> masked = ParserUtil.maskSensitiveFields(map);
            assertNotNull(masked);
            assertEquals("123456******3456", masked.get("primaryAccountNumber"));
            assertEquals("0000000", masked.get("trackOneData"));
            assertEquals("0000000", masked.get("trackTwoData"));
        }
    }

    @Test
    void testObfuscateLeft() {
        String value = "1234567890123456";
        String direction = "left";
        String character = "*";
        int startPosition = 4;
        int numberOfChars = 4;
        String expected = "1234****90123456";

        String result = ParserUtil.obfuscate(value, direction, character, startPosition, numberOfChars);
        assertEquals(expected, result);
    }


    @Test
    void testObfuscateRight() {
        String value = "1234567890123456";
        String direction = "right";
        String character = "*";
        int startPosition = 4;
        int numberOfChars = 4;
        String expected = "12345678****3456";

        String result = ParserUtil.obfuscate(value, direction, character, startPosition, numberOfChars);
        assertEquals(expected, result);
    }

    @Test
    void testMaskSensitiveFields_nullValues() {
        Map<String, String> map = new HashMap<>();
        map.put("primaryAccountNumber", null);
        map.put("trackOneData", null);
        map.put("trackTwoData", null);
        try (var mocked = mockStatic(ParserUtil.class, CALLS_REAL_METHODS)) {
                mocked.when(() -> ParserUtil.obfuscate(null, "left", "*", 6, 6))
                        .thenReturn(null);
            Map<String, String> masked = ParserUtil.maskSensitiveFields(map);
            assertNull(masked.get("primaryAccountNumber"));
            assertNull(masked.get("trackOneData"));
            assertNull(masked.get("trackTwoData"));
        }
    }

    @Nested
    @DisplayName("Pruebas para el método processFieldData()")
    class ProcessFieldDataTests {

        @Test
        @DisplayName("Debería procesar correctamente un campo, actualizar el mapa y retornar nueva posición")
        void testProcessFieldData_Success() {
            StringBuilder isoMessage = new StringBuilder("HEADERDATAREST");
            int currentPosition = 6; // Supongamos que empezamos donde dice "DATA"
            Map<String, String> valuesMap = new HashMap<>();

            com.bbva.orchestrator.core.fields.definitions.ISOField mockField = mock(com.bbva.orchestrator.core.fields.definitions.ISOField.class);
            NetworkHandlerField mockNetworkHandler = mock(NetworkHandlerField.class);

            var mockStrategy = mock(com.bbva.orchestrator.core.parser.iso8583.strategy.FieldParserStrategy.class);

            ParsedFieldResult mockResult = new ParsedFieldResult("DATA", 4); // Valor extraído y longitud consumida

            when(mockField.getName()).thenReturn("FIELD_DATA");
            when(mockField.getParserStrategy()).thenReturn(mockStrategy);
            when(mockStrategy.parse(eq("DATAREST"), eq(mockField), eq(mockNetworkHandler))).thenReturn(mockResult);

            int newPosition = ParserUtil.processFieldData(mockField, isoMessage, currentPosition, valuesMap, mockNetworkHandler);


            assertEquals(10, newPosition);
            assertTrue(valuesMap.containsKey("FIELD_DATA"));
            assertEquals("DATA", valuesMap.get("FIELD_DATA"));
        }

        @Test
        @DisplayName("Debería capturar ParserFieldsException y relanzar ParserException")
        void testProcessFieldData_Exception() {
            StringBuilder isoMessage = new StringBuilder("ERRORSEGMENT");
            int currentPosition = 0;
            Map<String, String> valuesMap = new HashMap<>();

            com.bbva.orchestrator.core.fields.definitions.ISOField mockField = mock(com.bbva.orchestrator.core.fields.definitions.ISOField.class);
            NetworkHandlerField mockNetworkHandler = mock(NetworkHandlerField.class);
            var mockStrategy = mock(com.bbva.orchestrator.core.parser.iso8583.strategy.FieldParserStrategy.class);

            ParserFieldsException originalException = new ParserFieldsException("Fallo de parseo");

            when(mockField.getParserStrategy()).thenReturn(mockStrategy);
            when(mockStrategy.parse(anyString(), eq(mockField), eq(mockNetworkHandler))).thenThrow(originalException);

            com.bbva.orchlib.parser.ParserException thrown = assertThrows(com.bbva.orchlib.parser.ParserException.class, () -> {
                ParserUtil.processFieldData(mockField, isoMessage, currentPosition, valuesMap, mockNetworkHandler);
            });

            assertNotNull(thrown.getMessage());
        }
    }

    @Test
    @DisplayName("Prueba de instanciación para cubrir el constructor por defecto (Lombok @NoArgsConstructor)")
    void testConstructor() {
        // Aunque es una clase utilitaria estática, instanciarla cubre la línea de la clase generada por Lombok
        ParserUtil parserUtil = new ParserUtil();
        assertNotNull(parserUtil);
    }

    @Test
    @DisplayName("Debería mantener campos no sensibles sin cambios en maskSensitiveFields")
    void testMaskSensitiveFields_PreserveOtherFields() {
        Map<String, String> map = new HashMap<>();
        map.put("otherField", "SensitiveDataButNotInList");
        map.put("primaryAccountNumber", "1234567890123456");

        try (var mocked = mockStatic(ParserUtil.class, CALLS_REAL_METHODS)) {
                mocked.when(() -> ParserUtil.obfuscate(anyString(), anyString(), anyString(), anyInt(), anyInt()))
                        .thenReturn("MASKED_PAN");
            Map<String, String> result = ParserUtil.maskSensitiveFields(map);

            // Verificar que se enmascaró el PAN
            assertEquals("MASKED_PAN", result.get("primaryAccountNumber"));
            // Verificar que el otro campo sigue intacto
            assertEquals("SensitiveDataButNotInList", result.get("otherField"));
        }
    }
}