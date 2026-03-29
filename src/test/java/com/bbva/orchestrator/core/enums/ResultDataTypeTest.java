package com.bbva.orchestrator.core.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ResultDataTypeTest {

    // --- Pruebas para el método convertCardDataEntryMode ---
    // Este método, por su nombre, parece diseñado para convertir de código a acrónimo.

    @DisplayName("convertResultDataType() debe devolver el valor esperado para claves válidas")
    @Test
    void convertResultDataTypeReturnsExpectedValueForKnownKeys() {
        assertThat(ResultaDataType.convertResultDataType("0100")).isEqualTo("APPR");
        assertThat(ResultaDataType.convertResultDataType("0101")).isEqualTo("APPR");
        assertThat(ResultaDataType.convertResultDataType("0110")).isEqualTo("APPR");

        assertThat(ResultaDataType.convertResultDataType("0120")).isEqualTo("PRCS");
        assertThat(ResultaDataType.convertResultDataType("0130")).isEqualTo("PRCS");
        assertThat(ResultaDataType.convertResultDataType("0420")).isEqualTo("PRCS");
        assertThat(ResultaDataType.convertResultDataType("0430")).isEqualTo("PRCS");

        assertThat(ResultaDataType.convertResultDataType("0400")).isEqualTo("SUCC");
        assertThat(ResultaDataType.convertResultDataType("0401")).isEqualTo("SUCC");
        assertThat(ResultaDataType.convertResultDataType("0410")).isEqualTo("SUCC");

        assertThat(ResultaDataType.convertResultDataType("0302")).isEqualTo("UKNW");
        assertThat(ResultaDataType.convertResultDataType("0312")).isEqualTo("UKNW");
    }

    @DisplayName("convertResultDataType() debe devolver null cuando la clave no existe")
    @Test
    void convertResultDataTypeReturnsNullForUnknownKey() {
        assertThat(ResultaDataType.convertResultDataType("9999")).isNull();
        assertThat(ResultaDataType.convertResultDataType("")).isNull();
        assertThat(ResultaDataType.convertResultDataType("01100")).isNull();
        assertThat(ResultaDataType.convertResultDataType("011")).isNull();
    }

    @DisplayName("convertResultDataType() debe devolver null cuando la entrada es null")
    @Test
    void convertResultDataTypeReturnsNullWhenInputIsNull() {
        assertThat(ResultaDataType.convertResultDataType(null)).isNull();
    }

    @DisplayName("convertResultDataType() debe ser sensible a espacios (no debe hacer trim implícito)")
    @Test
    void convertResultDataTypeReturnsNullWhenKeyHasWhitespace() {
        assertThat(ResultaDataType.convertResultDataType(" 0110")).isNull();
        assertThat(ResultaDataType.convertResultDataType("0110 ")).isNull();
        assertThat(ResultaDataType.convertResultDataType("01 10")).isNull();
    }

    @DisplayName("convertResultDataType() debe ser sensible a mayúsculas/minúsculas en el código")
    @Test
    void convertResultDataTypeIsCaseSensitiveForKey() {
        assertThat(ResultaDataType.convertResultDataType("appr")).isNull();
        assertThat(ResultaDataType.convertResultDataType("APPR")).isNull();
    }
}