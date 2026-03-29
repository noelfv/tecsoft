package com.bbva.orchestrator.network.mastercard;

import com.bbva.gateway.utils.LogsTraces;
import com.bbva.orchestrator.core.commons.CommonsProcessSubField;
import com.bbva.orchestrator.core.dto.ISO8583;
import com.bbva.orchestrator.core.exception.ParserFieldsException;
import com.bbva.orchestrator.core.fields.MastercardISOField;
import com.bbva.orchestrator.core.logic.process.MastercardProcessSubField;
import com.bbva.orchestrator.core.parser.iso8583.handlers.impl.MastercardHandlerField;
import com.bbva.orchestrator.core.parser.iso8583.strategy.subfields.CompositeTlvFieldParser;
import com.bbva.orchestrator.core.fields.definitions.subfields.tlv.TLVFieldLoadStructure;
import com.bbva.orchestrator.core.utils.FieldUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MastercardISOSubFieldParserTest {

    // --- Mocks para las dependencias de la clase ---
    @Mock
    private MastercardHandlerField mastercardHandlerField;

    @Mock
    private CommonsProcessSubField defaultISOSubFieldParser;

    // --- Objeto bajo prueba ---
    @InjectMocks
    private MastercardProcessSubField parser;

    // --- Mocks para objetos de datos ---
    @Mock
    private ISO8583 iso8583;

    // --- Mocks para clases con métodos estáticos ---
    private MockedStatic<TLVFieldLoadStructure> subFieldDefinitionsMock;
    private MockedStatic<FieldUtil> fieldUtilsMock;

    private MockedStatic<LogsTraces> logsTracesMock;

    @BeforeEach
    void setUp() {
        // 1. Mockea PRIMERO la clase que causa el problema en el bloque estático.
        logsTracesMock = mockStatic(LogsTraces.class);

        // 2. Ahora, mockea la clase que la usa.
        //    Cuando se inicialice, llamará al mock de LogsTraces en lugar del real.
        subFieldDefinitionsMock = mockStatic(TLVFieldLoadStructure.class);

        // 3. Mockea el resto de tus utilidades estáticas.
        fieldUtilsMock = mockStatic(FieldUtil.class);
    }

    @AfterEach
    void tearDown() {
        // Cierra todos los mocks en el orden que prefieras, aunque el inverso es común.
        fieldUtilsMock.close();
        subFieldDefinitionsMock.close();
        logsTracesMock.close();
    }

    @Test
    @DisplayName("Debería retornar un mapa vacío si el tipo de mensaje no requiere procesamiento")
    void shouldReturnEmptyMap_whenMessageTypeDoesNotRequireProcessing() {
        // Arrange
        when(iso8583.getMessageType()).thenReturn("0800");
        // Mockeamos la llamada estática para que devuelva false
        when(FieldUtil.requiredProcess("0800")).thenReturn(false);

        // Act
        Map<String, String> result = parser.parseSubfields(iso8583);

        // Assert
        assertTrue(result.isEmpty(), "El mapa de resultados debe estar vacío para el tipo de mensaje 0800.");
        // Verificamos que no hubo interacción con ninguna de las dependencias principales
        verifyNoInteractions(defaultISOSubFieldParser);
        verifyNoInteractions(mastercardHandlerField);
    }

    @Test
    @DisplayName("Debería retornar solo los campos por defecto si no hay Campo 48")
    void shouldReturnOnlyDefaultFields_whenField48IsAbsent() {
        // Arrange
        Map<String, String> defaultFields = Map.of("DE2", "123456", "DE3", "000000");
        when(iso8583.getMessageType()).thenReturn("0100");
        when(iso8583.getAdditionalDataRetailer()).thenReturn(null); // No hay Campo 48
        when(FieldUtil.requiredProcess("0100")).thenReturn(true);
        when(defaultISOSubFieldParser.parseSubfields(iso8583)).thenReturn(new HashMap<>(defaultFields));

        // Act
        Map<String, String> result = parser.parseSubfields(iso8583);

        // Assert
        assertThat(result).isEqualTo(defaultFields);
        verify(defaultISOSubFieldParser).parseSubfields(iso8583);
        verifyNoInteractions(mastercardHandlerField);
    }

    @Test
    @DisplayName("Debería combinar los campos por defecto y los del Campo 48 cuando ambos están presentes")
    void shouldMergeDefaultAndField48Subfields_whenBothArePresent() {
        // Arrange
        String field48HexData = "DUMMY_HEX_DATA";
        Map<String, String> defaultFields = Map.of("DE2", "123456");
        Map<String, String> field48Subfields = Map.of("48.01", "Subfield1Value");
        Map<String, String> expectedResult = Map.of("DE2", "123456", "48.01", "Subfield1Value");

        when(iso8583.getMessageType()).thenReturn("0100");
        when(iso8583.getAdditionalDataRetailer()).thenReturn(field48HexData);
        when(FieldUtil.requiredProcess("0100")).thenReturn(true);
        when(defaultISOSubFieldParser.parseSubfields(iso8583)).thenReturn(new HashMap<>(defaultFields));

        // Mockeamos la construcción de CompositeTlvFieldParser
        try (MockedConstruction<CompositeTlvFieldParser> mockedParser = mockConstruction(CompositeTlvFieldParser.class,
                (mock, context) -> {
                    // Cuando se llame a parseToMap en el objeto recién creado, devolvemos los subcampos del 48
                    when(mock.parseToMap(field48HexData, MastercardISOField.ADDITIONAL_DATA_48, mastercardHandlerField))
                            .thenReturn(field48Subfields);
                })) {
            // Act
            Map<String, String> result = parser.parseSubfields(iso8583);

            // Assert
            assertThat(result).containsAllEntriesOf(expectedResult);
            assertEquals(1, mockedParser.constructed().size(), "Se debe crear una instancia de CompositeTlvFieldParser.");
        }
    }

    @Test
    @DisplayName("Debería lanzar ParserFieldsException si el parseo del Campo 48 falla")
    void shouldThrowParserFieldsException_whenField48ParsingFails() {
        // Arrange
        String field48HexData = "INVALID_HEX_DATA";
        when(iso8583.getMessageType()).thenReturn("0100");
        when(iso8583.getAdditionalDataRetailer()).thenReturn(field48HexData);
        when(FieldUtil.requiredProcess("0100")).thenReturn(true);
        when(defaultISOSubFieldParser.parseSubfields(iso8583)).thenReturn(new HashMap<>());

        // Mockeamos la construcción para que lance una excepción
        try (MockedConstruction<CompositeTlvFieldParser> mockedParser = mockConstruction(CompositeTlvFieldParser.class,
                (mock, context) -> {
                    when(mock.parseToMap(any(), any(), any())).thenThrow(new RuntimeException("Error de parseo interno"));
                })) {

            // Act & Assert
            ParserFieldsException exception = assertThrows(ParserFieldsException.class, () -> {
                parser.parseSubfields(iso8583);
            });

            // --- ASSERT CORREGIDO ---
            // En lugar de verificar el mensaje principal (que es incorrecto),
            // verificamos la excepción original que fue encapsulada (la causa).
            assertThat(exception.getCause()).isNotNull();
            assertThat(exception.getCause()).isInstanceOf(RuntimeException.class);
            assertThat(exception.getCause().getMessage()).isEqualTo("Error de parseo interno");
        }
    }
}