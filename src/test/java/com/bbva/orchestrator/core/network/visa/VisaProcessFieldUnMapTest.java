package com.bbva.orchestrator.core.network.visa;

import com.bbva.gateway.utils.LogsTraces;
import com.bbva.orchestrator.core.fields.VisaISOField;
import com.bbva.orchestrator.core.parser.iso8583.handlers.impl.VisaHandlerField;
import com.bbva.orchestrator.core.parser.iso8583.strategy.fields.PlainTextFieldParser;
import com.bbva.orchestrator.core.utils.FieldUtil;
import com.bbva.orchestrator.core.utils.ISOUtil;
import com.bbva.orchestrator.core.utils.ParserUtil;
import com.bbva.orchlib.parser.ParserException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VisaProcessFieldUnMapTest {

    @Mock
    private PlainTextFieldParser plainTextFieldParserDecorator;

    @Mock
    private VisaHandlerField visaFieldDefinition;

    @InjectMocks
    private VisaProcessField visaProcessField;

    @Test
    void unMapFields_NullMap_ThrowsParserException() {
        ParserException exception = assertThrows(ParserException.class, () -> {
            visaProcessField.unMapFields(null);
        });
        assertEquals("No se puede generar ISO8583: mapa vacío o nulo", exception.getMessage());
    }

    @Test
    void unMapFields_EmptyMap_ThrowsParserException() {
        Map<String, String> emptyMap = new HashMap<>();
        ParserException exception = assertThrows(ParserException.class, () -> {
            visaProcessField.unMapFields(emptyMap);
        });
        assertEquals("No se puede generar ISO8583: mapa vacío o nulo", exception.getMessage());
    }

    @Test
    void unMapFields_OnlyPrimaryBitmapFields_BuildsCorrectly() {
        // Arrange
        Map<String, String> mapValues = new HashMap<>();
        mapValues.put(VisaISOField.HEADER.getName(), "0B1234567890");
        mapValues.put("messageType", "0200");

        mapValues.put(VisaISOField.PROCESSING_CODE.getName(), "123456");
        mapValues.put(VisaISOField.TRANSACTION_AMOUNT.getName(), "000000001000");

        try (MockedStatic<ISOUtil> mockedIsoUtil = mockStatic(ISOUtil.class)) {
            mockedIsoUtil.when(() -> ISOUtil.convertBITMAPtoHEX(anyString())).thenAnswer(invocation -> {
                String binaryStr = invocation.getArgument(0);

                // ASSERT: Validar la lógica del Bitmap Primario
                assertEquals(64, binaryStr.length(), "Debe ser de 64 bits al no haber campos > 64");
                assertEquals('0', binaryStr.charAt(0), "Bit 1 (Bitmap secundario) debe estar apagado");
                assertEquals('0', binaryStr.charAt(1), "Bit 2 (PAN) debe estar apagado");
                assertEquals('1', binaryStr.charAt(2), "Bit 3 (PROCESSING_CODE) debe estar encendido");
                assertEquals('1', binaryStr.charAt(3), "Bit 4 (TRANSACTION_AMOUNT) debe estar encendido");

                return "MOCKED_HEX_BITMAP"; // Retornamos un hex falso para el resto del test
            });

            // Act
            String result = visaProcessField.unMapFields(mapValues);

            // Assert
            assertNotNull(result);
            assertTrue(result.startsWith("0B12345678900200MOCKED_HEX_BITMAP"));
            assertTrue(result.endsWith("404040")); // END_MESSAGE_VISA
        }
    }

    @Test
    void unMapFields_WithSecondaryBitmapFields_ActivatesBit1() {
        // Arrange
        Map<String, String> mapValues = new HashMap<>();
        mapValues.put(VisaISOField.HEADER.getName(), "HEADER00");
        mapValues.put("messageType", "0800");
        mapValues.put(VisaISOField.PROCESSING_CODE.getName(), "123456"); // Campo 3 (Primario)
        mapValues.put(VisaISOField.SETTLEMENT_CODE.getName(), "99");     // Campo 66 (Secundario)

        try (MockedStatic<ISOUtil> mockedIsoUtil = mockStatic(ISOUtil.class)) {
            mockedIsoUtil.when(() -> ISOUtil.convertBITMAPtoHEX(anyString())).thenAnswer(invocation -> {
                String binaryStr = invocation.getArgument(0);

                // ASSERT: Validar la lógica del Bitmap Secundario
                assertEquals(128, binaryStr.length(), "Debe ser de 128 bits porque hay un campo > 64");
                assertEquals('1', binaryStr.charAt(0), "Bit 1 DEBE estar encendido indicando bitmap secundario");
                assertEquals('1', binaryStr.charAt(2), "Bit 3 (PROCESSING_CODE) debe estar encendido");
                assertEquals('1', binaryStr.charAt(65), "Bit 66 (SETTLEMENT_CODE) debe estar encendido");

                return "MOCKED_128_HEX_BITMAP";
            });

            // Act
            String result = visaProcessField.unMapFields(mapValues);

            // Assert
            assertNotNull(result);
            assertTrue(result.startsWith("HEADER000800MOCKED_128_HEX_BITMAP"));
            assertTrue(result.endsWith("404040"));
        }
    }

    @Test
    void unMapFieldsPlainText_Success() {
        // Arrange
        Map<String, String> mapValues = new HashMap<>();
        mapValues.put("messageType", "0200");
        mapValues.put(VisaISOField.PROCESSING_CODE.getName(), "123456"); // Campo 3

        // SOLUCIÓN: Pasamos los valores directamente sin envolverlos en eq()
        when(plainTextFieldParserDecorator.build("123456", VisaISOField.PROCESSING_CODE, visaFieldDefinition))
                .thenReturn("DECORATED_123456");

        try (MockedStatic<ISOUtil> mockedIsoUtil = mockStatic(ISOUtil.class)) {
            mockedIsoUtil.when(() -> ISOUtil.convertBITMAPtoHEX(anyString())).thenReturn("MOCK_HEX_BM");

            // Act
            String result = visaProcessField.unMapFieldsPlainText(mapValues);

            // Assert
            assertNotNull(result);
            // El formato es messageType + bitmapHex + isoValues
            assertEquals("0200MOCK_HEX_BMDECORATED_123456", result);

            // Aquí sí está bien usar los matchers genéricos any() porque nos interesa
            // solo saber que se llamó 1 vez, sin importar con qué argumentos exactos.
            verify(plainTextFieldParserDecorator, times(1)).build(anyString(), any(), any());
        }
    }

    @Test
    void mapFields_GenericException_ThrowsWrappedParserException() {
        // Arrange
        String hexMessage = "0B00000000000000000000" + "0200" + "4000000000000000";

        try (MockedStatic<FieldUtil> fieldUtilMock = mockStatic(FieldUtil.class);
             MockedStatic<LogsTraces> logsMock = mockStatic(LogsTraces.class)) {

            fieldUtilMock.when(() -> FieldUtil.extractSegment(anyString(), anyBoolean())).thenReturn("Seg");
            fieldUtilMock.when(() -> FieldUtil.processError(anyString(), anyString(), anyBoolean())).thenReturn("ProcessDetails");
            fieldUtilMock.when(() -> FieldUtil.formatMessageException(anyString(), anyString(), any()))
                    .thenReturn("Error Genérico Envuelto [PGWP-00000]");

            // Act & Assert
            ParserException ex = assertThrows(ParserException.class, () -> {
                visaProcessField.mapFields(hexMessage);
            });

            assertEquals("Error Genérico Envuelto [PGWP-00000]", ex.getMessage());
            logsMock.verify(() -> LogsTraces.writeError(contains("messageError")));
        }
    }


    @Test
    void processHeaderComplete_StandardHeader_ReturnsSameHeader() {
        // Arrange
        Map<String, String> valuesMap = new HashMap<>();
        String header = "0B" + "12345678901234567890"; // 22 caracteres exactos
        StringBuilder isoMessage = new StringBuilder(header + "020040000000..."); // Tramo posterior (MTI...)

        // Act
        String result = visaProcessField.processHeaderComplete(isoMessage, valuesMap);

        // Assert
        assertEquals(header, result, "Debe devolver la misma cabecera cortada");
        assertFalse(valuesMap.containsKey("rejectFlag"), "No debe setear rejectFlag en una cabecera estándar");
    }

    @Test
    void processHeaderComplete_DoubleHeader1A_ReordersHeadersAndSetsRejectFlag() {
        // Arrange
        Map<String, String> valuesMap = new HashMap<>();
        String firstHeader = "1A" + "A".repeat(50);
        String secondHeader = "0B" + "B".repeat(20);

        StringBuilder isoMessage = new StringBuilder(firstHeader + secondHeader + "0200RESTODELATRAMA");

        // Act
        String result = visaProcessField.processHeaderComplete(isoMessage, valuesMap);

        assertEquals(secondHeader + firstHeader, result);

        assertEquals("0000", valuesMap.get("rejectFlag"));
    }

    @Test
    void mapFields_WhenFieldIsNull_ThrowsAndLogsParserException() {
        // Arrange
        String hexMessage = "0B00000000000000000000" + "0200" + "0000002000000000";

        try (MockedStatic<ParserUtil> parserUtilMock = mockStatic(ParserUtil.class, Mockito.CALLS_REAL_METHODS);
             MockedStatic<FieldUtil> fieldUtilMock = mockStatic(FieldUtil.class, Mockito.CALLS_REAL_METHODS);
             MockedStatic<LogsTraces> logsMock = mockStatic(LogsTraces.class)) {

            parserUtilMock.when(() -> ParserUtil.createMessageError(27))
                    .thenReturn("Error: Campo 27 no soportado");
            fieldUtilMock.when(() -> FieldUtil.extractSegment(anyString(), anyBoolean()))
                    .thenReturn("SegmentoErrorMock");

            ParserException exception = assertThrows(ParserException.class, () -> {
                visaProcessField.mapFields(hexMessage);
            });

            assertEquals("Error Code: [PGWP-00000], Description: No se puede parsear el mensaje ISO - 0B00000000000000000000020000F0F0F0F0F0F0F0F0F0F0F0F0, Cause: No cause provided", exception.getMessage());
        }
    }

    @Test
    void mapFields_WhenGenericExceptionOccurs_WrapsAndLogsAsParserException() {

        String hexMessage = "0B00000000000000000000" + "0200" + "4000000000000000";

        try (MockedStatic<FieldUtil> fieldUtilMock = mockStatic(FieldUtil.class);
             MockedStatic<LogsTraces> logsMock = mockStatic(LogsTraces.class)) {

            fieldUtilMock.when(() -> FieldUtil.extractSegment(anyString(), anyBoolean()))
                    .thenReturn("SegmentoErrorMock");
            fieldUtilMock.when(() -> FieldUtil.processError(anyString(), anyString(), anyBoolean()))
                    .thenReturn("DetalleErrorProcesado");

            String expectedWrappedMessage = "Error Genérico [PGWP-00000] No se puede parsear...";
            fieldUtilMock.when(() -> FieldUtil.formatMessageException(eq("[PGWP-00000]"), anyString(), any()))
                    .thenReturn(expectedWrappedMessage);

            ParserException exception = assertThrows(ParserException.class, () -> {
                visaProcessField.mapFields(hexMessage);
            });

            assertEquals(expectedWrappedMessage, exception.getMessage());

            logsMock.verify(() -> LogsTraces.writeError(contains("messageError: SegmentoErrorMock")));
        }
    }
}