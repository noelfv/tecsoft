package com.bbva.orchestrator.core.utils;

import com.bbva.orchlib.parser.ParserException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ISOUtilTest {

    @Test
    void testConvertHEXtoEBCDIC_Valid() {
        String hexValue = "C1C2C3";
        String result = ISOUtil.convertHEXtoEBCDIC(hexValue);
        assertEquals("ABC", result);
    }

    @Test
    void testConvertHEXtoEBCDIC_Invalid() {
        assertThrows(IllegalArgumentException.class, () -> ISOUtil.convertHEXtoEBCDIC("InvalidHex"));
    }

    @Test
    void testConvertHEXtoBITMAP_Valid() {
        String hexValue = "F0F1";
        String result = ISOUtil.convertHEXtoBITMAP(hexValue);
        assertEquals("1111000011110001", result);
    }

    @Test
    void testConvertHEXtoBITMAP_Invalid() {
        assertThrows(ParserException.class, () -> ISOUtil.convertHEXtoBITMAP("G0"));
    }

    @Test
    void testConvertBITMAPtoHEX_Valid() {
        String binaryBitmap = "1111000011110001";
        String result = ISOUtil.convertBITMAPtoHEX(binaryBitmap);
        assertEquals("F0F1", result);
    }

    @Test
    void testPadOddToEvenLength_OddLength() {
        int input = 13;
        int result = ISOUtil.alignToEvenLength(input);
        assertEquals(14, result, "La cadena impar debe agregarse uno mas");
    }

    @Test
    void testPadOddToEvenLength_EvenLength() {
        int input = 12;
        int result = ISOUtil.alignToEvenLength(input);
        assertEquals(12, result, "La cadena par debe retornar sin cambios.");
    }

    @Test
    void testPadOddToEvenLength_EmptyOrNull() {
        int input = 0;
        assertEquals(0, ISOUtil.alignToEvenLength(input), "Debe devolver 0");
    }

    @Test
    void testPrefixZeroIfOdd_WhenLengthIsOdd() {
        String input = "abc"; // Longitud 3 (impar)
        String expected = "0abc";

        String actual = ISOUtil.prefixZeroIfOdd(input);

        assertEquals(expected, actual, "Debería añadir '0' a un string de longitud impar (3)");
    }

    @Test
    void testPrefixZeroIfOdd_WhenLengthIsEven() {
        String input = "abcd"; // Longitud 4 (par)
        String expected = "abcd";

        String actual = ISOUtil.prefixZeroIfOdd(input);

        assertEquals(expected, actual, "No debería modificar un string de longitud par (4)");
    }
}