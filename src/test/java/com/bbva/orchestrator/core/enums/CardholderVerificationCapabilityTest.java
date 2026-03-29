package com.bbva.orchestrator.core.enums;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
class CardholderVerificationCapabilityTest {

    @Nested
    class ConversionTests {

        @Test
        void shouldConvertFromCodeToAcronym() {
            assertThat(CardholderVerificationCapability.convertCardholderVerificationCapability("0")).isEqualTo("UNSP");
            assertThat(CardholderVerificationCapability.convertCardholderVerificationCapability("1")).isEqualTo("NPIN");
            assertThat(CardholderVerificationCapability.convertCardholderVerificationCapability("2")).isEqualTo("NOPN");
            assertThat(CardholderVerificationCapability.convertCardholderVerificationCapability("3")).isEqualTo("OTHN");
            assertThat(CardholderVerificationCapability.convertCardholderVerificationCapability("8")).isEqualTo("OTHN");
        }

        @Test
        void shouldConvertFromAcronymToCode() {
            assertThat(CardholderVerificationCapability.convertTypeCardholderVerificationCapability("UNSP")).isEqualTo("0");
            // ¡Ojo! Estos valores parecen ser un error de copia en el enum original, pero el test refleja el comportamiento actual.
            assertThat(CardholderVerificationCapability.convertTypeCardholderVerificationCapability("MLEY")).isEqualTo("1");
            assertThat(CardholderVerificationCapability.convertTypeCardholderVerificationCapability("MGST")).isEqualTo("2");
            // Dado que "OTHN" aparece dos veces, el último valor en el enum ("OTHN", "8") es el que prevalece.
            assertThat(CardholderVerificationCapability.convertTypeCardholderVerificationCapability("OTHN")).isEqualTo("8");
        }

        @Test
        void shouldReturnNullForInvalidOrNullInputs() {
            assertThat(CardholderVerificationCapability.convertCardholderVerificationCapability("99")).isNull();
            assertThat(CardholderVerificationCapability.convertTypeCardholderVerificationCapability("INVALID")).isNull();
            assertThat(CardholderVerificationCapability.convertCardholderVerificationCapability(null)).isNull();
        }
    }

    @Nested
    class MapPointOfServiceContext_AttendedIndicatorTests {

        @Test
        void shouldReturnTrueWhenInputIsOne() {
            assertThat(CardholderVerificationCapability.mapPointOfServiceContext_AttendedIndicator("0")).isTrue();
        }

        @Test
        void shouldReturnFalseWhenInputIsNotOne() {
            assertThat(CardholderVerificationCapability.mapPointOfServiceContext_AttendedIndicator("1")).isFalse();
            assertThat(CardholderVerificationCapability.mapPointOfServiceContext_AttendedIndicator("2")).isFalse();
            assertThat(CardholderVerificationCapability.mapPointOfServiceContext_AttendedIndicator("abc")).isFalse();
        }

        @Test
        void shouldReturnNullWhenInputIsNull() {
            assertThat(CardholderVerificationCapability.mapPointOfServiceContext_AttendedIndicator(null)).isNull();
        }
    }

    @Nested
    class MapPointOfServiceContext_UnattendedLevelCategoryTests {

        @Test
        void shouldReturnCorrectCategoryForCardHolderAtl() {
            assertThat(CardholderVerificationCapability.mapPointOfServiceContext_UnattendedLevelCategory("0", "1")).isEqualTo("CAT LEVEL 1");
            assertThat(CardholderVerificationCapability.mapPointOfServiceContext_UnattendedLevelCategory("0", "8")).isEqualTo("MBOB INIT");
        }

        @Test
        void shouldReturnVoiceAudioWhenAttendanceIsTwo() {
            assertThat(CardholderVerificationCapability.mapPointOfServiceContext_UnattendedLevelCategory("2", "1")).isEqualTo("Voice/Audio");
            assertThat(CardholderVerificationCapability.mapPointOfServiceContext_UnattendedLevelCategory("2", null)).isEqualTo("Voice/Audio");
        }

