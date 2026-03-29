package com.bbva.orchestrator.core.logic.impl;

import com.bbva.orchestrator.configuration.ApplicationDataLocalCache;
import com.bbva.orchestrator.core.dto.ISO8583;
import com.bbva.orchestrator.core.fields.MastercardISOField;
import com.bbva.orchestrator.core.fields.VisaISOField;
import com.bbva.orchestrator.core.logic.factory.impl.MastercardDelegateFieldLogic;
import com.bbva.orchestrator.core.logic.process.MastercardProcessSubField;
import com.bbva.orchestrator.core.parser.iso8583.strategy.subfields.CompositeFixedFieldParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MastercardFieldLogicTest {

    @Mock
    private ApplicationDataLocalCache applicationDataLocalCache;

    @Mock
    private CompositeFixedFieldParser compositeFieldParser;

    @InjectMocks
    private MastercardDelegateFieldLogic mastercardDelegateFieldLogic;

    @Mock
    private MastercardProcessSubField mastercardISOSubFieldParser;

    @Nested
    @DisplayName("Pruebas para el método parseSubfields()")
    class ParseSubfieldsTests {

        @Test
        void shouldParseAndCombineSubfieldsForValidMessageType() {
            // Arrange
            ISO8583 iso8583 = ISO8583.builder()
                    .messageType("0100")
                    .processingCode("000000")
                    .pointServiceEntryMode("051")
                    .additionalAmounts("SomeAmount")
                    .posCardIssuer("SomePosData")
                    .build();

            when(mastercardISOSubFieldParser.parseSubfields(any(ISO8583.class))).thenReturn(Map.of("key2", "value2"));

            // Act
            Map<String, String> result = mastercardDelegateFieldLogic.parseSubfields(iso8583);

            // Assert
            assertThat(result).isNotNull();
            verify(mastercardISOSubFieldParser, times(1)).parseSubfields(any(ISO8583.class));
        }

        @Test
        void shouldReturnEmptyMapForNonProcessableMessageType() {
            // Arrange
            ISO8583 iso8583 = ISO8583.builder()
                    .messageType("0800")
                    .build();

            // Act
            Map<String, String> result = mastercardDelegateFieldLogic.parseSubfields(iso8583);

            // Assert
            assertThat(result).isNotNull().isEmpty();
            verify(compositeFieldParser, never()).buildSubFieldsSpecific(anyString(), anyString());
        }
    }

    @Nested
    class ApplyLogicFieldsTests {

        @Test
        @DisplayName("Debería devolver un mapa con los campos requeridos y sin 'header'")
        void shouldReturnMapWithRequiredFieldsAndWithoutHeader() {
            // Arrange
            Map<String, String> inputMap = new HashMap<>();
            inputMap.put("networkName", "mastercard");
            inputMap.put("messageType", "0110");
            inputMap.put("header", "HEADER_DATA"); // Este campo no debería ser devuelto
            inputMap.put("processingCode", "000000");

            Map<Integer, String> fieldsResponse = Map.of(3, "M");
            when(applicationDataLocalCache.getFieldsResponse("mastercard", "0110")).thenReturn(fieldsResponse);

            try (MockedStatic<MastercardISOField> mockedMastercardField = mockStatic(MastercardISOField.class)) {
                MastercardISOField mockField3 = mock(MastercardISOField.class);

                // Clave: que el campo 3 resuelva al nombre "processingCode"
                when(mockField3.getName()).thenReturn("processingCode");
                mockedMastercardField.when(() -> MastercardISOField.getById(3)).thenReturn(mockField3);

                // Act
                Map<String, String> result = mastercardDelegateFieldLogic.applyLogicFields(inputMap);

                // Assert
                assertThat(result)
                        .isNotNull()
                        .containsEntry("messageType", "0110")
                        .containsEntry("processingCode", "000000")
                        .doesNotContainKey("header");
            }
        }

        @Test
        void shouldReturnOnlyMessageTypeWhenCacheReturnsNoFields() {
            // Arrange
            Map<String, String> inputMap = new HashMap<>();
            inputMap.put("networkName", "mastercard");
            inputMap.put("messageType", "0110");
            inputMap.put("processingCode", "000000");

            when(applicationDataLocalCache.getFieldsResponse("mastercard", "0110")).thenReturn(Collections.emptyMap());

            // Act
            Map<String, String> result = mastercardDelegateFieldLogic.applyLogicFields(inputMap);

            // Assert
            assertThat(result)
                    .isNotNull()
                    .hasSize(2)
                    .containsEntry("messageType", "0110");
        }
    }

    @Nested
    class ApplyLogicField48Tests {

        // Helper para invocar el método privado usando Reflexión
        private String invokeApplyLogicField48(String messageType, Map<String, String> mapValues) throws Exception {
            Method method = MastercardDelegateFieldLogic.class.getDeclaredMethod("applyLogicField48", String.class, Map.class);
            method.setAccessible(true);
            return (String) method.invoke(mastercardDelegateFieldLogic, messageType, mapValues);
        }

        @Test
        @DisplayName("Debería añadir sufijo D4 cuando el mensaje es 0110 y el tag 48.87 es 'true'")
        void shouldAppendD4WhenMsgTypeIs0110AndTag87IsTrue() throws Exception {
            // Arrange
            String messageType = "0110";
            Map<String, String> mapValues = Map.of(
                    "additionalDataRetailer", "E2",
                    "48.87", "true"
            );

            // Act
            String result = invokeApplyLogicField48(messageType, mapValues);

            // Assert
            assertThat(result).isEqualTo("E2F8F7F0F1D4");
        }

        @Test
        void shouldAppendD4WhenMsgTypeIs0110AndTag87_2IsTrue() throws Exception {
            // Arrange
            String messageType = "0110";
            Map<String, String> mapValues = Map.of(
                    "additionalDataRetailer", "E2",
                    "48.87_2", "true"
            );

            // Act
            String result = invokeApplyLogicField48(messageType, mapValues);

            // Assert
            assertThat(result).isEqualTo("E2F8F7F0F1D4");
        }

        @Test
        void shouldAppendD4WhenMsgTypeIs0110AndTag87_2IsFalse() throws Exception {
            // Arrange
            String messageType = "0110";
            Map<String, String> mapValues = Map.of(
                    "additionalDataRetailer", "E2",
                    "48.87_2", "false"
            );

            // Act
            String result = invokeApplyLogicField48(messageType, mapValues);

            // Assert
            assertThat(result).isEqualTo("E2F8F7F0F1D5");
        }

        @Test
        void shouldAppendD4WhenMsgTypeIs0110AndTag87_iIsTrue() throws Exception {
            // Arrange
            String messageType = "0110";
            Map<String, String> mapValues = Map.of(
                    "additionalDataRetailer", "E2",
                    "48.87_i", "true"
            );

            // Act
            String result = invokeApplyLogicField48(messageType, mapValues);

            // Assert
            assertThat(result).isEqualTo("E2F8F7F0F1D4");
        }

        @Test
        void shouldAppendD4WhenMsgTypeIs0110AndTag87_iIsFalse() throws Exception {
            // Arrange
            String messageType = "0110";
            Map<String, String> mapValues = Map.of(
                    "additionalDataRetailer", "E2",
                    "48.87_i", "false"
            );

            // Act
            String result = invokeApplyLogicField48(messageType, mapValues);

            // Assert
            assertThat(result).isEqualTo("E2F8F7F0F1D5");
        }

        @Test
        @DisplayName("Debería añadir sufijo D4 cuando el mensaje es 0110 y el tag 48.87_d es 'true'")
        void shouldAppendD4WhenMsgTypeIs0110AndTag87_d_IsTrue() throws Exception {
            // Arrange
            String messageType = "0110";
            Map<String, String> mapValues = Map.of(
                    "additionalDataRetailer", "E2",
                    "48.87_d", "true"
            );

            // Act
            String result = invokeApplyLogicField48(messageType, mapValues);

            // Assert
            assertThat(result).isEqualTo("E2F8F7F0F1D4");
        }

        @Test
        void shouldAppendD5WhenMsgTypeIs0110AndTag87IsFalse() throws Exception {
            // Arrange
            String messageType = "0110";
            Map<String, String> mapValues = Map.of(
                    "additionalDataRetailer", "E2",
                    "48.87", "false"
            );

            // Act
            String result = invokeApplyLogicField48(messageType, mapValues);

            // Assert
            assertThat(result).isEqualTo("E2F8F7F0F1D5");
        }

        @Test
        void shouldAppendD5WhenMsgTypeIs0110AndTag87_d_IsFalse() throws Exception {
            // Arrange
            String messageType = "0110";
            Map<String, String> mapValues = Map.of(
                    "additionalDataRetailer", "E2",
                    "48.87_d", "false"
            );

            // Act
            String result = invokeApplyLogicField48(messageType, mapValues);

            // Assert
            assertThat(result).isEqualTo("E2F8F7F0F1D5");
        }

        @Test
        void shouldReturnOriginalValueWhenMsgTypeIsNot0110() throws Exception {
            // Arrange
            String messageType = "0100";
            Map<String, String> mapValues = Map.of(
                    "additionalDataRetailer", "DATA",
                    "48.87", "true"
            );

            // Act
            String result = invokeApplyLogicField48(messageType, mapValues);

            // Assert
            assertThat(result).isEqualTo("DATA");
        }

        @Test
        void shouldReturnOriginalValueWhenTag87IsMissing() throws Exception {
            // Arrange
            String messageType = "0110";
            Map<String, String> mapValues = Map.of(
                    "additionalDataRetailer", "E3F2F3F0F2F0F1F4F2F0F7F0F1F0F3F2F1F2F4F5F0F1F0F7F1F0F4F0F5E540F7F5F3F2F0F1F0F3F0F4F0F0F2F0F2F0F0F0F3F0F3F0F4F0F0F4F0F2F0F0F0F5F0F2F0F0F7F7F0F0F9F2F0F3F4F2F3"
            );

            // Act
            String result = invokeApplyLogicField48(messageType, mapValues);

            // Assert
            assertThat(result).isEqualTo("E3F4F2F0F7F0F1F0F3F2F1F2F9F2F0F3F4F2F3");
        }
    }
}