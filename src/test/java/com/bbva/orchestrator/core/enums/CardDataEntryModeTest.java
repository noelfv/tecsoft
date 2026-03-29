package com.bbva.orchestrator.core.enums;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
class CardDataEntryModeTest {

    // --- Pruebas para el método convertCardDataEntryMode ---
    // Este método, por su nombre, parece diseñado para convertir de código a acrónimo.

    @Test
    void convertCardDataEntryMode_whenCodeIsValid_shouldReturnCorrectAcronym() {
        assertThat(CardDataEntryMode.convertCardDataEntryMode("00")).isEqualTo("UNSP");
        assertThat(CardDataEntryMode.convertCardDataEntryMode("01")).isEqualTo("MLEY");
        assertThat(CardDataEntryMode.convertCardDataEntryMode("02")).isEqualTo("MGST");
        assertThat(CardDataEntryMode.convertCardDataEntryMode("03")).isEqualTo("OPTC");
        assertThat(CardDataEntryMode.convertCardDataEntryMode("04")).isEqualTo("OCRR");
        assertThat(CardDataEntryMode.convertCardDataEntryMode("05")).isEqualTo("ICCY");
        assertThat(CardDataEntryMode.convertCardDataEntryMode("07")).isEqualTo("ICPY");
        // Nota: El valor ENUM_10 tiene la clave "20", no "10".
        assertThat(CardDataEntryMode.convertCardDataEntryMode("10")).isEqualTo("DFLE");
    }

    // --- Pruebas para el método convertTypeCardDataEntryMode ---
    // Este método parece diseñado para convertir de acrónimo a código.

    @Test
    void convertTypeCardDataEntryMode_whenAcronymIsValid_shouldReturnCorrectCode() {
        assertThat(CardDataEntryMode.convertTypeCardDataEntryMode("UNSP")).isEqualTo("00");
        assertThat(CardDataEntryMode.convertTypeCardDataEntryMode("MLEY")).isEqualTo("01");
        assertThat(CardDataEntryMode.convertTypeCardDataEntryMode("MGST")).isEqualTo("91");
        assertThat(CardDataEntryMode.convertTypeCardDataEntryMode("OPTC")).isEqualTo("03");
        assertThat(CardDataEntryMode.convertTypeCardDataEntryMode("OCRR")).isEqualTo("04");
        assertThat(CardDataEntryMode.convertTypeCardDataEntryMode("ICCY")).isEqualTo("05");
        assertThat(CardDataEntryMode.convertTypeCardDataEntryMode("ICPY")).isEqualTo("07");
        assertThat(CardDataEntryMode.convertTypeCardDataEntryMode("DFLE")).isEqualTo("10");
        assertThat(CardDataEntryMode.convertTypeCardDataEntryMode("OTHN")).isEqualTo("82");
    }

    // --- Pruebas para casos de error y borde ---

    @Test
    void conversionMethods_whenKeyIsInvalid_shouldReturnNull() {
        // Probando con una clave numérica inválida
        assertThat(CardDataEntryMode.convertCardDataEntryMode("99")).isNull();

        // Probando con un acrónimo inválido
        assertThat(CardDataEntryMode.convertTypeCardDataEntryMode("INVALID")).isNull();

        // Probando con una cadena vacía
        assertThat(CardDataEntryMode.convertCardDataEntryMode("")).isNull();
        assertThat(CardDataEntryMode.convertTypeCardDataEntryMode("")).isNull();
    }

    @Test
    void conversionMethods_whenInputIsNull_shouldReturnNull() {
        assertThat(CardDataEntryMode.convertCardDataEntryMode(null)).isNull();
        assertThat(CardDataEntryMode.convertTypeCardDataEntryMode(null)).isNull();
    }

    @Test
    void conversionMethods_shouldBeInterchangeable() {
        // Demostramos que el primer método también puede convertir de acrónimo a código
        assertThat(CardDataEntryMode.convertCardDataEntryMode("UNSP")).isEqualTo("00");

        // Y el segundo método también puede convertir de código a acrónimo
        assertThat(CardDataEntryMode.convertTypeCardDataEntryMode("01")).isEqualTo("MLEY");
    }
}