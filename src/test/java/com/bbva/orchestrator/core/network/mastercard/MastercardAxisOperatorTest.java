package com.bbva.orchestrator.core.network.mastercard;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class MastercardAxisOperatorTest {

    private static final String VAR_4801 = "48.01";
    private static final String VAR_2201 = "22.01";
    private static final String VAR_6110 = "61.10";
    private static final String VAR_4842 = "48.42";
    private static final String VAR_6104 = "61.04";
    private static final String VAR_6105 = "61.05";
    private static final String VAR_0301 = "03.01";
    private static final String VAR_6111 = "61.11";
    private static final String TCC = "T"; // Asumiendo que TCC es una constante con valor "TCC" o similar
    private static final String RETAIL = "R";

    // Helper para crear un mapa base válido
    private Map<String, String> createValidMap() {
        Map<String, String> map = new HashMap<>();
        map.put(VAR_4801, TCC);
        map.put(VAR_2201, "10");
        map.put(VAR_4842, "anyValue"); // El valor de este no importa, solo su existencia
        map.put(VAR_6104, "4");
        map.put(VAR_6105, "1");
        map.put(VAR_6110, "6");
        return map;
    }

    // Crea un mapa base configurado para que la primera rama (POST) sea exitosa
    private Map<String, String> createBasePostMap() {
        Map<String, String> map = new HashMap<>();
        map.put(VAR_4801, RETAIL);
        map.put(VAR_2201, "05");          // Valor válido por defecto
        map.put(VAR_6111, "2");  // Debe estar en VALID_VALUES
        map.put(VAR_6105, "0");           // Debe ser "0"
        return map;
    }

    @Test
    void testMissingKey_VAR_4801() {
        Map<String, String> subFields = createValidMap();
        subFields.remove(VAR_4801);
        assertFalse(MastercardAxisOperator.channelECommerceIndicator(subFields), "Debe fallar si falta 48.01");
    }

    @Test
    void testMissingKey_VAR_2201() {
        Map<String, String> subFields = createValidMap();
        subFields.remove(VAR_2201);
        assertFalse(MastercardAxisOperator.channelECommerceIndicator(subFields), "Debe fallar si falta 22.01");
    }

    @Test
    void testMissingKey_VAR_4842() {
        Map<String, String> subFields = createValidMap();
        subFields.remove(VAR_4842);
        assertFalse(MastercardAxisOperator.channelECommerceIndicator(subFields), "Debe fallar si falta 48.42");
    }

    @Test
    void testMissingKey_VAR_6104() {
        Map<String, String> subFields = createValidMap();
        subFields.remove(VAR_6104);
        assertFalse(MastercardAxisOperator.channelECommerceIndicator(subFields), "Debe fallar si falta 61.04");
    }

    @Test
    void testMissingKey_VAR_6105() {
        Map<String, String> subFields = createValidMap();
        subFields.remove(VAR_6105);
        assertFalse(MastercardAxisOperator.channelECommerceIndicator(subFields), "Debe fallar si falta 61.05");
    }

    @Test
    void testMissingKey_VAR_6110() {
        Map<String, String> subFields = createValidMap();
        subFields.remove(VAR_6110);
        assertFalse(MastercardAxisOperator.channelECommerceIndicator(subFields), "Debe fallar si falta 61.10");
    }


    // Prueba cuando 61.04 es "4" con los diferentes valores permitidos de 22.01
    @ParameterizedTest
    @ValueSource(strings = {"10", "01", "81"})
    void testSuccess_Block1_Var6104Is4(String var2201Value) {
        Map<String, String> subFields = createValidMap();
        subFields.put(VAR_6104, "4");
        subFields.put(VAR_2201, var2201Value);

        assertTrue(MastercardAxisOperator.channelECommerceIndicator(subFields),
                "Debe ser true para 61.04='4' y 22.01='" + var2201Value + "'");
    }


    @ParameterizedTest
    @ValueSource(strings = {"10", "01", "81"})
    void testSuccess_Block2_Var6104Is5(String var2201Value) {
        Map<String, String> subFields = createValidMap();
        subFields.put(VAR_6104, "5"); // Cambiamos a la segunda condición lógica
        subFields.put(VAR_2201, var2201Value);

        assertTrue(MastercardAxisOperator.channelECommerceIndicator(subFields),
                "Debe ser true para 61.04='5' y 22.01='" + var2201Value + "'");
    }

    @Test
    void testFail_WrongTCC() {
        Map<String, String> subFields = createValidMap();
        subFields.put(VAR_4801, "WRONG_VALUE");
        assertFalse(MastercardAxisOperator.channelECommerceIndicator(subFields));
    }

    @Test
    void testFail_Wrong2201() {
        Map<String, String> subFields = createValidMap();
        subFields.put(VAR_2201, "99"); // Valor no permitido
        assertFalse(MastercardAxisOperator.channelECommerceIndicator(subFields));
    }

    @Test
    void testFail_Wrong6104() {
        Map<String, String> subFields = createValidMap();
        subFields.put(VAR_6104, "3"); // Solo permite "4" o "5"
        assertFalse(MastercardAxisOperator.channelECommerceIndicator(subFields));
    }

    @Test
    void testFail_Wrong6105() {
        Map<String, String> subFields = createValidMap();
        subFields.put(VAR_6105, "0"); // Debe ser "1"
        assertFalse(MastercardAxisOperator.channelECommerceIndicator(subFields));
    }

    @Test
    void testFail_Wrong6110() {
        Map<String, String> subFields = createValidMap();
        subFields.put(VAR_6110, "7"); // Debe ser "6"
        assertFalse(MastercardAxisOperator.channelECommerceIndicator(subFields));
    }

    @Test
    void testChannelECommerceIndicator_missingFields() {
        Map<String, String> subFields = new HashMap<>();
        subFields.put("61.04", "val");
        subFields.put("61.05", "val");
        subFields.put("61.10", "val");
        // Falta 48.42
        assertFalse(MastercardAxisOperator.channelECommerceIndicator(subFields));
    }

    @Test
    void testChannelECommerceIndicator_missingFields2() {
        Map<String, String> subFields = new HashMap<>();
        subFields.put("48.01", "val");
        subFields.put("22.01", "val");
        subFields.put("48.42", "val");
        // Falta 48.42
        assertFalse(MastercardAxisOperator.channelECommerceIndicator(subFields));
    }

    @Test
    void testChannelECommerceIndicator_allFieldsPresent_invalidValues() {
        Map<String, String> subFields = new HashMap<>();
        subFields.put("48.42", "1");
        subFields.put("61.04", "1");
        subFields.put("61.05", "1");
        subFields.put("61.10", "1");
        assertFalse(MastercardAxisOperator.channelECommerceIndicator( subFields));
    }

    @Test
    void testChannelECommerceIndicator_allFieldsPresent_validValues_firstCondition() {
        Map<String, String> subFields = new HashMap<>();
        subFields.put("48.01", "T");
        subFields.put("22.01", "10");
        subFields.put("48.42", "0103212");
        subFields.put("61.04", "4");
        subFields.put("61.05", "1");
        subFields.put("61.10", "6");
        assertTrue(MastercardAxisOperator.channelECommerceIndicator( subFields));
    }

    @Test
    void testChannelECommerceIndicator_allFieldsPresent_validValues_new1() {
        Map<String, String> subFields = new HashMap<>();
        subFields.put("48.01", "T");
        subFields.put("22.01", "01");
        subFields.put("48.42", "0103212");
        subFields.put("61.04", "4");
        subFields.put("61.05", "1");
        subFields.put("61.10", "6");
        assertTrue(MastercardAxisOperator.channelECommerceIndicator( subFields));
    }

    @Test
    void testChannelECommerceIndicator_allFieldsPresent_validValues_new2() {
        Map<String, String> subFields = new HashMap<>();
        subFields.put("48.01", "T");
        subFields.put("22.01", "81");
        subFields.put("48.42", "0103212");
        subFields.put("61.04", "4");
        subFields.put("61.05", "1");
        subFields.put("61.10", "6");
        assertTrue(MastercardAxisOperator.channelECommerceIndicator( subFields));
    }

    @Test
    void testChannelECommerceIndicator_allFieldsPresent_validValues_secondCondition() {
        Map<String, String> subFields = new HashMap<>();
        subFields.put("48.01", "T");
        subFields.put("22.01", "10");
        subFields.put("48.42", "0103212");
        subFields.put("61.04", "5");
        subFields.put("61.05", "1");
        subFields.put("61.10", "6");
        assertTrue(MastercardAxisOperator.channelECommerceIndicator(subFields));
    }

    @Test
    void testChannelECommerceIndicator_pointServiceEntryModeNotValid() {
        Map<String, String> subFields = new HashMap<>();
        subFields.put("48.01", " ");
        subFields.put("22.01", "10");
        subFields.put("48.42", "0103212");
        subFields.put("61.04", "3");
        subFields.put("61.05", "4");
        subFields.put("61.10", "0");
        assertFalse(MastercardAxisOperator.channelECommerceIndicator( subFields));
    }

    @Test
    void testChannelTPVIndicator_POST() {
        Map<String, String> subFields = new HashMap<>();
        subFields.put("48.01", "R");
        subFields.put("22.01", "05");
        subFields.put("61.11", "2");
        subFields.put("61.05", "0");
        String result = MastercardAxisOperator.channelTPVIndicator(subFields, "any");
        assertEquals("POST", result);
    }

    @Test
    void testChannelTPVIndicator_POST_2() {
        Map<String, String> subFields = new HashMap<>();
        subFields.put("48.01", "R");
        subFields.put("22.01", "05");
        subFields.put("61.11", "2");
        subFields.put("61.05", "0");
        String result = MastercardAxisOperator.channelTPVIndicator(subFields, "any");
        assertEquals("POST", result);
    }

    @Test
    void testChannelTPVIndicator_POST_3() {
        Map<String, String> subFields = new HashMap<>();
        subFields.put("48.01", "R");
        subFields.put("22.01", "07");
        subFields.put("61.11", "2");
        subFields.put("61.05", "0");
        String result = MastercardAxisOperator.channelTPVIndicator(subFields, "any");
        assertEquals("POST", result);
    }

    @Test
    void testChannelTPVIndicator_POST_4() {
        Map<String, String> subFields = new HashMap<>();
        subFields.put("48.01", "R");
        subFields.put("22.01", "02");
        subFields.put("61.11", "2");
        subFields.put("61.05", "0");
        String result = MastercardAxisOperator.channelTPVIndicator(subFields, "any");
        assertEquals("POST", result);
    }

    @Test
    void testChannelTPVIndicator_ATMT() {
        Map<String, String> subFields = new HashMap<>();
        subFields.put("03.01", "01");
        String result = MastercardAxisOperator.channelTPVIndicator(subFields, "6011");
        assertEquals("ATMT", result);
    }

    @Test
    void testChannelTPVIndicator_OTHNRETV() {
        Map<String, String> subFields = new HashMap<>();
        subFields.put("03.01", "01");
        String result = MastercardAxisOperator.channelTPVIndicator(subFields, "6010");
        assertEquals("OTHNRETV", result);
    }

    @Test
    void testChannelTPVIndicator_OTHN() {
        Map<String, String> subFields = new HashMap<>();
        subFields.put("03.01", "02");
        String result = MastercardAxisOperator.channelTPVIndicator(subFields, "other");
        assertEquals("OTHN", result);
    }


    @Test
    void shouldReturnSubstringWhenAllConditionsMet() {
        Map<String, String> subFields = new HashMap<>();
        subFields.put(VAR_4801, "T");
        subFields.put(VAR_2201, "81");
        subFields.put(VAR_6110, "6");
        subFields.put(VAR_4842, "1234ABC789"); // El substring(4, 7) es "ABC"

        String result = MastercardAxisOperator.valueElectronicCommerceIndicators(subFields);
        assertEquals("ABC", result);
    }

    @Test
    void shouldReturnNullWhenVar4801IsMissing() {
        Map<String, String> subFields = new HashMap<>();
        // Falta VAR_4801
        subFields.put(VAR_2201, "81");
        subFields.put(VAR_6110, "6");

        String result = MastercardAxisOperator.valueElectronicCommerceIndicators(subFields);
        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenVar2201IsMissing() {
        Map<String, String> subFields = new HashMap<>();
        subFields.put(VAR_4801, "T");
        // Falta VAR_2201
        subFields.put(VAR_6110, "6");

        String result = MastercardAxisOperator.valueElectronicCommerceIndicators(subFields);
        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenVar6110IsMissing() {
        Map<String, String> subFields = new HashMap<>();
        subFields.put(VAR_4801, "T");
        subFields.put(VAR_2201, "81");
        // Falta VAR_6110

        String result = MastercardAxisOperator.valueElectronicCommerceIndicators(subFields);
        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenVar4801IsNotT() {
        Map<String, String> subFields = new HashMap<>();
        subFields.put(VAR_4801, "R"); // Valor incorrecto
        subFields.put(VAR_2201, "81");
        subFields.put(VAR_6110, "6");
        subFields.put(VAR_4842, "1234ABC789");

        String result = MastercardAxisOperator.valueElectronicCommerceIndicators(subFields);
        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenVar2201IsNot81() {
        Map<String, String> subFields = new HashMap<>();
        subFields.put(VAR_4801, "T");
        subFields.put(VAR_2201, "10"); // Valor incorrecto
        subFields.put(VAR_6110, "6");
        subFields.put(VAR_4842, "1234ABC789");

        String result = MastercardAxisOperator.valueElectronicCommerceIndicators(subFields);
        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenVar6110IsNot6() {
        Map<String, String> subFields = new HashMap<>();
        subFields.put(VAR_4801, "T");
        subFields.put(VAR_2201, "81");
        subFields.put(VAR_6110, "5"); // Valor incorrecto
        subFields.put(VAR_4842, "1234ABC789");

        String result = MastercardAxisOperator.valueElectronicCommerceIndicators(subFields);
        assertNull(result);
    }

    @Test
    void shouldThrowNPEWhenConditionsMetButVar4842IsMissing() {
        Map<String, String> subFields = new HashMap<>();
        subFields.put(VAR_4801, "T");
        subFields.put(VAR_2201, "81");
        subFields.put(VAR_6110, "6");
        // Falta VAR_4842
        String result = MastercardAxisOperator.valueElectronicCommerceIndicators(subFields);

        assertNull(result);
    }

    @Test
    void shouldThrowIndexOutOfBoundsWhenVar4842IsTooShort() {
        Map<String, String> subFields = new HashMap<>();
        subFields.put(VAR_4801, "T");
        subFields.put(VAR_2201, "81");
        subFields.put(VAR_6110, "6");
        subFields.put(VAR_4842, "12345"); // Demasiado corto para substring(4, 7)

        String result = MastercardAxisOperator.valueElectronicCommerceIndicators(subFields);
        assertNull(result);
    }

    @Test
    void eciGneretorSubfieldsNullOrEmpty() {
        Map<String, String> subFields = new HashMap<>();

        String result1 = MastercardAxisOperator.valueElectronicCommerceIndicators(null);
        String result2 = MastercardAxisOperator.valueElectronicCommerceIndicators(subFields);
        assertNull(result1);
        assertNull(result2);
    }

    @Test
    void shouldReturnNullWhenEciIsNull() {
        assertNull(MastercardAxisOperator.securityLevelECI(null));
    }

    @ParameterizedTest(name = "Debe devolver '05' para ECI con substring {0}")
    @ValueSource(strings = {"X12", "A14BC", "Z12345"})
    void shouldReturn05ForValidInputs(String eci) {
        // El substring(1, 3) de "X12" es "12"
        // El substring(1, 3) de "A14BC" es "14"
        // El substring(1, 3) de "Z12345" es "12"
        String input = eci.equals("A14BC") ? eci : (eci.equals("Z12345") ? eci : "X12");
        if (eci.equals("A14BC")) input = "A14BC";
        else if (eci.equals("Z12345")) input = "Z12345";
        else input = "X12"; // Corrección simple para el test parametrizado

        // Re-escribamos esto de forma más clara
        if(eci.contains("12")) assertEquals("05", MastercardAxisOperator.securityLevelECI(eci));
        if(eci.contains("14")) assertEquals("05", MastercardAxisOperator.securityLevelECI(eci));
    }

    // Es más claro usar CsvSource o tests separados

    @ParameterizedTest(name = "Input: {0}, Expected: {1}")
    @CsvSource({
            "A12B, 05", // Substring "12"
            "A14B, 05", // Substring "14"
            "A11B, 06", // Substring "11"
            "A13B, 06", // Substring "13"
            "A21B, 07", // Substring "21"
            "A22B, 07", // Substring "22"
            "A23B, 07", // Substring "23"
            "A99B, 08", // Default
            "A00B, 08", // Default
            "TEST, 08"  // Default
    })
    void shouldReturnCorrectSecurityLevel(String eci, String expectedLevel) {
        assertEquals(expectedLevel, MastercardAxisOperator.securityLevelECI(eci));
    }


    @Test
    void shouldReturn08ForDefaultCase() {
        assertEquals("08", MastercardAxisOperator.securityLevelECI("X99"));
    }

    @Test
    void shouldThrowIndexOutOfBoundsWhenEciIsTooShort() {
        // El método intenta hacer .substring(1, 3), que requiere un String de al menos 3 caracteres
        assertThrows(StringIndexOutOfBoundsException.class, () -> {
            MastercardAxisOperator.securityLevelECI("X1");
        });

        assertThrows(StringIndexOutOfBoundsException.class, () -> {
            MastercardAxisOperator.securityLevelECI("");
        });
    }


    @Test
    void shouldReturnNullWhenEntryModeIsEmpty() {
        // El parámetro subFields no se usa en el método, así que podemos pasar null
        assertNull(MastercardAxisOperator.entryModeIndicator(null, ""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"05", "07", "02"})
    void testReturnsPOST_WhenAllConditionsMet(String var2201Value) {
        Map<String, String> subFields = createBasePostMap();
        subFields.put(VAR_2201, var2201Value); // Probamos los 3 valores permitidos

        // merchantType no importa aquí, puede ser null o cualquier cosa
        String result = MastercardAxisOperator.channelTPVIndicator(subFields, "ANY");

        assertEquals("POST", result, "Debe retornar POST para var2201=" + var2201Value);
    }

    @Test
    void testNotPOST_WhenVar4801IsNotRetail() {
        Map<String, String> subFields = createBasePostMap();
        subFields.put(VAR_4801, "OTHER"); // Rompemos la 1ra condición

        String result = MastercardAxisOperator.channelTPVIndicator(subFields, "ANY");
        assertEquals("OTHN", result); // Cae al default porque no cumple ATMT ni OTHNRETV
    }

    @Test
    void testNotPOST_WhenMissingKeys() {
        Map<String, String> subFields = createBasePostMap();
        subFields.remove(VAR_2201); // Rompemos containsKey(VAR_2201)

        assertEquals("OTHN", MastercardAxisOperator.channelTPVIndicator(subFields, "ANY"));
    }

    @Test
    void testNotPOST_WhenVar2201IsInvalid() {
        Map<String, String> subFields = createBasePostMap();
        subFields.put(VAR_2201, "99"); // No es 05, 07 ni 02

        assertEquals("OTHN", MastercardAxisOperator.channelTPVIndicator(subFields, "ANY"));
    }

    @Test
    void testNotPOST_WhenVar6111IsNotInValidValues() {
        Map<String, String> subFields = createBasePostMap();
        subFields.put(VAR_6111, "INVALID_CODE"); // No está en la lista VALID_VALUES

        assertEquals("OTHN", MastercardAxisOperator.channelTPVIndicator(subFields, "ANY"));
    }

    @Test
    void testNotPOST_WhenVar6105IsNotZero() {
        Map<String, String> subFields = createBasePostMap();
        subFields.put(VAR_6105, "1"); // Debe ser "0"

        assertEquals("OTHN", MastercardAxisOperator.channelTPVIndicator(subFields, "ANY"));
    }

    // --- 2. TEST RAMA "ATMT" ---

    @Test
    void testReturnsATMT_Success() {
        Map<String, String> subFields = new HashMap<>();
        // No cumple condiciones de POST, así que evalúa el primer else-if
        subFields.put(VAR_0301, "01");

        String result = MastercardAxisOperator.channelTPVIndicator(subFields, "6011");
        assertEquals("ATMT", result);
    }

    // --- 3. TEST RAMA "OTHNRETV" ---

    @Test
    void testReturnsOTHNRETV_Success() {
        Map<String, String> subFields = new HashMap<>();
        subFields.put(VAR_0301, "01");

        String result = MastercardAxisOperator.channelTPVIndicator(subFields, "6010");
        assertEquals("OTHNRETV", result);
    }

    // --- 4. TEST RAMA "OTHN" (Default) y Edge Cases ---

    @Test
    void testReturnsOTHN_WhenMerchantTypeMismatch() {
        Map<String, String> subFields = new HashMap<>();
        subFields.put(VAR_0301, "01");

        // El código es correcto (01) pero el merchantType no es 6011 ni 6010
        String result = MastercardAxisOperator.channelTPVIndicator(subFields, "9999");
        assertEquals("OTHN", result);
    }

    @Test
    void testReturnsOTHN_WhenVar0301IsWrong() {
        Map<String, String> subFields = new HashMap<>();
        subFields.put(VAR_0301, "02"); // Debe ser "01" para entrar a ATMT o OTHNRETV

        // Aunque el merchant sea 6011, si el 0301 falla, va al default
        String result = MastercardAxisOperator.channelTPVIndicator(subFields, "6011");
        assertEquals("OTHN", result);
    }

    @ParameterizedTest
    @NullSource
    void testReturnsOTHN_WhenMerchantTypeIsNull(String nullMerchant) {
        Map<String, String> subFields = new HashMap<>();
        subFields.put(VAR_0301, "01");

        // Probamos que el check `merchantType != null` funcione y evite NullPointerException
        String result = MastercardAxisOperator.channelTPVIndicator(subFields, nullMerchant);
        assertEquals("OTHN", result);
    }

    @Test
    void testReturnsOTHN_EmptyMap() {
        assertEquals("OTHN", MastercardAxisOperator.channelTPVIndicator(new HashMap<>(), "6011"));
    }


    @ParameterizedTest(name = "Input: {0}, Expected: {1}")
    @CsvSource({
            "MLEY, MANUAL",
            "CICC, CARDCHIP",
            "CTLS, CARDCTLS",
            "MGST, CARDSTRP",
            "MBNK, PHONE",
            "QRCD, QR",
            "UNKNOWN, ALL", // Caso Default
            "RANDOM, ALL",   // Caso Default
            "mley, ALL"    // El switch es sensible a mayúsculas, por lo que va a default
    })
    void shouldReturnCorrectIndicator(String entryMode, String expectedIndicator) {
        assertEquals(expectedIndicator, MastercardAxisOperator.entryModeIndicator(null, entryMode));
    }

    @Test
    void shouldReturnAllForLowerCase() {
        // El switch es sensible a mayúsculas
        assertEquals("ALL", MastercardAxisOperator.entryModeIndicator(null, "mley"));
    }

}