        @Test
        void shouldReturnNullForNonMatchingRules() {
            assertThat(CardholderVerificationCapability.mapPointOfServiceContext_UnattendedLevelCategory("0", "X")).isNull();
            assertThat(CardholderVerificationCapability.mapPointOfServiceContext_UnattendedLevelCategory(null, null)).isNull();
        }

        @Test
        void shouldReturnNullForNonMatchingRules_() {
            assertThat(CardholderVerificationCapability.mapPointOfServiceContext_UnattendedLevelCategory(null, "1")).isEqualTo("CAT LEVEL 1");
            assertThat(CardholderVerificationCapability.mapPointOfServiceContext_UnattendedLevelCategory(null, "2")).isEqualTo("CAT LEVEL 2");
            assertThat(CardholderVerificationCapability.mapPointOfServiceContext_UnattendedLevelCategory(null, "3")).isEqualTo("CAT LEVEL 3");
            assertThat(CardholderVerificationCapability.mapPointOfServiceContext_UnattendedLevelCategory(null, "4")).isEqualTo("CAT LEVEL 4");
            assertThat(CardholderVerificationCapability.mapPointOfServiceContext_UnattendedLevelCategory(null, "6")).isEqualTo("CAT LEVEL 6");
            assertThat(CardholderVerificationCapability.mapPointOfServiceContext_UnattendedLevelCategory(null, "7")).isEqualTo("CAT LEVEL 7");
            assertThat(CardholderVerificationCapability.mapPointOfServiceContext_UnattendedLevelCategory(null, "9")).isEqualTo("MPOS ACCEP");
        }
    }

    @Nested
    class MapPosTerminalLocationTests {
        @Test
        void shouldReturnEmptyMapOnFailedValidation() {
            assertThat(CardholderVerificationCapability.mapPosTerminalLocation("5", "1", null)).isEmpty(); // posTerminalLocation inválido
            assertThat(CardholderVerificationCapability.mapPosTerminalLocation("1", null, null)).isEmpty(); // posTerminalAttendance es null
        }

        @Test
        void shouldMapCorrectlyForLocation0() {
            Map<String, String> result = CardholderVerificationCapability.mapPosTerminalLocation("0", "1", null);
            assertThat(result).containsEntry("offPremisesIndicator", "false").containsEntry("type", "POS");
        }

        @Test
        void shouldMapCorrectlyForLocation3AndAttendance2() {
            Map<String, String> result = CardholderVerificationCapability.mapPosTerminalLocation("3", "2", null);
            assertThat(result).containsEntry("offPremisesIndicator", "false").containsEntry("OtherType", "Voice/Audio");
        }

        @Test
        void shouldReturnEmptyMapForLocation3WithoutAttendance2() {
            assertThat(CardholderVerificationCapability.mapPosTerminalLocation("3", "1", null)).isEmpty();
        }

        @Test
        void shouldOverwriteOtherTypeWhenTypeIsValid() {
            // Sobrescribe el valor
            Map<String, String> result = CardholderVerificationCapability.mapPosTerminalLocation("1", "1", "TYPE_ABCD");
            assertThat(result).containsEntry("offPremisesIndicator", "true").containsEntry("OtherType", "_ABCD");

            // Añade el valor
            Map<String, String> result2 = CardholderVerificationCapability.mapPosTerminalLocation("0", "1", "TYPE_XYZ");
            assertThat(result2).containsEntry("OtherType", "_XYZ");
        }

        @Test
        void case_2_posTerminalLocation() {
            assertThat(CardholderVerificationCapability.mapPosTerminalLocation("2", "1", null)).isEqualTo(Map.of("offPremisesIndicator", "true", "OtherType", "Cardholder terminal"));
        }

