package com.bbva.orchestrator.core.commons;

import com.bbva.orchestrator.core.dto.ISO8583;
import com.bbva.orchestrator.core.parser.iso8583.strategy.subfields.CompositeFixedFieldParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the CommonsProcessSubField class.
 */
@ExtendWith(MockitoExtension.class)
class CommonsProcessSubFieldTest {

    @Mock
    private CompositeFixedFieldParser compositeFieldParser;

    @InjectMocks
    private CommonsProcessSubField commonsProcessSubField;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Debe parsear y combinar todos los subcampos cuando todos los datos están presentes")
    void parseSubfields_shouldReturnAllParsedSubfields_whenAllFieldsArePresent() {
        // Arrange: Configurar los datos de entrada y los mocks
        ISO8583 iso8583= ISO8583.builder()
                .processingCode("010203")
                .pointServiceEntryMode("0510")
                .additionalAmounts("100USD")
                .posCardIssuer("VISA")
                .build();

        // Mapas simulados que devolverá el parser
        Map<String, String> map03 = Map.of("03.1", "01", "03.2", "02");
        Map<String, String> map22 = Map.of("22.1", "051", "22.2", "0");
        Map<String, String> map54 = Map.of("54.amount", "100", "54.currency", "USD");
        Map<String, String> map61 = Map.of("61.issuer", "VISA");

        // Configurar el comportamiento del mock
        when(compositeFieldParser.buildSubFieldsSpecific("03", iso8583.getProcessingCode())).thenReturn(map03);
        when(compositeFieldParser.buildSubFieldsSpecific("22", iso8583.getPointServiceEntryMode())).thenReturn(map22);
        when(compositeFieldParser.buildSubFieldsSpecific("54", iso8583.getAdditionalAmounts())).thenReturn(map54);
        when(compositeFieldParser.buildSubFieldsSpecific("61", iso8583.getPosCardIssuer())).thenReturn(map61);

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.putAll(map03);
        expectedMap.putAll(map22);
        expectedMap.putAll(map54);
        expectedMap.putAll(map61);

        // Act: Ejecutar el método a probar
        Map<String, String> result = commonsProcessSubField.parseSubfields(iso8583);

        // Assert: Verificar los resultados
        assertNotNull(result);
        assertEquals(expectedMap.size(), result.size());
        assertEquals(expectedMap, result, "El mapa resultante debe contener todos los subcampos combinados.");

        // Verificar que el mock fue llamado con los parámetros correctos
        verify(compositeFieldParser).buildSubFieldsSpecific("03", "010203");
        verify(compositeFieldParser).buildSubFieldsSpecific("22", "0510");
        verify(compositeFieldParser).buildSubFieldsSpecific("54", "100USD");
        verify(compositeFieldParser).buildSubFieldsSpecific("61", "VISA");
        verifyNoMoreInteractions(compositeFieldParser);
    }

    @Test
    @DisplayName("Debe devolver un mapa vacío si todos los campos del ISO son nulos")
    void parseSubfields_shouldReturnEmptyMap_whenAllFieldsAreNull() {
        // Arrange: Configurar los datos de entrada como nulos
        ISO8583 iso8583= ISO8583.builder()
                .processingCode(null)
                .pointServiceEntryMode(null)
                .additionalAmounts(null)
                .posCardIssuer(null)
                .build();

        // Si se le pasa null, el parser debe devolver un mapa vacío
        when(compositeFieldParser.buildSubFieldsSpecific(anyString(), eq(null)))
                .thenReturn(Collections.emptyMap());

        // Act: Ejecutar el método a probar
        Map<String, String> result = commonsProcessSubField.parseSubfields(iso8583);

        // Assert: Verificar el resultado
        assertNotNull(result);
        assertTrue(result.isEmpty(), "El mapa resultante debe estar vacío si no hay datos de entrada.");

        // Verificar que el parser fue llamado 4 veces con null
        verify(compositeFieldParser, times(4)).buildSubFieldsSpecific(anyString(), eq(null));
        verifyNoMoreInteractions(compositeFieldParser);
    }
}