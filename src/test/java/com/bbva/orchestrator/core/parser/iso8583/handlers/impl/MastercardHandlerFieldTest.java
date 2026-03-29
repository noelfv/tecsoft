package com.bbva.orchestrator.core.parser.iso8583.handlers.impl;

import com.bbva.orchestrator.core.exception.ParserFieldsException;
import com.bbva.orchestrator.core.fields.definitions.IFieldDefinition;
import com.bbva.orchestrator.core.fields.definitions.ISODataType;
import com.bbva.orchestrator.core.utils.ISOUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class MastercardHandlerFieldTest {

    private MastercardHandlerField handler;

    // Se usa un mock para la definición del campo, lo que nos permite
    // controlar las propiedades del campo en cada prueba.
    private IFieldDefinition mockFieldDefinition;

    @BeforeEach
    void setUp() {
        handler = new MastercardHandlerField();
        mockFieldDefinition = Mockito.mock(IFieldDefinition.class);
    }

    @Test
    void testGetHeaderFieldVar_shouldReturnCorrectLengthInBytes() {
        // Arrange
        int fieldLength = 4; // Por ejemplo, un campo de 4 bytes
        when(mockFieldDefinition.getLength()).thenReturn(fieldLength);

        // Act
        int result = handler.getHeaderFieldVar(mockFieldDefinition);

        // Assert
        assertEquals(fieldLength * 2, result, "El resultado debería ser el doble de la longitud del campo");
    }

    @Test
    void testDecodeHeaderFieldVar_shouldReturnCorrectDecodedLength() {
        // Arrange
        int lengthHeader = 4; // Longitud del campo de cabecera en bytes (no en caracteres)
        // Datos brutos de ejemplo: longitud "0100" (EBCDIC) y el resto del mensaje.
        String rawDataSegment = ISOUtil.stringToEBCDICHex("0100") + "restoDelMensaje";
        when(mockFieldDefinition.getLength()).thenReturn(lengthHeader); // La longitud del campo en la definición del campo.

        // Act
        int result = handler.decodeHeaderFieldVar(lengthHeader * 2, rawDataSegment, mockFieldDefinition);

        // Assert
        // "0100" en EBCDIC se decodifica a 100. 100 * 2 (representación en bytes) = 200.
        assertEquals(200, result, "La longitud decodificada debería ser 200 (100 * 2)");
    }

    @Test
    void testDecodeHeaderFieldVar_withInvalidData_shouldThrowParserLocalException() {
        // Arrange
        int lengthHeader = 4;
        String rawDataSegment = "ABCDEFGHIJ"; // Datos no válidos, longitud par para evitar IllegalArgumentException
        when(mockFieldDefinition.getLength()).thenReturn(lengthHeader);

        // Act & Assert
        Executable executable = () -> handler.decodeHeaderFieldVar(lengthHeader, rawDataSegment, mockFieldDefinition);
        ParserFieldsException exception = assertThrows(ParserFieldsException.class, executable);

        // CORREGIDO: Usar assertEquals para un mensaje más exacto o asegurar que no es nulo
        assertNotNull(exception.getMessage());
    }

    @Test
    void testDecodeLengthField_shouldReturnCorrectLengthInBytes() {
        // Arrange
        int fieldLength = 16; // Por ejemplo, un bitmap de 16 bytes
        when(mockFieldDefinition.getLength()).thenReturn(fieldLength);

        // Act
        int result = handler.decodeLengthField(mockFieldDefinition);

        // Assert
        assertEquals(fieldLength * 2, result, "El resultado debería ser el doble de la longitud del campo (representación HEX)");
    }

    @Test
    void testDecode_withNumericDataType_shouldConvertEBCDICToString() {
        // Arrange
        String valueEncode = "F1F2F3F4"; // Representación EBCDIC de "1234" en HEX
        ISODataType dataType = ISODataType.NUMERIC;

        // Act
        String result = handler.decode(valueEncode, dataType);

        // Assert
        assertEquals("1234", result, "La decodificación de NUMERIC debe ser correcta");
    }

    @Test
    void testDecode_withAlphaNumericDataType_shouldConvertHEXtoEBCDIC() {
        // Arrange
        String valueEncode = "C1C2C3C4"; // Representación HEX
        ISODataType dataType = ISODataType.ALPHA_NUMERIC;

        // Act
        String result = handler.decode(valueEncode, dataType);

        // Assert
        assertEquals("ABCD", result, "La decodificación de ALPHA_NUMERIC debe ser correcta");
    }

    @Test
    void testDecode_withBinaryStringDataType_shouldConvertHEXtoBITMAP() {
        // Arrange
        String valueEncode = "F1F2F3F4"; // Representación HEX del bitmap
        ISODataType dataType = ISODataType.BINARY_STRING;

        // Act
        String result = handler.decode(valueEncode, dataType);

        // Assert
        // El resultado esperado es una cadena de 0s y 1s.
        String expectedResult = ISOUtil.convertHEXtoBITMAP(valueEncode);
        assertEquals(expectedResult, result, "La decodificación de BINARY_STRING debe ser correcta");
    }

    @Test
    void testDecode_withDefaultDataType_shouldReturnUnchangedValue() {
        // Arrange
        String valueEncode = "someValue";
        ISODataType dataType = ISODataType.HEXADECIMAL; // Usamos un tipo de dato válido que no está en los 'case'

        // Act
        String result = handler.decode(valueEncode, dataType);

        // Assert
        assertEquals(valueEncode, result, "Los tipos de datos no especificados no deben ser modificados");
    }


    @Test
    void testEncodeHeaderFieldVar_withHexadecimalType_shouldReturnCorrectEBCDICHex() {
        // Arrange
        String lengthHeaderDecode = "0A0B"; // 2 bytes
        when(mockFieldDefinition.getTypeData()).thenReturn(ISODataType.HEXADECIMAL);
        when(mockFieldDefinition.getLength()).thenReturn(4);

        // Act
        String result = handler.encodeHeaderFieldVar(lengthHeaderDecode, mockFieldDefinition);

        // Assert
        assertEquals("F0F0F0F2", result, "La codificación del encabezado de longitud hexadecimal debe ser correcta");
    }

    @Test
    void testEncodeHeaderFieldVar_withNonHexadecimalType_shouldReturnCorrectEBCDICHex() {
        // Arrange
        String lengthHeaderDecode = "12345"; // 5 caracteres
        when(mockFieldDefinition.getTypeData()).thenReturn(ISODataType.NUMERIC);
        when(mockFieldDefinition.getLength()).thenReturn(4);

        // Act
        String result = handler.encodeHeaderFieldVar(lengthHeaderDecode, mockFieldDefinition);

        // Assert
        assertEquals("F0F0F0F5", result, "La codificación del encabezado de longitud numérica debe ser correcta");
    }

    @Test
    void testEncode_withNumericDataType_shouldConvertStringToEBCDICHex() {
        // Arrange
        String processedDataSegment = "123456";
        ISODataType dataType = ISODataType.NUMERIC;

        // Act
        String result = handler.encode(processedDataSegment, dataType);

        // Assert
        assertEquals("F1F2F3F4F5F6", result, "La codificación de NUMERIC debe ser correcta");
    }

    @Test
    void testEncode_withAlphaNumericDataType_shouldConvertStringToEBCDICHex() {
        // Arrange
        String processedDataSegment = "ABCDEF";
        ISODataType dataType = ISODataType.ALPHA_NUMERIC;

        // Act
        String result = handler.encode(processedDataSegment, dataType);

        // Assert
        assertEquals("C1C2C3C4C5C6", result, "La codificación de ALPHA_NUMERIC debe ser correcta");
    }

    @Test
    void testEncode_withBinaryStringDataType_shouldConvertBITMAPtoHEX() {
        // Arrange
        String processedDataSegment = "10101010"; // Bitmap
        ISODataType dataType = ISODataType.BINARY_STRING;

        // Act
        String result = handler.encode(processedDataSegment, dataType);

        // Assert
        // El resultado esperado es una representación HEX del bitmap
        String expectedResult = ISOUtil.convertBITMAPtoHEX(processedDataSegment);
        assertEquals(expectedResult, result, "La codificación de BINARY_STRING debe ser correcta");
    }

    @Test
    void testEncode_withDefaultDataType_shouldReturnUnchangedValue() {
        // Arrange
        String processedDataSegment = "unprocessedValue";
        ISODataType dataType = ISODataType.HEXADECIMAL; // Usamos un tipo de dato válido que no está en los 'case'

        // Act
        String result = handler.encode(processedDataSegment, dataType);

        // Assert
        assertEquals(processedDataSegment, result, "Los tipos de datos no especificados no deben ser modificados");
    }
}