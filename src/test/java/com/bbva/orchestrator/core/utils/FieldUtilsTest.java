package com.bbva.orchestrator.core.utils;

import com.bbva.gateway.interceptors.GrpcHeadersInfo;
import com.bbva.gateway.utils.LogsTraces;
import com.bbva.orchestrator.core.fields.MastercardISOField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

class FieldUtilsTest {
    private MockedStatic<GrpcHeadersInfo> mockedHeaders;

    @BeforeEach
    void setUp() {
        mockedHeaders = mockStatic(GrpcHeadersInfo.class);
    }

    @AfterEach
    void tearDown() {
        mockedHeaders.close();
    }

    @Test
    void testParseDouble_Valid() {
        assertEquals(123.45, FieldUtil.convertAmountDouble("123.45"));
    }

    @Test
    void testParseDouble_Invalid() {
        assertNull(FieldUtil.convertAmountDouble("invalid"));
    }

    @Test
    void testParseDouble_Null() {
        assertNull(FieldUtil.convertAmountDouble(null));
    }

    @Test
    void testIsNullOrEmptySubstring_Valid() {
        assertEquals("test", FieldUtil.isNullOrEmptySubstring("testString", 0, 4));
    }

    @Test
    void testIsNullOrEmptySubstring_OutOfRange() {
        assertNull(FieldUtil.isNullOrEmptySubstring("test", 0, 10));
    }

    @Test
    void testIsNullOrEmptySubstring_Null() {
        assertNull(FieldUtil.isNullOrEmptySubstring(null, 0, 4));
    }

    @Test
    void testConvertFormatDateTime_Valid() {
        assertEquals("2026-01-01T00:00:00.000Z", FieldUtil.convertFormatDateTime("0101000000"));
    }

    @Test
    void testConvertFormatDateTime_ErrorCase_WithMock() {
        String inputInvalido = "INVALIDO";

        // Simulamos la clase estática LogsTraces para que no haga nada real
        try (MockedStatic<LogsTraces> logsMock = Mockito.mockStatic(LogsTraces.class)) {

            String resultado = FieldUtil.convertFormatDateTime(inputInvalido);

            assertNotNull(resultado);
            // Verificamos que se llamó al log (opcional)
            logsMock.verify(() -> LogsTraces.writeWarning(Mockito.anyString()));
        }
    }

    @Test
    void testConvertFormatDateTime_Invalid() {
        assertNotNull(FieldUtil.convertFormatDateTime("invalid"));
    }

    @Test
    void testConvertFormatDateTime_Null() {
        assertNull(FieldUtil.convertFormatDateTime(null));
    }

    @Test
    void testConversionRateValidation_Valid() {
        assertEquals(1.23, FieldUtil.conversionRateValidation("1.23"));
    }

    @Test
    void testConversionRateValidation_Zero() {
        assertNull(FieldUtil.conversionRateValidation("0.0"));
    }

    @Test
    void testConversionRateValidation_Invalid() {
        assertNull(FieldUtil.conversionRateValidation("invalid"));
    }


    @Test
    void testParseDouble_EmptyOrBlank() {
        assertNull(FieldUtil.convertAmountDouble(""));
        assertNull(FieldUtil.convertAmountDouble("   "));
    }

    @Test
    void testIsNullOrEmptySubstring_ReturnsEmpty() {
        assertNull(FieldUtil.isNullOrEmptySubstring("testString", 2, 2));
    }

    @Test
    void testConvertFormatDateTime_CatchException() {
        String invalidInput = "MMddHHmmss";
        assertNotNull(FieldUtil.convertFormatDateTime(invalidInput));
    }

    @Test
    void testConversionRateValidation_NullInput() {
        assertNull(FieldUtil.conversionRateValidation(null));
    }

    private MastercardISOField createMockField(int id, String name) {
        MastercardISOField field = Mockito.mock(MastercardISOField.class);
        Mockito.when(field.getName()).thenReturn(name);
        return field;
    }

    @Test
    void testGetConversionRate_NullValue() {
        assertEquals("", FieldUtil.convertConversionRate(null));
    }

    @Test
    void testGetConversionRate_ValidValue() {
        assertEquals("42081404", FieldUtil.convertConversionRate("208.1404"));
    }

    @Test
    void testConvertAmountString_Valid() {
        assertEquals("000000012345", FieldUtil.convertAmountString(123.45));
        assertEquals("000000000000", FieldUtil.convertAmountString(0.0));
        assertEquals("000000000001", FieldUtil.convertAmountString(0.01));
        assertEquals("000000000100", FieldUtil.convertAmountString(1.0));
    }

    @Test
    void testConvertFormatDateTime_invalid() {
        assertNotNull(FieldUtil.convertFormatDateTime("invalid"));
        assertNull(FieldUtil.convertFormatDateTime(null));
        assertNull(FieldUtil.convertFormatDateTime(""));
    }