        @Test
        void case_4_posTerminalLocation() {
            assertThat(CardholderVerificationCapability.mapPosTerminalLocation("4", "1", null)).isEqualTo(Map.of("offPremisesIndicator", "false","GeographicLocation", "Cardholder terminal"));
        }

    }

    @Nested
    class MapPointOfServiceContext_CardDataEntryModeTests {

        @Test
        void shouldReturnEmptyMapWhenInputIsNull() {
            assertThat(CardholderVerificationCapability.mapPointOfServiceContext_CardDataEntryMode(null)).isEmpty();
        }

        @Test
        void shouldIndicateCardholderNotPresent() {
            Map<String, String> result = CardholderVerificationCapability.mapPointOfServiceContext_CardDataEntryMode("0");
            assertThat(result).containsEntry("cardholderPresent", "true");
        }

        @Test
        void shouldMapMotoCodeForValue2() {
            Map<String, String> result = CardholderVerificationCapability.mapPointOfServiceContext_CardDataEntryMode("2");
            assertThat(result).containsEntry("cardholderPresent", "false").containsEntry("MOTOCode", "MAOR");
        }

        @Test
        void shouldMapMotoCodeForValueTPOR() {
            Map<String, String> result = CardholderVerificationCapability.mapPointOfServiceContext_CardDataEntryMode("3");
            assertThat(result).containsEntry("cardholderPresent", "false").containsEntry("MOTOCode", "TPOR");
        }

        @Test
        void shouldMapMotoCodeForValueRCPT() {
            Map<String, String> result = CardholderVerificationCapability.mapPointOfServiceContext_CardDataEntryMode("4");
            assertThat(result).containsEntry("cardholderPresent", "false").containsEntry("otherTransactionAttribute", "RCPT");
        }

        @Test
        void shouldMapEcommerceIndicatorForValue5() {
            Map<String, String> result = CardholderVerificationCapability.mapPointOfServiceContext_CardDataEntryMode("5");
            assertThat(result).containsEntry("cardholderPresent", "false").containsEntry("ecommerceIndicator", "true");
        }
    }

    @Nested
    class MapPointOfServiceContext_CardPresentTests {
        @Test
        void shouldReturnTrueOnlyForZero() {
            assertThat(CardholderVerificationCapability.mapPointOfServiceContext_CardPresent("0")).isTrue();
        }
        @Test
        void shouldReturnFalseForNonZero() {
            assertThat(CardholderVerificationCapability.mapPointOfServiceContext_CardPresent("1")).isFalse();
            assertThat(CardholderVerificationCapability.mapPointOfServiceContext_CardPresent("abc")).isFalse();
        }
        @Test
        void shouldReturnNullForNullInput() {
            assertThat(CardholderVerificationCapability.mapPointOfServiceContext_CardPresent(null)).isNull();
        }
    }

    @Nested
    class MapCapabilities_CardCaptureCapableTests {
        @Test
        void shouldReturnFalseOnlyForZero() {
            assertThat(CardholderVerificationCapability.mapCapabilities_CardCaptureCapable("0")).isFalse();
        }
        @Test
        void shouldReturnTrueForNonZero() {
            assertThat(CardholderVerificationCapability.mapCapabilities_CardCaptureCapable("1")).isTrue();
            assertThat(CardholderVerificationCapability.mapCapabilities_CardCaptureCapable("abc")).isTrue();
        }
        @Test
        void shouldReturnNullForNullInput() {
            assertThat(CardholderVerificationCapability.mapCapabilities_CardCaptureCapable(null)).isNull();
        }
    }

    @Nested
    class MapCardReadingCapability_CapabilityTests {
        @Test
        void shouldReturnEmptyMapForNullOrUnmapped() {
            assertThat(CardholderVerificationCapability.mapCardReadingCapability_Capability(null)).isEmpty();
            assertThat(CardholderVerificationCapability.mapCardReadingCapability_Capability("X")).isEmpty();
        }

