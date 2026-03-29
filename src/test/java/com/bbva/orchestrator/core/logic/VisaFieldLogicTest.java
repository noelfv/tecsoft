package com.bbva.orchestrator.core.logic;

import com.bbva.orchestrator.configuration.ApplicationDataLocalCache;
import com.bbva.orchestrator.core.dto.ISO8583;
import com.bbva.orchestrator.core.fields.VisaISOField;
import com.bbva.orchestrator.core.logic.factory.impl.VisaDelegateFieldLogic;
import com.bbva.orchestrator.core.logic.process.VisaProcessSubField;
import com.bbva.orchestrator.core.parser.iso8583.strategy.subfields.CompositeFixedFieldParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VisaFieldLogicTest {
    @Mock
    private ApplicationDataLocalCache applicationDataLocalCache;

    @Mock
    private CompositeFixedFieldParser compositeFieldParser;

    @Mock
    private VisaProcessSubField visaProcessSubField;

    @InjectMocks
    private VisaDelegateFieldLogic visaDelegateFieldLogic;

    @Nested
    @DisplayName("Pruebas para el método parseSubfields()")
    class ParseSubfieldsTests {

        @Test
        @DisplayName("Debería parsear y combinar subcampos para un tipo de mensaje válido (ej. 0100)")
        void shouldParseAndCombineSubfieldsForValidMessageType() {
            // Arrange
            ISO8583 iso8583 = ISO8583.builder()
                    .messageType("0100")
                    .processingCode("000000")
                    .pointServiceEntryMode("051")
                    .additionalAmounts("SomeAmount")
                    .posCardIssuer("SomePosData")
                    .build();


            // Simulamos lo que devolverá el parser para cada campo
            Map<String, String> subfields03 = Map.of("3.01", "00", "3.02", "0000");
            Map<String, String> subfields22 = Map.of("22.01", "05");
            Map<String, String> subfields54 = Map.of("54.01", "USD123");
            // Simulamos una clave repetida para verificar que el último `putAll` gana
            Map<String, String> subfields61 = Map.of("61.01", "DATA", "3.01", "OVERWRITE");

            Map<String, String> parsedSubfields = Map.of(
                    "3.01", "OVERWRITE",
                    "3.02", "0000",
                    "22.01", "05",
                    "54.01", "USD123",
                    "61.01", "DATA"
            );

            when(visaProcessSubField.parseSubfields(iso8583)).thenReturn(parsedSubfields);

            // Act
            Map<String, String> result = visaDelegateFieldLogic.parseSubfields(iso8583);

            // Assert
            assertThat(result)
                    .isNotNull()
                    .hasSize(5)
                    .containsEntry("3.02", "0000")
                    .containsEntry("22.01", "05")
                    .containsEntry("54.01", "USD123")
                    .containsEntry("61.01", "DATA")
                    .containsEntry("3.01", "OVERWRITE"); // Verifica que el valor del último mapa prevalece

            // Verificamos que el método parseSubfields fue llamado una vez
            verify(visaProcessSubField, times(1)).parseSubfields(iso8583);
        }

        @Test
        @DisplayName("Debería devolver un mapa vacío para un tipo de mensaje que no requiere procesamiento (ej. 0800)")
        void shouldReturnEmptyMapForNonProcessableMessageType() {
            // Arrange
            ISO8583 iso8583 = ISO8583.builder()
                    .messageType("0800")
                    .build();

            // Act
            Map<String, String> result = visaDelegateFieldLogic.parseSubfields(iso8583);

            // Assert
            assertThat(result).isNotNull().isEmpty();
            // Verificamos que el parser nunca fue llamado
            verify(compositeFieldParser, never()).buildSubFieldsSpecific(anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Pruebas para el método applyLogicFields()")
    class ApplyLogicFieldsTests {

        @Test
        @DisplayName("Debería devolver un mapa con los campos requeridos cuando todos están presentes")
        void shouldReturnMapWithRequiredFieldsWhenAllArePresent() {
            // Arrange
            Map<String, String> inputMap = new HashMap<>();
            inputMap.put("networkName", "visa");
            inputMap.put("messageType", "0110");
            inputMap.put("header", "HEADER_DATA");
            inputMap.put("processingCode", "000000"); // Corresponde al campo 3
            inputMap.put("amountTransaction", "1000"); // Corresponde al campo 4

            // Simulamos la respuesta del caché: campo 3 es mandatorio, campo 4 es opcional
            Map<Integer, String> fieldsResponse = Map.of(3, "M", 4, "O");
            when(applicationDataLocalCache.getFieldsResponse("visa", "0110")).thenReturn(fieldsResponse);

            // Simulamos el enum estático VisaISOField
            try (MockedStatic<VisaISOField> mockedVisaField = mockStatic(VisaISOField.class)) {
                // Creamos mocks para los enums individuales que esperamos
                VisaISOField mockField3 = mock(VisaISOField.class);
                VisaISOField mockField4 = mock(VisaISOField.class);
                when(mockField3.getName()).thenReturn("processingCode");
                when(mockField4.getName()).thenReturn("amountTransaction");

                mockedVisaField.when(() -> VisaISOField.getById(3)).thenReturn(mockField3);
                mockedVisaField.when(() -> VisaISOField.getById(4)).thenReturn(mockField4);

                // Act
                Map<String, String> result = visaDelegateFieldLogic.applyLogicFields(inputMap);

                // Assert
                assertThat(result)
                        .isNotNull()
                        .hasSize(4) // header, messageType, processingCode, amountTransaction
                        .containsEntry("header", "HEADER_DATA")
                        .containsEntry("messageType", "0110")
                        .containsEntry("processingCode", "000000")
                        .containsEntry("amountTransaction", "1000");
            }
        }

        @Test
        @DisplayName("Debería devolver un mapa sin un campo mandatorio si este falta en la entrada")
        void shouldReturnMapWithoutMissingMandatoryField() {
            // Arrange
            Map<String, String> inputMap = new HashMap<>();
            inputMap.put("networkName", "visa");
            inputMap.put("messageType", "0110");
            inputMap.put("header", "HEADER_DATA");
            // NO colocar processingCode para simular que falta el campo mandatorio 3

            Map<Integer, String> fieldsResponse = Map.of(3, "M");
            when(applicationDataLocalCache.getFieldsResponse("visa", "0110")).thenReturn(fieldsResponse);

            try (MockedStatic<VisaISOField> mockedVisaField = mockStatic(VisaISOField.class)) {
                VisaISOField mockField3 = mock(VisaISOField.class);
                when(mockField3.getName()).thenReturn("processingCode");
                mockedVisaField.when(() -> VisaISOField.getById(3)).thenReturn(mockField3);

                // Act
                Map<String, String> result = visaDelegateFieldLogic.applyLogicFields(inputMap);

                // Assert
                assertThat(result)
                        .isNotNull()
                        .hasSize(2)
                        .containsEntry("header", "HEADER_DATA")
                        .containsEntry("messageType", "0110")
                        .doesNotContainKey("processingCode");
            }
        }

        @Test
        @DisplayName("Debería devolver solo header y messageType si el caché no devuelve campos")
        void shouldReturnOnlyHeaderAndMessageTypeWhenCacheReturnsNoFields() {
            // Arrange
            Map<String, String> inputMap = new HashMap<>();
            inputMap.put("networkName", "visa");
            inputMap.put("messageType", "0110");
            inputMap.put("header", "HEADER_DATA");
            inputMap.put("processingCode", "000000");

            // El caché devuelve una lista vacía de campos a procesar
            when(applicationDataLocalCache.getFieldsResponse("visa", "0110")).thenReturn(Collections.emptyMap());

            // Act
            Map<String, String> result = visaDelegateFieldLogic.applyLogicFields(inputMap);

            // Assert
            assertThat(result)
                    .isNotNull()
                    .hasSize(2)
                    .containsEntry("header", "HEADER_DATA")
                    .containsEntry("messageType", "0110");
        }
    }
}