    @Test
    void testReConvertFormatDateTime_Valid() {
        String isoDate = "2025-08-20T15:03:22Z";
        assertEquals("0820150322", FieldUtil.reConvertFormatDateTime(isoDate));
    }

    @Test
    void testReConvertFormatDateTime_InvalidOrNull() {
        assertNull(FieldUtil.reConvertFormatDateTime("invalid"));
        assertNull(FieldUtil.reConvertFormatDateTime(null));
        assertNull(FieldUtil.reConvertFormatDateTime(""));
    }

    @Test
    void testReConvertFormatExpiryDate_Valid() {
        assertEquals("2503", FieldUtil.reConvertFormatExpiryDate("2025-03-31"));
        assertEquals("2704", FieldUtil.reConvertFormatExpiryDate("2027-04-30"));

    }

    @Test
    void testReConvertFormatExpiryDate_InvalidOrNull() {
        assertNull(FieldUtil.reConvertFormatExpiryDate("invalid"));
        assertNull(FieldUtil.reConvertFormatExpiryDate(null));
        assertNull(FieldUtil.reConvertFormatExpiryDate(""));
    }

    @Test
    void testConvertEffectiveExchangeRate_Valid_1() {
        assertEquals("208.1404", FieldUtil.convertEffectiveExchangeRate("42081404"));
    }

    @Test
    void testConvertEffectiveExchangeRate_NullOrEmpty() {
        assertNull(FieldUtil.convertEffectiveExchangeRate(null));
        assertNull(FieldUtil.convertEffectiveExchangeRate(""));
    }

    @Test
    void testConvertConversionRate_Valid_3() {
        assertEquals("90945098", FieldUtil.convertConversionRate("0.000945098"));
    }

    @Test
    void testConvertConversionRate_Valid_3_return() {
        assertEquals("0.000945098", FieldUtil.convertEffectiveExchangeRate("90945098"));
    }

    @Test
    void testConvertConversionRate_Valid_2() {
        assertEquals("42081404", FieldUtil.convertConversionRate("208.1404"));
    }

    @Test
    void testConvertConversionRate_Null_1() {
        assertEquals("", FieldUtil.convertConversionRate(null));
    }

    @Test
    void testReplaceWithF0_Valid() {
        String originalString = "1234567890";
        String result = FieldUtil.replaceWithF0(originalString, 2, 4);
        assertEquals("12F0F07890", result);
    }

    @Test
    void testReplaceWithF0_Invalid() {
        String originalString = "123";
        String result = FieldUtil.replaceWithF0(originalString, 5, 4);
        assertEquals("123", result);
    }

    @Test
    void testRevertValidAmount_Invalid() {
        String amount = "invalid";
        String result = FieldUtil.revertValidAmount(amount);
        assertEquals("invalid", result);
    }

    @Test
    void testProcessError_WithSecondaryBitmap() {
        String messageIso = "123456789012345678901234567890";
        String result = FieldUtil.processError(messageIso,"", true);
        assertEquals("123456789012345678901234567890", result);
    }

    @Test
    void testProcessError_WithoutSecondaryBitmap() {
        String messageIso = "123456789012345678901234567890";
        String result = FieldUtil.processError(messageIso,"", false);
        assertEquals("1234567890123456789012345678F0F0F0F0F0F0F0F0F0F0F0F0", result);
    }


    @Test
    void testValidAmount_Valid() {
        String amount = "12345";
        String result = FieldUtil.validAmount(amount);
        assertEquals("123.45", result);
    }

    @Test
    void testValidAmount_Invalid() {
        assertNull(FieldUtil.validAmount(null));
        assertNull(FieldUtil.validAmount(""));
    }

    @Test
    void testRevertValidAmount_Valid() {
        String amount = "123.45";
        String result = FieldUtil.revertValidAmount(amount);
        assertEquals("12345", result);
    }


    @Test
    void testReplaceWithF0() {
        String originalString = "1234567890";
        int startPosition = 2;
        int charsToReplace = 4;

        String result = FieldUtil.replaceWithF0(originalString, startPosition, charsToReplace);

        assertThat(result).isEqualTo("12F0F07890");
    }

    @Test
    void testGetValue_FieldExists() {
        Map<String, String> map = new HashMap<>();
        map.put("field1", "value1");
        String result = FieldUtil.getValue("field1", map);
        assertEquals("value1", result);
    }

    @Test
    void testGetValue_FieldNotExists() {
        Map<String, String> map = new HashMap<>();
        map.put("field1", "value1");
        String result = FieldUtil.getValue("field2", map);
        assertEquals("", result);
    }

    @Test
    void extractSegment_ShouldReturnErrorMessage_WhenInputIsNull() {
        String result = FieldUtil.extractSegment(null, false);
        assertEquals("La cadena no puede ser nula.", result);
    }

    @Test
    void extractSegment_ShouldReturnErrorMessage_WhenLengthIsInsufficient_Primary() {
        String shortString = "1234567890";
        String result = FieldUtil.extractSegment(shortString, false);

        assertTrue(result.contains("Longitud de la cadena"));
        assertTrue(result.contains("offset :82"));
    }

