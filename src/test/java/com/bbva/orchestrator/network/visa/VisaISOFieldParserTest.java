package com.bbva.orchestrator.network.visa;

import com.bbva.orchestrator.core.exception.ParserFieldsException;
import com.bbva.orchestrator.core.fields.VisaISOField;
import com.bbva.orchestrator.core.parser.iso8583.ParsedFieldResult;
import com.bbva.orchestrator.core.parser.iso8583.handlers.impl.VisaHandlerField;
import com.bbva.orchestrator.core.parser.iso8583.strategy.FieldParserStrategy;
import com.bbva.orchestrator.core.parser.iso8583.strategy.fields.AlphaNumericFieldParser;
import com.bbva.orchestrator.core.parser.iso8583.strategy.fields.PlainTextFieldParser;
import com.bbva.orchestrator.core.network.visa.VisaProcessField;
import com.bbva.orchlib.parser.ParserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VisaISOFieldParserTest {

    @Mock
    private PlainTextFieldParser plainTextFieldParserDecorator;

    @InjectMocks
    private VisaProcessField visaISOFieldParser;

    private Map<String, String> sampleFieldMap;

    @BeforeEach
    void setUp() {
        sampleFieldMap = new HashMap<>();
        sampleFieldMap.put(VisaISOField.MESSAGE_TYPE.getName(), "0800");
        sampleFieldMap.put(VisaISOField.PROCESSING_CODE.getName(), "900000");
        sampleFieldMap.put(VisaISOField.SYSTEM_TRACE_AUDIT_NUMBER.getName(), "123456");
        sampleFieldMap.put(VisaISOField.RETRIEVAL_REFERENCE_NUMBER.getName(), "ABC123DEF456");
    }

    @Test
    void unMapFields_ShouldThrowExceptionForNullOrEmptyMap() {
        Exception exNull = assertThrows(ParserException.class, () -> visaISOFieldParser.unMapFields(null));
        assertEquals("No se puede generar ISO8583: mapa vacío o nulo", exNull.getMessage());

        Exception exEmpty = assertThrows(ParserException.class, () -> visaISOFieldParser.unMapFields(new HashMap<>()));
        assertEquals("No se puede generar ISO8583: mapa vacío o nulo", exEmpty.getMessage());
    }

    @Test
    void unMapFieldsPlainText_ShouldBuildMessageUsingInjectedMock() {
        when(plainTextFieldParserDecorator.build(anyString(), any(VisaISOField.class), any())).thenAnswer(invocation -> invocation.getArgument(0, String.class));
        String result = visaISOFieldParser.unMapFieldsPlainText(sampleFieldMap);
        String expectedBitmap = "2020000008000000";
        String expectedMTI = "0800";
        String expectedValues = "900000" + "123456" + "ABC123DEF456";
        assertEquals(expectedMTI + expectedBitmap + expectedValues, result);
    }

    @Test
    void processHeader_ShouldProcessValidHeader() throws Exception {
        String expectedHeader = "0A" + "F1F2F3F4F5F6F7F8F9";
        StringBuilder isoMessage = new StringBuilder(expectedHeader + "0800...");
        Method processHeaderMethod = VisaProcessField.class.getDeclaredMethod("processHeaderComplete", StringBuilder.class, Map.class);
        processHeaderMethod.setAccessible(true);
        String actualHeader = (String) processHeaderMethod.invoke(visaISOFieldParser, isoMessage, new HashMap<>());
        assertEquals(expectedHeader, actualHeader);
    }

    @Test
    void processHeader_ShouldThrowExceptionForInvalidHexLength() throws Exception {
        StringBuilder isoMessage = new StringBuilder("XX" + "DatosInvalidos");
        Method processHeaderMethod = VisaProcessField.class.getDeclaredMethod("processHeaderComplete", StringBuilder.class, Map.class);
        processHeaderMethod.setAccessible(true);
        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, () ->
                processHeaderMethod.invoke(visaISOFieldParser, isoMessage,new HashMap<>()));
        assertInstanceOf(NumberFormatException.class, thrown.getCause());
    }

    @Test
    void processHeader_ShouldThrowExceptionForMessageShorterThanIndicatedLength() throws Exception {
        StringBuilder isoMessage = new StringBuilder("10" + "Corto");
        Method processHeaderMethod = VisaProcessField.class.getDeclaredMethod("processHeaderComplete", StringBuilder.class, Map.class);
        processHeaderMethod.setAccessible(true);
        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, () ->
                processHeaderMethod.invoke(visaISOFieldParser, isoMessage,new HashMap<>()));
        assertInstanceOf(StringIndexOutOfBoundsException.class, thrown.getCause());
    }

    @Test
    void processFieldData_ShouldHandleNullField() throws Exception {
        StringBuilder isoMessage = new StringBuilder("0800");
        Map<String, String> valuesMap = new HashMap<>();
        var method = VisaProcessField.class.getDeclaredMethod("processFieldData", VisaISOField.class, StringBuilder.class, int.class, Map.class);
        method.setAccessible(true);
        Exception ex = assertThrows(InvocationTargetException.class, () -> method.invoke(visaISOFieldParser, null, isoMessage, 0, valuesMap));
        assertInstanceOf(NullPointerException.class, ex.getCause());
    }

    @Test
    void processFieldData_ShouldHandleEmptyValue() throws Exception {
        StringBuilder isoMessage = new StringBuilder();
        Map<String, String> valuesMap = new HashMap<>();
        VisaISOField field = VisaISOField.getById(2); // Usar un campo válido
        var method = VisaProcessField.class.getDeclaredMethod("processFieldData", VisaISOField.class, StringBuilder.class, int.class, Map.class);
        method.setAccessible(true);
        Exception ex = assertThrows(InvocationTargetException.class, () -> method.invoke(visaISOFieldParser, field, isoMessage, 0, valuesMap));
        assertInstanceOf(ParserException.class, ex.getCause());
    }

    @Test
    void unMapFields_shouldProcessPresentFieldsAndBuildIsoValues() throws Exception {
        Map<String, String> mapValues = new HashMap<>();
        mapValues.put(VisaISOField.HEADER.getName(), "601234567890");
        mapValues.put("messageType", "0800");
        mapValues.put(VisaISOField.RETRIEVAL_REFERENCE_NUMBER.getName(), "ABC123DEF456");

        // Mock AlphaNumericFieldParser para evitar NullPointerException
        var mockAlphaNumericParser = mock(AlphaNumericFieldParser.class);
        when(mockAlphaNumericParser.build(anyString(), any(), any())).thenReturn("ABC123DEF456HEX");
        // Reemplazar el parser en el campo por el mock usando reflexión
        java.lang.reflect.Field parserField = VisaISOField.RETRIEVAL_REFERENCE_NUMBER.getClass().getDeclaredField("parserStrategy");
        parserField.setAccessible(true);
        parserField.set(VisaISOField.RETRIEVAL_REFERENCE_NUMBER, mockAlphaNumericParser);

        String result = visaISOFieldParser.unMapFields(mapValues);
        assertNotNull(result);
        assertTrue(result.contains("ABC123DEF456HEX"));
    }

    @Test
    void unMapFields_shouldSetHasSecondaryBitmapWhenFieldAbove64Present() throws Exception {
        Map<String, String> mapValues = new HashMap<>();
        mapValues.put(VisaISOField.HEADER.getName(), "601234567890");
        mapValues.put("messageType", "0800");
        // Campo 104: TRANSACTION_DATA
        mapValues.put(VisaISOField.TRANSACTION_DATA.getName(), "ABCDEF123456");

        // Mockear el parser del campo 104 para devolver un valor hexadecimal
        VisaISOField field104 = VisaISOField.getById(104);
        var mockParser104 = mock(field104.getParserStrategy().getClass());
        when(mockParser104.build(anyString(), any(), any())).thenReturn("ABCDEF123456HEX");
        Field parserField104 = field104.getClass().getDeclaredField("parserStrategy");
        parserField104.setAccessible(true);
        parserField104.set(field104, mockParser104);

        String result = visaISOFieldParser.unMapFields(mapValues);
        // El bit 1 del bitmap debe estar activado ("1" al inicio del bitmap)
        // El resultado debe contener el valor hexadecimal mockeado
        assertTrue(result.contains("ABCDEF123456HEX"));
        // El bitmap debe tener el bit 1 activado (bitmap secundario presente)
        String bitmapHex = result.substring(14, 30); // header(12) + mti(4) = 16, bitmap(16)
        // El primer bit del bitmap en binario debe ser '1'
        String bitmapBin = new java.math.BigInteger(bitmapHex, 16).toString(2);
        assertEquals('1', bitmapBin.charAt(0));
    }

    @Test
    void unMapFields_shouldSetSecondaryBitmapWithField104() throws Exception {
        Map<String, String> mapValues = new HashMap<>();
        mapValues.put(VisaISOField.HEADER.getName(), "601234567890");
        mapValues.put("messageType", "0800");
        // Campo 104: TRANSACTION_DATA
        mapValues.put(VisaISOField.TRANSACTION_DATA.getName(), "VALOR104");

        // Mockear el parser del campo 104 para devolver un valor hexadecimal
        VisaISOField field104 = VisaISOField.getById(104);
        var mockParser104 = mock(field104.getParserStrategy().getClass());
        when(mockParser104.build(anyString(), any(), any())).thenReturn("VALOR104HEX");
        Field parserField104 = field104.getClass().getDeclaredField("parserStrategy");
        parserField104.setAccessible(true);
        parserField104.set(field104, mockParser104);

        String result = visaISOFieldParser.unMapFields(mapValues);
        assertTrue(result.contains("VALOR104HEX"));
        // El bitmap debe tener el bit 1 activado (bitmap secundario presente)
        String bitmapHex = result.substring(14, 30); // header(12) + mti(4) = 16, bitmap(16)
        String bitmapBin = new java.math.BigInteger(bitmapHex, 16).toString(2);
        assertEquals('1', bitmapBin.charAt(0));
    }

    @Test
    void processFieldData_ShouldUpdateMapAndPosition() throws Exception {
        // Mock VisaISOField y su estrategia
        VisaISOField mockField = mock(VisaISOField.class);
        doReturn("mockField").when(mockField).getName();
        // Crear mock de la interfaz FieldParserStrategy
        com.bbva.orchestrator.core.parser.iso8583.strategy.FieldParserStrategy mockStrategy = mock(com.bbva.orchestrator.core.parser.iso8583.strategy.FieldParserStrategy.class);
        ParsedFieldResult mockResult = mock(ParsedFieldResult.class);
        doReturn("parsedValue").when(mockResult).value();
        doReturn(5).when(mockResult).consumedLengthInChars();
        doReturn(mockResult).when(mockStrategy).parse(any(), any(), any());
        doReturn(mockStrategy).when(mockField).getParserStrategy();

        StringBuilder isoMessage = new StringBuilder("12345restodelmensaje");
        int initialPosition = 0;
        Map<String, String> valuesMap = new HashMap<>();

        // Acceder al método privado por reflexión
        Method method = VisaProcessField.class.getDeclaredMethod(
                "processFieldData",
                VisaISOField.class,
                StringBuilder.class,
                int.class,
                Map.class
        );
        method.setAccessible(true);
        int newPosition = (int) method.invoke(visaISOFieldParser, mockField, isoMessage, initialPosition, valuesMap);

        // Verificar que el mapa se actualizó y la posición incrementó
        assertEquals("parsedValue", valuesMap.get("mockField"));
        assertEquals(initialPosition + 5, newPosition);
    }

    @Test
    void unMapFields_ShouldIgnoreNullOrEmptyValuesInsideMap() {
        Map<String, String> mapValues = new HashMap<>();
        mapValues.put(VisaISOField.HEADER.getName(), "6000000000");
        mapValues.put("messageType", "0800");
        mapValues.put(VisaISOField.PROCESSING_CODE.getName(), "");
        mapValues.put(VisaISOField.SYSTEM_TRACE_AUDIT_NUMBER.getName(), null);

        String result = visaISOFieldParser.unMapFields(mapValues);

        assertNotNull(result);
        assertTrue(result.contains("0000000000000000")); // Bitmap vacío en Hex
    }

    @Test
    void unMapFieldsPlainText_ShouldHandleSecondaryBitmap() throws Exception {
        Map<String, String> mapValues = new HashMap<>();
        mapValues.put("messageType", "0200");
        VisaISOField field70 = VisaISOField.getById(70);
        if(field70 != null) {
            mapValues.put(field70.getName(), "DATA70");

            when(plainTextFieldParserDecorator.build(anyString(), any(), any())).thenReturn("PARSED_DATA_70");

            String result = visaISOFieldParser.unMapFieldsPlainText(mapValues);

            assertTrue(result.startsWith("0200"));
            assertTrue(result.contains("PARSED_DATA_70"));

            String bitmapHex = result.substring(4, 6);
            int firstByte = Integer.parseInt(bitmapHex, 16);

            assertTrue((firstByte & 0x80) != 0, "El primer bit del bitmap debería ser 1 indicando bitmap secundario");
        }
    }

    @Test
    void mapFields_ShouldProcessSecondaryBitmap_WhenFirstBitIsOne() throws Exception {

        String validHeader = "00"; // 00 hex -> 0 dec -> length 0.

        String headerHex = "000000000000"; // 12 chars. valor 0. length total 0.

        VisaISOField mtiField = VisaISOField.MESSAGE_TYPE;
        var mockMtiStrategy = mock(com.bbva.orchestrator.core.parser.iso8583.strategy.FieldParserStrategy.class);
        ParsedFieldResult mtiResult = mock(ParsedFieldResult.class);
        when(mtiResult.value()).thenReturn("0800");
        when(mtiResult.consumedLengthInChars()).thenReturn(4);
        when(mockMtiStrategy.parse(any(), any(), any())).thenReturn(mtiResult);
        injectMockStrategy(mtiField, mockMtiStrategy);

        VisaISOField bmpPrimaryField = VisaISOField.BITMAP_PRIMARY;
        var mockBmp1Strategy = mock(com.bbva.orchestrator.core.parser.iso8583.strategy.FieldParserStrategy.class);
        ParsedFieldResult bmp1Result = mock(ParsedFieldResult.class);
        String binaryBitmapPrimary = "1" + "0".repeat(63);
        when(bmp1Result.value()).thenReturn(binaryBitmapPrimary);
        when(bmp1Result.consumedLengthInChars()).thenReturn(16);
        when(mockBmp1Strategy.parse(any(), any(), any())).thenReturn(bmp1Result);
        injectMockStrategy(bmpPrimaryField, mockBmp1Strategy);

        VisaISOField bmpSecondaryField = VisaISOField.BITMAP_SECONDARY;
        var mockBmp2Strategy = mock(com.bbva.orchestrator.core.parser.iso8583.strategy.FieldParserStrategy.class);
        ParsedFieldResult bmp2Result = mock(ParsedFieldResult.class);
        String binaryBitmapSecondary = "0".repeat(64); // Todo ceros para no procesar más campos
        when(bmp2Result.value()).thenReturn(binaryBitmapSecondary);
        when(bmp2Result.consumedLengthInChars()).thenReturn(16);
        when(mockBmp2Strategy.parse(any(), any(), any())).thenReturn(bmp2Result);
        injectMockStrategy(bmpSecondaryField, mockBmp2Strategy);

        String inputMsg = headerHex + "0800" + "8000000000000000" + "0000000000000000";

        Map<String, String> result = visaISOFieldParser.mapFields(inputMsg);

        assertTrue(result.containsKey(VisaISOField.BITMAP_SECONDARY.getName()));
        assertEquals(binaryBitmapSecondary, result.get(VisaISOField.BITMAP_SECONDARY.getName()));
    }

    // Método helper para inyectar mocks en las estrategias de los campos del Enum
    private void injectMockStrategy(VisaISOField field,FieldParserStrategy mockStrategy) throws Exception {
        Field parserField = VisaISOField.class.getDeclaredField("parserStrategy");
        parserField.setAccessible(true);
        // Importante: Como es un Enum, modificamos el campo en la instancia específica del Enum
        parserField.set(field, mockStrategy);
    }

    // Método helper para crear estrategias exitosas rápidamente
    private FieldParserStrategy createMockStrategy(String value, int consumedLength) {
        var strategy = mock(FieldParserStrategy.class);
        ParsedFieldResult result = mock(ParsedFieldResult.class);
        when(result.value()).thenReturn(value);
        when(result.consumedLengthInChars()).thenReturn(consumedLength);
        when(strategy.parse(any(), any(), any())).thenReturn(result);
        return strategy;
    }
}
