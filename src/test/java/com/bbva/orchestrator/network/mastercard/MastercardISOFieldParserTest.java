package com.bbva.orchestrator.network.mastercard;

import com.bbva.gateway.utils.LogsTraces;
import com.bbva.orchestrator.core.fields.MastercardISOField;
import com.bbva.orchestrator.core.parser.iso8583.handlers.impl.MastercardHandlerField;
import com.bbva.orchestrator.core.parser.iso8583.strategy.FieldParserStrategy;
import com.bbva.orchestrator.core.parser.iso8583.strategy.fields.PlainTextFieldParser;
import com.bbva.orchestrator.core.network.mastercard.MastercardProcessField;
import com.bbva.orchestrator.core.utils.ISOUtil;
import com.bbva.orchestrator.core.utils.ParserUtil;
import com.bbva.orchlib.parser.ParserException;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MastercardISOFieldParserTest {

    @Mock
    private PlainTextFieldParser plainTextFieldParserDecorator;

    @Mock
    private MastercardHandlerField mastercardHandlerField;

    @Mock
    private FieldParserStrategy parserStrategy;
    private static final String DUMMY_HEX_MESSAGE = "020070380000000000040000000000000000...";


    @InjectMocks
    private MastercardProcessField mastercardISOFieldParser;

    @BeforeEach
    void setUp() {
        reset(plainTextFieldParserDecorator, mastercardHandlerField, parserStrategy);
    }

    @Test
    void mapFields_ShouldThrowParserException_WhenProcessFieldDataThrowsParserLocalException() {
        try (MockedStatic<MastercardISOField> mockedISOField = mockStatic(MastercardISOField.class, Mockito.CALLS_REAL_METHODS)) {
            String hexMessage = "0200C000000000000001001A02";
            MastercardISOField field2 = mock(MastercardISOField.class);
            mockedISOField.when(() -> MastercardISOField.getById(2)).thenReturn(field2);
            ParserException exception = assertThrows(ParserException.class, () -> mastercardISOFieldParser.mapFields(hexMessage));
            assertFalse(exception.getMessage().contains("PGWP-00001"));
        }
    }

    @Test
    void mapFields_ShouldThrowParserException_WhenGenericExceptionOccurs() {
        try (MockedStatic<MastercardISOField> mockedISOField = mockStatic(MastercardISOField.class, Mockito.CALLS_REAL_METHODS)) {
            String hexMessage = "0200C000000000000001001A02";
            ParserException exception = assertThrows(ParserException.class, () -> mastercardISOFieldParser.mapFields(hexMessage));
            assertTrue(exception.getMessage().contains("No se puede parsear el mensaje ISO"));
        }
    }

    // --- Tests para el método unMapFields ---

    @Test
    void unMapFields_ShouldBuildCorrectHexMessage_WithPrimaryBitmapOnly() {
        try (MockedStatic<MastercardISOField> mockedISOField = mockStatic(MastercardISOField.class, Mockito.CALLS_REAL_METHODS)) {
            Map<String, String> valuesMap = new HashMap<>();
            valuesMap.put("messageType", "0200");
            valuesMap.put("field2", "02");
            valuesMap.put("field3", "03");

            MastercardISOField field2 = mock(MastercardISOField.class);
            MastercardISOField field3 = mock(MastercardISOField.class);
            mockedISOField.when(() -> MastercardISOField.getById(2)).thenReturn(field2);
            mockedISOField.when(() -> MastercardISOField.getById(3)).thenReturn(field3);
            when(field2.getName()).thenReturn("field2");
            when(field3.getName()).thenReturn("field3");
            when(field2.getParserStrategy()).thenReturn(parserStrategy);
            when(field3.getParserStrategy()).thenReturn(parserStrategy);

            when(parserStrategy.build(eq("02"), eq(field2), any(MastercardHandlerField.class))).thenReturn("02");
            when(parserStrategy.build(eq("03"), eq(field3), any(MastercardHandlerField.class))).thenReturn("03");

            StringBuilder binaryBitmap = new StringBuilder("0");
            for (int i = 2; i <= 64; i++) {
                if (i == 2 || i == 3) {
                    binaryBitmap.append('1');
                } else {
                    binaryBitmap.append('0');
                }
            }

            String expectedHex = ISOUtil.stringToEBCDICHex("0200") +
                    ISOUtil.convertBITMAPtoHEX(binaryBitmap.toString()) +
                    "02" + "03";

            String result = mastercardISOFieldParser.unMapFields(valuesMap);
            assertEquals(expectedHex, result);
        }
    }

    @Test
    void unMapFields_ShouldBuildCorrectHexMessage_WithSecondaryBitmap() {
        try (MockedStatic<MastercardISOField> mockedISOField = mockStatic(MastercardISOField.class, Mockito.CALLS_REAL_METHODS)) {
            Map<String, String> valuesMap = new HashMap<>();
            valuesMap.put("messageType", "0200");
            valuesMap.put("field2", "02");
            valuesMap.put("field65", "65");

            MastercardISOField field2 = mock(MastercardISOField.class);
            MastercardISOField field65 = mock(MastercardISOField.class);
            mockedISOField.when(() -> MastercardISOField.getById(2)).thenReturn(field2);
            mockedISOField.when(() -> MastercardISOField.getById(65)).thenReturn(field65);
            when(field2.getName()).thenReturn("field2");
            when(field65.getName()).thenReturn("field65");
            when(field2.getParserStrategy()).thenReturn(parserStrategy);
            when(field65.getParserStrategy()).thenReturn(parserStrategy);

            when(parserStrategy.build(eq("02"), eq(field2), any(MastercardHandlerField.class))).thenReturn("02");
            when(parserStrategy.build(eq("65"), eq(field65), any(MastercardHandlerField.class))).thenReturn("65");

            StringBuilder binaryBitmap = new StringBuilder("1");
            for (int i = 2; i <= 128; i++) {
                if (i == 2 || i == 65) {
                    binaryBitmap.append('1');
                } else {
                    binaryBitmap.append('0');
                }
            }

            String expectedHex = ISOUtil.stringToEBCDICHex("0200") +
                    ISOUtil.convertBITMAPtoHEX(binaryBitmap.toString()) +
                    "0265";

            String result = mastercardISOFieldParser.unMapFields(valuesMap);
            assertEquals(expectedHex, result);
        }
    }

    @Test
    void unMapFields_ShouldThrowException_WhenMapIsEmpty() {
        Map<String, String> emptyMap = new HashMap<>();
        ParserException exception = assertThrows(ParserException.class, () -> mastercardISOFieldParser.unMapFields(emptyMap));
        assertEquals("No se puede generar ISO8583: mapa vacío o nulo", exception.getMessage());
    }

    @Test
    void unMapFields_ShouldHandleNullAndEmptyValuesInMap() {
        try (MockedStatic<MastercardISOField> mockedISOField = mockStatic(MastercardISOField.class, Mockito.CALLS_REAL_METHODS)) {
            Map<String, String> valuesMap = new HashMap<>();
            valuesMap.put("messageType", "0200");
            valuesMap.put("field2", "02");
            valuesMap.put("field3", ""); // Campo con valor vacío
            valuesMap.put("field4", null); // Campo con valor nulo

            MastercardISOField field2 = mock(MastercardISOField.class);
            MastercardISOField field3 = mock(MastercardISOField.class);
            MastercardISOField field4 = mock(MastercardISOField.class);
            mockedISOField.when(() -> MastercardISOField.getById(2)).thenReturn(field2);
            mockedISOField.when(() -> MastercardISOField.getById(3)).thenReturn(field3);
            mockedISOField.when(() -> MastercardISOField.getById(4)).thenReturn(field4);
            when(field2.getName()).thenReturn("field2");
            when(field3.getName()).thenReturn("field3");
            when(field4.getName()).thenReturn("field4");
            when(field2.getParserStrategy()).thenReturn(parserStrategy);

            when(parserStrategy.build(eq("02"), eq(field2), any(MastercardHandlerField.class))).thenReturn("02");

            StringBuilder binaryBitmap = new StringBuilder("0");
            for (int i = 2; i <= 64; i++) {
                if (i == 2) {
                    binaryBitmap.append('1');
                } else {
                    binaryBitmap.append('0');
                }
            }

            String expectedHex = ISOUtil.stringToEBCDICHex("0200") +
                    ISOUtil.convertBITMAPtoHEX(binaryBitmap.toString()) +
                    "02";

            String result = mastercardISOFieldParser.unMapFields(valuesMap);
            assertEquals(expectedHex, result);
        }
    }

    // --- Tests para el método unMapFieldsPlainText ---

    @Test
    void unMapFieldsPlainText_ShouldBuildCorrectPlainTextMessage_WithPrimaryBitmapOnly() {
        try (MockedStatic<MastercardISOField> mockedISOField = mockStatic(MastercardISOField.class, Mockito.CALLS_REAL_METHODS)) {
            Map<String, String> valuesMap = new HashMap<>();
            valuesMap.put("messageType", "0200");
            valuesMap.put("field2", "02");
            valuesMap.put("field3", "03");

            MastercardISOField field2 = mock(MastercardISOField.class);
            MastercardISOField field3 = mock(MastercardISOField.class);
            mockedISOField.when(() -> MastercardISOField.getById(2)).thenReturn(field2);
            mockedISOField.when(() -> MastercardISOField.getById(3)).thenReturn(field3);
            when(field2.getName()).thenReturn("field2");
            when(field3.getName()).thenReturn("field3");

            when(plainTextFieldParserDecorator.build(eq("02"), eq(field2), any(MastercardHandlerField.class))).thenReturn("02");
            when(plainTextFieldParserDecorator.build(eq("03"), eq(field3), any(MastercardHandlerField.class))).thenReturn("03");

            StringBuilder binaryBitmap = new StringBuilder("0");
            for (int i = 2; i <= 64; i++) {
                if (i == 2 || i == 3) {
                    binaryBitmap.append('1');
                } else {
                    binaryBitmap.append('0');
                }
            }

            String expectedPlain = "0200" +
                    ISOUtil.convertBITMAPtoHEX(binaryBitmap.toString()) +
                    "0203";

            String result = mastercardISOFieldParser.unMapFieldsPlainText(valuesMap);
            assertEquals(expectedPlain, result);
        }
    }

    @Test
    void unMapFieldsPlainText_ShouldBuildCorrectPlainTextMessage_WithSecondaryBitmap() {
        try (MockedStatic<MastercardISOField> mockedISOField = mockStatic(MastercardISOField.class, Mockito.CALLS_REAL_METHODS)) {
            Map<String, String> valuesMap = new HashMap<>();
            valuesMap.put("messageType", "0200");
            valuesMap.put("field2", "02");
            valuesMap.put("field65", "65");

            MastercardISOField field2 = mock(MastercardISOField.class);
            MastercardISOField field65 = mock(MastercardISOField.class);
            mockedISOField.when(() -> MastercardISOField.getById(2)).thenReturn(field2);
            mockedISOField.when(() -> MastercardISOField.getById(65)).thenReturn(field65);
            when(field2.getName()).thenReturn("field2");
            when(field65.getName()).thenReturn("field65");

            when(plainTextFieldParserDecorator.build(eq("02"), eq(field2), any(MastercardHandlerField.class))).thenReturn("02");
            when(plainTextFieldParserDecorator.build(eq("65"), eq(field65), any(MastercardHandlerField.class))).thenReturn("65");

            StringBuilder binaryBitmap = new StringBuilder("1");
            for (int i = 2; i <= 128; i++) {
                if (i == 2 || i == 65) {
                    binaryBitmap.append('1');
                } else {
                    binaryBitmap.append('0');
                }
            }

            String expectedPlain = "0200" +
                    ISOUtil.convertBITMAPtoHEX(binaryBitmap.toString()) +
                    "0265";

            String result = mastercardISOFieldParser.unMapFieldsPlainText(valuesMap);
            assertEquals(expectedPlain, result);
        }
    }

    @Test
    void unMapFieldsPlainText_ShouldHandleNullAndEmptyValuesInMap() {
        try (MockedStatic<MastercardISOField> mockedISOField = mockStatic(MastercardISOField.class, Mockito.CALLS_REAL_METHODS)) {
            Map<String, String> valuesMap = new HashMap<>();
            valuesMap.put("messageType", "0200");
            valuesMap.put("field2", "02");
            valuesMap.put("field3", ""); // Campo con valor vacío
            valuesMap.put("field4", null); // Campo con valor nulo

            MastercardISOField field2 = mock(MastercardISOField.class);
            MastercardISOField field3 = mock(MastercardISOField.class);
            MastercardISOField field4 = mock(MastercardISOField.class);
            mockedISOField.when(() -> MastercardISOField.getById(2)).thenReturn(field2);
            mockedISOField.when(() -> MastercardISOField.getById(3)).thenReturn(field3);
            mockedISOField.when(() -> MastercardISOField.getById(4)).thenReturn(field4);
            when(field2.getName()).thenReturn("field2");
            when(field3.getName()).thenReturn("field3");
            when(field4.getName()).thenReturn("field4");

            when(plainTextFieldParserDecorator.build(eq("02"), eq(field2), any(MastercardHandlerField.class))).thenReturn("02");

            StringBuilder binaryBitmap = new StringBuilder("0");
            for (int i = 2; i <= 64; i++) {
                if (i == 2) {
                    binaryBitmap.append('1');
                } else {
                    binaryBitmap.append('0');
                }
            }

            String expectedPlain = "0200" +
                    ISOUtil.convertBITMAPtoHEX(binaryBitmap.toString()) +
                    "02";

            String result = mastercardISOFieldParser.unMapFieldsPlainText(valuesMap);
            assertEquals(expectedPlain, result);
        }
    }

    /**
     * Prueba el "camino feliz" para un mensaje que solo tiene bitmap primario.
     * Este test cubre el bucle principal de procesamiento de campos.
     */
    @Test
    void mapFields_HappyPath_PrimaryBitmapOnly() {
        // --- ARRANGE ---
        // Usamos try-with-resources para manejar los mocks estáticos
        try (MockedStatic<ParserUtil> parserUtilMock = mockStatic(ParserUtil.class);
             MockedStatic<MastercardISOField> isoFieldMock = mockStatic(MastercardISOField.class, Mockito.CALLS_REAL_METHODS)) {

            // Simulamos el bitmap: campos 2 y 3 están activos. El bit 1 es '0'.
            String primaryBitmapBinary = "011" + "0".repeat(61);

            // Simulamos lo que haría ParserUtil.processFieldData
            // 1. Procesa MESSAGE_TYPE
            parserUtilMock.when(() -> ParserUtil.processFieldData(eq(MastercardISOField.MESSAGE_TYPE), any(), eq(0), anyMap(), any()))
                    .thenAnswer(invocation -> {
                        Map<String, String> map = invocation.getArgument(3);
                        map.put(MastercardISOField.MESSAGE_TYPE.getName(), "0200");
                        return 4; // Avanza 4 posiciones
                    });

            // 2. Procesa BITMAP_PRIMARY
            parserUtilMock.when(() -> ParserUtil.processFieldData(eq(MastercardISOField.BITMAP_PRIMARY), any(), eq(4), anyMap(), any()))
                    .thenAnswer(invocation -> {
                        Map<String, String> map = invocation.getArgument(3);
                        map.put(MastercardISOField.BITMAP_PRIMARY.getName(), primaryBitmapBinary);
                        return 20; // Avanza 16 posiciones (8 bytes en hex)
                    });

            // 3. Mocks para los campos 2 y 3 que están activos en el bitmap
            MastercardISOField field2 = mock(MastercardISOField.class);
            MastercardISOField field3 = mock(MastercardISOField.class);
            isoFieldMock.when(() -> MastercardISOField.getById(2)).thenReturn(field2);
            isoFieldMock.when(() -> MastercardISOField.getById(3)).thenReturn(field3);

            // 4. Procesa los campos del bucle
            parserUtilMock.when(() -> ParserUtil.processFieldData(eq(field2), any(), eq(20), anyMap(), any())).thenReturn(30);
            parserUtilMock.when(() -> ParserUtil.processFieldData(eq(field3), any(), eq(30), anyMap(), any())).thenReturn(40);

            // --- ACT ---
            Map<String, String> result = mastercardISOFieldParser.mapFields(DUMMY_HEX_MESSAGE);

            // --- ASSERT ---
            assertNotNull(result);
            assertEquals("0200", result.get(MastercardISOField.MESSAGE_TYPE.getName()));
            assertEquals(primaryBitmapBinary, result.get(MastercardISOField.BITMAP_PRIMARY.getName()));

            // Verificamos que se intentó procesar los campos 2 y 3
            parserUtilMock.verify(() -> ParserUtil.processFieldData(eq(field2), any(), anyInt(), anyMap(), any()));
            parserUtilMock.verify(() -> ParserUtil.processFieldData(eq(field3), any(), anyInt(), anyMap(), any()));
        }
    }

    /**
     * Prueba el "camino feliz" para un mensaje con bitmap primario y secundario.
     * Este test cubre el bloque if (binaryBitMapPrimary.charAt(0) == '1').
     */
    @Test
    void mapFields_HappyPath_WithSecondaryBitmap() {
        try (MockedStatic<ParserUtil> parserUtilMock = mockStatic(ParserUtil.class);
             MockedStatic<MastercardISOField> isoFieldMock = mockStatic(MastercardISOField.class, Mockito.CALLS_REAL_METHODS)) {

            // Simulamos el bitmap: Bit 1 es '1', campo 65 está activo.
            String primaryBitmapBinary = "1" + "0".repeat(63);
            String secondaryBitmapBinary = "1" + "0".repeat(63);

            // Simulamos el comportamiento de ParserUtil
            parserUtilMock.when(() -> ParserUtil.processFieldData(eq(MastercardISOField.MESSAGE_TYPE), any(), anyInt(), anyMap(), any())).thenReturn(4);
            parserUtilMock.when(() -> ParserUtil.processFieldData(eq(MastercardISOField.BITMAP_PRIMARY), any(), anyInt(), anyMap(), any()))
                    .thenAnswer(invocation -> {
                        invocation.<Map<String, String>>getArgument(3).put(MastercardISOField.BITMAP_PRIMARY.getName(), primaryBitmapBinary);
                        return 20;
                    });

            // Esta es la llamada clave para este test
            parserUtilMock.when(() -> ParserUtil.processFieldData(eq(MastercardISOField.BITMAP_SECONDARY), any(), eq(20), anyMap(), any()))
                    .thenAnswer(invocation -> {
                        invocation.<Map<String, String>>getArgument(3).put(MastercardISOField.BITMAP_SECONDARY.getName(), secondaryBitmapBinary);
                        return 36;
                    });

            // Mock del campo 65
            MastercardISOField field65 = mock(MastercardISOField.class);
            isoFieldMock.when(() -> MastercardISOField.getById(65)).thenReturn(field65);
            parserUtilMock.when(() -> ParserUtil.processFieldData(eq(field65), any(), eq(36), anyMap(), any())).thenReturn(50);

            // --- ACT ---
            Map<String, String> result = mastercardISOFieldParser.mapFields(DUMMY_HEX_MESSAGE);

            // --- ASSERT ---
            assertNotNull(result);
            assertEquals(secondaryBitmapBinary, result.get(MastercardISOField.BITMAP_SECONDARY.getName()));

            // Verificamos que se intentó procesar el campo 65
            parserUtilMock.verify(() -> ParserUtil.processFieldData(eq(field65), any(), anyInt(), anyMap(), any()));
        }
    }

    /**
     * Prueba la ruta de error cuando un campo activado en el bitmap no tiene
     * un mapeo válido en MastercardISOField. Cubre el bloque if (field == null).
     */
    @Test
    void mapFields_ShouldThrowParserException_WhenFieldIsInvalid() {
        // --- ARRANGE ---
        try (MockedStatic<ParserUtil> parserUtilMock = mockStatic(ParserUtil.class);
             MockedStatic<MastercardISOField> isoFieldMock = mockStatic(MastercardISOField.class, Mockito.CALLS_REAL_METHODS);
             MockedStatic<LogsTraces> logsTracesMock = mockStatic(LogsTraces.class)) {

            // Bitmap con el campo 4 activo, el cual simularemos que es inválido
            String primaryBitmapBinary = "0001" + "0".repeat(60);

            // Simulamos las llamadas iniciales a ParserUtil
            parserUtilMock.when(() -> ParserUtil.processFieldData(any(), any(), anyInt(), anyMap(), any()))
                    .thenAnswer(invocation -> {
                        MastercardISOField f = invocation.getArgument(0);
                        Map<String, String> map = invocation.getArgument(3);
                        if (f.equals(MastercardISOField.MESSAGE_TYPE)) return 4;
                        if (f.equals(MastercardISOField.BITMAP_PRIMARY)) {
                            map.put(MastercardISOField.BITMAP_PRIMARY.getName(), primaryBitmapBinary);
                            return 20;
                        }
                        return 0;
                    });

            // Simulamos que el campo 4 es inválido (getById devuelve null)
            isoFieldMock.when(() -> MastercardISOField.getById(4)).thenReturn(null);

            // Simulamos la creación del mensaje de error
            String expectedErrorMessage = "Error en campo 4: No hay mapeo disponible";
            parserUtilMock.when(() -> ParserUtil.createMessageError(4)).thenReturn(expectedErrorMessage);

            // --- ACT & ASSERT ---
            ParserException exception = assertThrows(ParserException.class,
                    () -> mastercardISOFieldParser.mapFields(DUMMY_HEX_MESSAGE));

            assertEquals(expectedErrorMessage, exception.getMessage());

            // Verificamos que se escribió en el log
            logsTracesMock.verify(() -> LogsTraces.writeInfo(contains("Campo no permitido: 4")));
        }
    }

}