    @Test
    void extractSegment_ShouldReturnErrorMessage_WhenLengthIsInsufficient_Secondary() {
        String mediumString = "A".repeat(90);
        String result = FieldUtil.extractSegment(mediumString, true);

        assertTrue(result.contains("Longitud de la cadena 90"));
        assertTrue(result.contains("offset :98"));
    }

    @Test
    void extractSegment_ShouldExtractCorrectly_WithPrimaryBitmap() {
        String part1 = "A".repeat(64);
        String toRemove = "B".repeat(18);
        String part2 = "C".repeat(10);
        String input = part1 + toRemove + part2;

        String result = FieldUtil.extractSegment(input, false);

        assertEquals(part1 + part2, result);
        assertFalse(result.contains("B"));
    }

    @Test
    void extractSegment_ShouldExtractCorrectly_WithSecondaryBitmap() {
        String part1 = "A".repeat(80);
        String toRemove = "B".repeat(18);
        String part2 = "C".repeat(10);
        String input = part1 + toRemove + part2;

        String result = FieldUtil.extractSegment(input, true);

        assertEquals(part1 + part2, result);
        assertFalse(result.contains("B"));
    }

    @Test
    void testConvertConversionRate_Valid() {
        // Escenario ideal con decimales
        assertEquals("42081404", FieldUtil.convertConversionRate("208.1404"));
    }

    @Test
    void testConvertConversionRate_Valid_return() {
        // Escenario ideal con decimales
        assertEquals("208.1404", FieldUtil.convertEffectiveExchangeRate("42081404"));
    }

    @Test
    void testConvertConversionRate_Null() {
        // Validación de nulidad
        assertEquals("", FieldUtil.convertConversionRate(null));
    }

    @Test
    void testConvertConversionRate_Empty() {
        // Validación de cadena vacía
        assertEquals("", FieldUtil.convertConversionRate(""));
    }

    @Test
    void testConvertConversionRate_NoDecimals() {
        // Escenario sin punto decimal (el que causaba indexOf -1 y fallaba)
        assertEquals("01500000", FieldUtil.convertConversionRate("150"));
    }

    @Test
    void testConvertConversionRate_TruncateExtraLength() {
        // Escenario donde el string original es muy largo y debe truncarse a 8 chars
        assertEquals("61234567", FieldUtil.convertConversionRate("1.234567"));
    }

    @Test
    void testConvertConversionRate_TruncateExtraLength_return() {
        // Escenario donde el string original es muy largo y debe truncarse a 8 chars
        assertEquals("1.234567", FieldUtil.convertEffectiveExchangeRate("61234567"));
    }

    @Test
    void testConvertConversionRate_return() {
        // Escenario donde el string original es muy largo y debe truncarse a 8 chars
        assertEquals("87929700", FieldUtil.convertConversionRate("0.07929700"));
    }

    @Test
    void testConvertConversionRate_1() {
        // Escenario donde el string original es muy largo y debe truncarse a 8 chars
        assertEquals("0.07929700", FieldUtil.convertEffectiveExchangeRate("87929700"));
    }

    // ==========================================================
    // TESTS PARA: convertEffectiveExchangeRate (De Trama a Double/String)
    // ==========================================================

    @Test
    void testConvertEffectiveExchangeRate_Valid() {
        // Escenario ideal (inverso al primer test)
        assertEquals("208.1404", FieldUtil.convertEffectiveExchangeRate("42081404"));
    }

    @Test
    void testConvertEffectiveExchangeRate_Null() {
        // Validación de nulidad
        assertNull(FieldUtil.convertEffectiveExchangeRate(null));
    }

    @Test
    void testConvertEffectiveExchangeRate_Empty() {
        // Validación de cadena vacía
        assertNull(FieldUtil.convertEffectiveExchangeRate(""));
    }

    @Test
    void testConvertEffectiveExchangeRate_HighPrecision() {
        // ¡EL BUG CORREGIDO! Escenario donde la precisión es mayor a la longitud restante
        // Precisión 8: debe rellenar con ceros a la izquierda de forma segura.
        assertEquals("0.01234567", FieldUtil.convertEffectiveExchangeRate("81234567"));
    }

    @Test
    void testConvertEffectiveExchangeRate_ZeroPrecision() {
        // Escenario con precisión 0 (sin decimales en el valor original)
        // Colocará el punto al final: "1500000." (Double.parseDouble lo lee sin problemas)
        assertEquals("1500000.", FieldUtil.convertEffectiveExchangeRate("01500000"));
    }

    @Test
    void testConvertEffectiveExchangeRate_StartsWithDot() {
        // Escenario donde el punto cae exactamente al inicio y debe agregar un '0' por legibilidad
        assertEquals("0.1234500", FieldUtil.convertEffectiveExchangeRate("71234500"));
    }
}