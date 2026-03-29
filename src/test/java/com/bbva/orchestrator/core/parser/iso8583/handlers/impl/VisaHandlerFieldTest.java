package com.bbva.orchestrator.core.parser.iso8583.handlers.impl;

import com.bbva.gateway.interceptors.GrpcHeadersInfo;
import com.bbva.orchestrator.core.exception.ParserFieldsException;
import com.bbva.orchestrator.core.fields.definitions.IFieldDefinition;
import com.bbva.orchestrator.core.fields.definitions.ISODataType;
import com.bbva.orchestrator.core.utils.ISOUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VisaHandlerFieldTest {
    private VisaHandlerField handler;
    private IFieldDefinition fieldDefinition;
    private MockedStatic<GrpcHeadersInfo> mockedHeaders;

    @BeforeEach
    void setUp() {
        handler = new VisaHandlerField();
        fieldDefinition = mock(IFieldDefinition.class);
        mockedHeaders = mockStatic(GrpcHeadersInfo.class);

    }

    @AfterEach
    void tearDown() {
        mockedHeaders.close();
    }

    @Test
    void testLengthFieldVarAlphaNumeric() {
        int lengthHeader = 2; // 10 en decimal
        String rawData = "0AXXXXXXXXXXXX"; // 10 en decimal
        when(fieldDefinition.getTypeData()).thenReturn(ISODataType.ALPHA_NUMERIC);
        int result = handler.decodeHeaderFieldVar(lengthHeader,rawData, fieldDefinition);
        assertEquals(20, result); // 10*2
    }

    @Test
    void testLengthFieldVarHexadecimal() {
        int lengthHeader = 2; // 10 en decimal
        String rawData = "05XXXXXXXXXXXX"; // 10 en decimal
        when(fieldDefinition.getTypeData()).thenReturn(ISODataType.HEXADECIMAL);
        int result = handler.decodeHeaderFieldVar(lengthHeader,rawData, fieldDefinition);
        assertEquals(10, result); // 5*2
    }

    @Test
    void testLengthFieldVarOtherType() {
        int lengthHeader = 2; // 10 en decimal
        String rawData = "07XXXXXXXXXXXX"; // 10 en decimal
        when(fieldDefinition.getTypeData()).thenReturn(ISODataType.NUMERIC);
        int result = handler.decodeHeaderFieldVar(lengthHeader,rawData, fieldDefinition);
        assertEquals(7, result);
    }

    @Test
    void testLengthFieldFixAlphaNumeric() {
        when(fieldDefinition.getLength()).thenReturn(8);
        when(fieldDefinition.getTypeData()).thenReturn(ISODataType.ALPHA_NUMERIC);
        int result = handler.decodeLengthField(fieldDefinition);
        assertEquals(16, result);
    }

    @Test
    void testLengthFieldFixHexadecimal() {
        when(fieldDefinition.getLength()).thenReturn(6);
        when(fieldDefinition.getTypeData()).thenReturn(ISODataType.HEXADECIMAL);
        int result = handler.decodeLengthField(fieldDefinition);
        assertEquals(12, result);
    }

    @Test
    void testLengthFieldFixBinaryString() {
        when(fieldDefinition.getLength()).thenReturn(5);
        when(fieldDefinition.getTypeData()).thenReturn(ISODataType.BINARY_STRING);
        int result = handler.decodeLengthField(fieldDefinition);
        assertEquals(10, result);
    }

    @Test
    void testLengthFieldFixOddNumeric() {
        when(fieldDefinition.getLength()).thenReturn(7);
        when(fieldDefinition.getTypeData()).thenReturn(ISODataType.NUMERIC);
        int result = handler.decodeLengthField(fieldDefinition);
        assertEquals(7, result); // 7 + 1
    }

    @Test
    void testLengthFieldFixEvenNumeric() {
        when(fieldDefinition.getLength()).thenReturn(8);
        when(fieldDefinition.getTypeData()).thenReturn(ISODataType.NUMERIC);
        int result = handler.decodeLengthField(fieldDefinition);
        assertEquals(8, result);
    }

    @Test
    void testDecodeValueAlphaNumeric() {
        String valueHex = "C1C2C3";
        try (var utilMocked = Mockito.mockStatic(ISOUtil.class)) {
            utilMocked.when(() -> ISOUtil.convertHEXtoEBCDIC(valueHex)).thenReturn("ABC");
            String result = handler.decode(valueHex, ISODataType.ALPHA_NUMERIC);
            assertEquals("ABC", result);
        }
    }

    @Test
    void testDecodeValueBinaryString() {
        String valueHex = "F0F1F2";
        try (var utilMocked = Mockito.mockStatic(ISOUtil.class)) {
            utilMocked.when(() -> ISOUtil.convertHEXtoBITMAP(valueHex)).thenReturn("101010");
            String result = handler.decode(valueHex, ISODataType.BINARY_STRING);
            assertEquals("101010", result);
        }
    }

    @Test
    void testDecodeValueDefault() {
        String valueHex = "FFFF";
        String result = handler.decode(valueHex, ISODataType.NUMERIC);
        assertEquals("FFFF", result);
    }

    @Test
    void testEncodeHeaderFieldVar_Hexadecimal() {
        when(fieldDefinition.getTypeData()).thenReturn(ISODataType.HEXADECIMAL);
        when(fieldDefinition.getLength()).thenReturn(4);
        String fieldValue = "A1B2C3D4"; // length 8, so 4 bytes
        String result = handler.encodeHeaderFieldVar(fieldValue, fieldDefinition);
        assertEquals("0004", result);
    }

    @Test
    void testEncodeHeaderFieldVar_AlphaNumeric() {
        when(fieldDefinition.getTypeData()).thenReturn(ISODataType.ALPHA_NUMERIC);
        when(fieldDefinition.getLength()).thenReturn(3);
        String fieldValue = "ABC"; // length 3
        String result = handler.encodeHeaderFieldVar(fieldValue, fieldDefinition);
        assertEquals("003", result);
    }

    @Test
    void testEncode_NumericOdd() {
        String processedDataSegment = "12345";
        String result = handler.encode(processedDataSegment, ISODataType.NUMERIC_ODD);
        assertEquals("012345", result);
    }

    @Test
    void testEncode_AlphaNumeric() {
        String processedDataSegment = "TEST";
        try (var utilMocked = Mockito.mockStatic(ISOUtil.class)) {
            utilMocked.when(() -> ISOUtil.stringToEBCDICHex(processedDataSegment)).thenReturn("EBCDIC");
            String result = handler.encode(processedDataSegment, ISODataType.ALPHA_NUMERIC);
            assertEquals("EBCDIC", result);
        }
    }

    @Test
    void testEncode_BinaryString() {
        String processedDataSegment = "101010";
        try (var utilMocked = Mockito.mockStatic(ISOUtil.class)) {
            utilMocked.when(() -> ISOUtil.convertBITMAPtoHEX(processedDataSegment)).thenReturn("HEXBITMAP");
            String result = handler.encode(processedDataSegment, ISODataType.BINARY_STRING);
            assertEquals("HEXBITMAP", result);
        }
    }

    @Test
    void testEncode_Default() {
        String processedDataSegment = "DEFAULT";
        String result = handler.encode(processedDataSegment, ISODataType.NUMERIC);
        assertEquals("DEFAULT", result);
    }

    @Test
    void testGetHeaderFieldVar_ReturnsLength() {
        when(fieldDefinition.getLength()).thenReturn(8);
        int result = handler.getHeaderFieldVar(fieldDefinition);
        assertEquals(8, result);
    }

    @Test
    void testLengthFieldVarNumericOddVariable_OddLength() {
        int lengthHeader = 2;
        String rawData = "05XXXXXXXX"; // Hex 05 -> Dec 5
        when(fieldDefinition.getTypeData()).thenReturn(ISODataType.NUMERIC_ODD_VARIABLE);

        try (var utilMocked = Mockito.mockStatic(ISOUtil.class)) {
            // Simulamos: 5 es impar, alignToEvenLength(5) debe devolver 6
            utilMocked.when(() -> ISOUtil.alignToEvenLength(5)).thenReturn(6);

            int result = handler.decodeHeaderFieldVar(lengthHeader, rawData, fieldDefinition);

            assertEquals(6, result, "Debe alinear la longitud impar 5 a 6");
            utilMocked.verify(() -> ISOUtil.alignToEvenLength(5)); // Verifica que se llamó al método mockeado
        }
    }

    @Test
    void testLengthFieldVarNumericOddVariable_EvenLength() {
        int lengthHeader = 2;
        String rawData = "06XXXXXXXX"; // Hex 06 -> Dec 6
        when(fieldDefinition.getTypeData()).thenReturn(ISODataType.NUMERIC_ODD_VARIABLE);

        try (var utilMocked = Mockito.mockStatic(ISOUtil.class)) {
            utilMocked.when(() -> ISOUtil.alignToEvenLength(6)).thenReturn(6);

            int result = handler.decodeHeaderFieldVar(lengthHeader, rawData, fieldDefinition);

            assertEquals(6, result, "Debe mantener la longitud par 6");
            utilMocked.verify(() -> ISOUtil.alignToEvenLength(6));
        }
    }

    @Test
    void testDecodeHeaderFieldVar_RuntimeException_ThrowsParserFieldsException() {
        int lengthHeader = 2;
        String rawData = "XX";
        when(fieldDefinition.getTypeData()).thenReturn(ISODataType.NUMERIC); // El tipo no es crítico aquí

        ParserFieldsException ex = assertThrows(ParserFieldsException.class, () -> {
            handler.decodeHeaderFieldVar(lengthHeader, rawData, fieldDefinition);
        });

        assertEquals("[PGWP-00141]", ex.getCode());
        assertInstanceOf(NumberFormatException.class, ex.getCause());
    }

    @Test
    void testLengthFieldFixNumericOddVariable() {
        when(fieldDefinition.getLength()).thenReturn(9);
        when(fieldDefinition.getTypeData()).thenReturn(ISODataType.NUMERIC_ODD_VARIABLE);

        int result = handler.decodeLengthField(fieldDefinition);
        assertEquals(9, result);
    }

    @Test
    void testDecodeValueNumericOddVariable() {
        String valueHex = "12345";
        // Debe caer en el 'default' case
        String result = handler.decode(valueHex, ISODataType.NUMERIC_ODD_VARIABLE);
        assertEquals("12345", result, "Debe devolver el valor sin procesar");
    }

    @Test
    void testEncodeHeaderFieldVar_NumericOddVariable() {
        when(fieldDefinition.getTypeData()).thenReturn(ISODataType.NUMERIC_ODD_VARIABLE);
        when(fieldDefinition.getLength()).thenReturn(4); // Longitud del prefijo (ej. LLLL)
        String fieldValue = "1234567"; // length 7

        String result = handler.encodeHeaderFieldVar(fieldValue, fieldDefinition);

        assertEquals("0007", result);
    }


    @Test
    void testEncode_NumericOddVariable_OddLength() {
        String processedDataSegment = "12345"; // Longitud 5 (impar)
        String expected = "012345";

        try (var utilMocked = Mockito.mockStatic(ISOUtil.class)) {
            utilMocked.when(() -> ISOUtil.prefixZeroIfOdd(processedDataSegment)).thenReturn(expected);

            String result = handler.encode(processedDataSegment, ISODataType.NUMERIC_ODD_VARIABLE);

            assertEquals(expected, result, "Debe añadir prefijo '0' a longitud impar");
            utilMocked.verify(() -> ISOUtil.prefixZeroIfOdd(processedDataSegment));
        }
    }

    @Test
    void testEncode_NumericOddVariable_EvenLength() {
        String processedDataSegment = "123456"; // Longitud 6 (par)
        String expected = "123456";

        try (var utilMocked = Mockito.mockStatic(ISOUtil.class)) {
            utilMocked.when(() -> ISOUtil.prefixZeroIfOdd(processedDataSegment)).thenReturn(expected);

            String result = handler.encode(processedDataSegment, ISODataType.NUMERIC_ODD_VARIABLE);

            assertEquals(expected, result, "No debe modificar longitud par");
            utilMocked.verify(() -> ISOUtil.prefixZeroIfOdd(processedDataSegment));
        }
    }

    @Test
    void testLengthFieldFixNumericOdd_WithOddLength() {
        when(fieldDefinition.getLength()).thenReturn(7); // Longitud impar
        when(fieldDefinition.getTypeData()).thenReturn(ISODataType.NUMERIC_ODD);

        int result = handler.decodeLengthField(fieldDefinition);

        assertEquals(8, result, "Para NUMERIC_ODD (impar), debe ser length + 1");
    }

    @Test
    void testLengthFieldFixNumericOdd_WithEvenLength() {
        when(fieldDefinition.getLength()).thenReturn(8); // Longitud par
        when(fieldDefinition.getTypeData()).thenReturn(ISODataType.NUMERIC_ODD);

        int result = handler.decodeLengthField(fieldDefinition);

        assertEquals(9, result, "Para NUMERIC_ODD (par), también debe ser length + 1");
    }
}