package com.bbva.orchestrator.core.parser.iso8583.strategy.fields;

import com.bbva.orchestrator.core.exception.ParserFieldsException;
import com.bbva.orchestrator.core.fields.definitions.IFieldDefinition;
import com.bbva.orchestrator.core.parser.iso8583.ParsedFieldResult;
import com.bbva.orchestrator.core.parser.iso8583.handlers.NetworkHandlerField;
import com.bbva.orchestrator.core.parser.iso8583.strategy.FieldParserStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LlvarLengthPrefixParserVisaTest {

    @Mock
    private FieldParserStrategy decoratedParser; // El parser base (decorado)

    @Mock
    private IFieldDefinition fieldDefinition;

    @Mock
    private NetworkHandlerField networkHandlerField;

    private LlvarLengthPrefixParserVisa visaParser; // La clase bajo prueba (SUT)

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Inyectamos el mock en el constructor del Decorator
        visaParser = new LlvarLengthPrefixParserVisa(decoratedParser);
    }

    /**
     * Prueba que el método build() simplemente delega la llamada
     * al parser decorado.
     */
    @Test
    void build_shouldDelegateToDecoratedParser() {
        // Arrange
        String fieldValue = "12345";
        String expectedResult = "000512345"; // Resultado simulado por el decorado
        when(decoratedParser.build(fieldValue, fieldDefinition, networkHandlerField))
                .thenReturn(expectedResult);

        // Act
        String actualResult = visaParser.build(fieldValue, fieldDefinition, networkHandlerField);

        // Assert
        assertNotNull(actualResult);
        assertEquals(expectedResult, actualResult);
        // Verifica que el método build del decorado fue llamado exactamente 1 vez
        verify(decoratedParser, times(1)).build(fieldValue, fieldDefinition, networkHandlerField);
    }

    /**
     * Prueba el caso donde la longitud en el prefijo (hex) coincide
     * con la longitud del valor parseado.
     * El valor NO debe ser modificado.
     */
    @Test
    void parse_whenPrefixLengthMatchesValueLength_shouldReturnValueAsIs() {
        // Arrange
        String rawDataSegment = "0A0123456789"; // Prefijo 0A (hex) = 10
        String parsedValue = "0123456789"; // Longitud 10
        int consumedLength = 12; // 2 (prefijo) + 10 (datos)
        ParsedFieldResult baseResult = new ParsedFieldResult(parsedValue, consumedLength);

        // Simulamos que el parser base devuelve el valor con el '0'
        when(decoratedParser.parse(rawDataSegment, fieldDefinition, networkHandlerField))
                .thenReturn(baseResult);

        // Act
        ParsedFieldResult finalResult = visaParser.parse(rawDataSegment, fieldDefinition, networkHandlerField);

        // Assert
        assertNotNull(finalResult);
        // La longitud del prefijo (10) COINCIDE con la longitud del valor (10)
        assertEquals(parsedValue, finalResult.value(), "El valor no debe cambiar si las longitudes coinciden");
        assertEquals(consumedLength, finalResult.consumedLengthInChars());
        verify(decoratedParser, times(1)).parse(rawDataSegment, fieldDefinition, networkHandlerField);
    }

    /**
     * Prueba el caso clave de VISA: la longitud en el prefijo (hex) es MENOR
     * que la longitud del valor parseado.
     * Se debe eliminar el '0' inicial.
     */
    @Test
    void parse_whenPrefixLengthIsLessValueLength_shouldRemoveLeadingZero() {
        // Arrange
        // Prefijo 0B (hex) = 11
        String rawDataSegment = "0B012345678901";
        // Valor parseado por el base (ej. porque leyó 6 bytes = 12 chars)
        String parsedValue = "012345678901"; // Longitud 12
        String expectedValue = "12345678901"; // Valor corregido
        int consumedLength = 14; // 2 (prefijo) + 12 (datos)
        ParsedFieldResult baseResult = new ParsedFieldResult(parsedValue, consumedLength);

        when(decoratedParser.parse(rawDataSegment, fieldDefinition, networkHandlerField))
                .thenReturn(baseResult);

        // Act
        ParsedFieldResult finalResult = visaParser.parse(rawDataSegment, fieldDefinition, networkHandlerField);

        // Assert
        assertNotNull(finalResult);
        // La longitud del prefijo (11) NO COINCIDE con la longitud del valor (12)
        assertEquals(expectedValue, finalResult.value(), "Debe eliminar el '0' inicial");
        assertEquals(consumedLength, finalResult.consumedLengthInChars(), "El 'consumedLength' debe ser el del resultado base");
    }

    /**
     * Prueba que si el parser decorado lanza una excepción,
     * esta se propaga correctamente.
     */
    @Test
    void parse_whenDecoratedParserThrowsException_shouldPropagateException() {
        // Arrange
        String rawDataSegment = "0A1234567890";
        ParserFieldsException simulatedException = new ParserFieldsException("PGWP-00106", "Error en parser base",new Throwable() );

        when(decoratedParser.parse(rawDataSegment, fieldDefinition, networkHandlerField))
                .thenThrow(simulatedException);

        // Act & Assert
        ParserFieldsException thrown = assertThrows(ParserFieldsException.class, () -> {
            visaParser.parse(rawDataSegment, fieldDefinition, networkHandlerField);
        }, "Se esperaba que la excepción del decorado se propagara");

        assertEquals("PGWP-00106", thrown.getCode());
    }

    /**
     * Prueba un caso límite donde el prefijo hex no es válido.
     * La clase debe lanzar NumberFormatException.
     */
    @Test
    void parse_withInvalidHexPrefix_shouldThrowNumberFormatException() {
        // Arrange
        String rawDataSegment = "XXINVALIDHEX"; // "XX" no es un hex válido
        ParsedFieldResult baseResult = new ParsedFieldResult("123", 5);
        when(decoratedParser.parse(rawDataSegment, fieldDefinition, networkHandlerField)).thenReturn(baseResult);

        // Act & Assert
        // La excepción ocurre en validateZeroLeftParse, DESPUÉS de la llamada al decorado
        assertThrows(NumberFormatException.class, () -> {
            visaParser.parse(rawDataSegment, fieldDefinition, networkHandlerField);
        }, "Debería lanzar NumberFormatException al parsear 'XX' como hex");
    }

    /**
     * Prueba un caso límite donde el rawDataSegment es demasiado corto
     * para extraer el prefijo.
     */
    @Test
    void parse_withRawDataTooShort_shouldThrowStringIndexOutOfBoundsException() {
        // Arrange
        String rawDataSegment = "1"; // Menor a 2 chars
        ParsedFieldResult baseResult = new ParsedFieldResult("1", 1);
        when(decoratedParser.parse(rawDataSegment, fieldDefinition, networkHandlerField)).thenReturn(baseResult);

        // Act & Assert
        // La excepción ocurre en validateZeroLeftParse (rawDataSegment.substring(0, 2))
        assertThrows(StringIndexOutOfBoundsException.class, () -> {
            visaParser.parse(rawDataSegment, fieldDefinition, networkHandlerField);
        }, "Debería lanzar StringIndexOutOfBoundsException por substring(0, 2)");
    }
}