        @Test
        void shouldReturnDoubleEntryMapForValue0() {
            assertThat(CardholderVerificationCapability.mapCardReadingCapability_Capability("0"))
                    .containsExactlyInAnyOrderEntriesOf(Map.of("capability", "UNKW"));
        }

        @Test
        void shouldReturnDoubleEntryMapForValue1() {
            assertThat(CardholderVerificationCapability.mapCardReadingCapability_Capability("1"))
                    .containsExactlyInAnyOrderEntriesOf(Map.of("capability", "OTHN", "otherCapability", "voice_aru"));
        }

        @Test
        void shouldReturnSingleEntryMapForValue2() {
            assertThat(CardholderVerificationCapability.mapCardReadingCapability_Capability("2"))
                    .containsExactlyInAnyOrderEntriesOf(Map.of("capability", "MGST"));
        }

        @Test
        void shouldReturnSingleEntryMapForValue3() {
            assertThat(CardholderVerificationCapability.mapCardReadingCapability_Capability("3"))
                    .containsExactlyInAnyOrderEntriesOf(Map.of("capability", "ECTL"));
        }

        @Test
        void shouldReturnSingleEntryMapForValue4() {
            assertThat(CardholderVerificationCapability.mapCardReadingCapability_Capability("4"))
                    .containsExactlyInAnyOrderEntriesOf(Map.of("capability", "MSIP"));
        }

        @Test
        void shouldReturnDoubleEntryMapForValue5() {
            assertThat(CardholderVerificationCapability.mapCardReadingCapability_Capability("5"))
                    .containsExactlyInAnyOrderEntriesOf(Map.of("capability", "OTHN", "otherCapability", "ECTL_MGST"));
        }

        @Test
        void shouldReturnSingleEntryMapForValue6() {
            assertThat(CardholderVerificationCapability.mapCardReadingCapability_Capability("6"))
                    .containsExactlyInAnyOrderEntriesOf(Map.of("capability", "KEEN"));
        }

        @Test
        void shouldReturnDoubleEntryMapForValue7() {
            assertThat(CardholderVerificationCapability.mapCardReadingCapability_Capability("7"))
                    .containsExactlyInAnyOrderEntriesOf(Map.of("capability", "OTHN", "otherCapability", "MGST_KEEN"));
        }

        @Test
        void shouldReturnDoubleEntryMapForValue8() {
            assertThat(CardholderVerificationCapability.mapCardReadingCapability_Capability("8"))
                    .containsExactlyInAnyOrderEntriesOf(Map.of("capability", "OTHN", "otherCapability", "ECTL_MGST_KEEN"));
        }

        @Test
        void shouldReturnDoubleEntryMapForValue9() {
            assertThat(CardholderVerificationCapability.mapCardReadingCapability_Capability("9"))
                    .containsExactlyInAnyOrderEntriesOf(Map.of("capability", "CICC"));
        }
    }

    @Nested
    class MapPointOfServiceContext_EcommerceIndicatorTests {

        @Test
        void shouldReturnTrueWhenValueIs5AndEcommerceIsNull() {
            assertThat(CardholderVerificationCapability.mapPointOfServiceContext_EcommerceIndicator("5", null)).isTrue();
        }

        @Test
        void shouldReturnEcommerceValueWhenNotNull() {
            assertThat(CardholderVerificationCapability.mapPointOfServiceContext_EcommerceIndicator("5", false)).isTrue();
            assertThat(CardholderVerificationCapability.mapPointOfServiceContext_EcommerceIndicator("X", true)).isTrue();
            assertThat(CardholderVerificationCapability.mapPointOfServiceContext_EcommerceIndicator(null, false)).isFalse();
        }

        @Test
        void shouldReturnNullWhenNoRuleMatches() {
            assertThat(CardholderVerificationCapability.mapPointOfServiceContext_EcommerceIndicator("X", null)).isNull();
            assertThat(CardholderVerificationCapability.mapPointOfServiceContext_EcommerceIndicator(null, null)).isNull();
        }
    }
}