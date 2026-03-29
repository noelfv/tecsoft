package com.bbva.orchestrator.core.parser.iso8583.strategy.impl;

import com.bbva.gateway.interceptors.GrpcHeadersInfo;
import com.bbva.gateway.utils.LogsTraces;
import com.bbva.orchestrator.core.fields.definitions.IFieldDefinition;
import com.bbva.orchestrator.core.fields.definitions.subfields.tlv.TLVFieldLoadStructure;
import com.bbva.orchestrator.core.fields.definitions.subfields.tlv.Field48;
import com.bbva.orchestrator.core.parser.iso8583.ParsedFieldResult;
import com.bbva.orchestrator.core.parser.iso8583.handlers.impl.MastercardHandlerField;
import com.bbva.orchestrator.core.parser.iso8583.strategy.FieldParserStrategy;
import com.bbva.orchestrator.core.parser.iso8583.strategy.subfields.CompositeTlvFieldParser;
import com.bbva.orchestrator.core.utils.ISOUtil;
import com.bbva.orchlib.parser.ParserException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompositeTlvFieldParserTest {

    private CompositeTlvFieldParser parser;

    @Mock private IFieldDefinition mockFieldDefinition;
    @Mock private Field48 mockField48Def;
    @Mock private Field48 mockNestedField48Def;
    @Mock private FieldParserStrategy mockParserStrategy;

    private final MastercardHandlerField mastercardHandlerField = new MastercardHandlerField();

    private MockedStatic<TLVFieldLoadStructure> mockedSubFieldDefs;
    private MockedStatic<ISOUtil> mockedIsoUtil;
    private MockedStatic<LogsTraces> mockedLogs;
    private MockedStatic<GrpcHeadersInfo> mockedHeaders;

    @BeforeEach
    void setUp() {
        mockedHeaders = mockStatic(GrpcHeadersInfo.class); // Mockear primero
        when(GrpcHeadersInfo.getNetwork()).thenReturn("some_network");
        when(GrpcHeadersInfo.getSpanId()).thenReturn("some_span_id");

        mockedSubFieldDefs = mockStatic(TLVFieldLoadStructure.class);
        mockedIsoUtil = mockStatic(ISOUtil.class);
        mockedLogs = mockStatic(LogsTraces.class);
    }

    @AfterEach
    void tearDown() {
        // Cerrar en orden inverso para evitar NullPointerExceptions si setUp falla a medias
        if (mockedLogs != null) mockedLogs.close();
        if (mockedIsoUtil != null) mockedIsoUtil.close();
        if (mockedSubFieldDefs != null) mockedSubFieldDefs.close();
        if (mockedHeaders != null) mockedHeaders.close();
    }

    // --- TESTS DEL CONSTRUCTOR ---

    @Test
    void constructor_withNullDefinitions_shouldLogWarning() {
        mockedSubFieldDefs.when(() -> TLVFieldLoadStructure.getSubFieldDefinitionsForComposite("48"))
                .thenReturn(null);
        parser = new CompositeTlvFieldParser("48");
        // Usamos contains para no fallar por diferencias menores de texto
        mockedLogs.verify(() -> LogsTraces.writeWarning(contains("definiciones de subcampo vacías")));
    }

    @Test
    void secondConstructor_withNullMap_shouldLogWarning() {
        parser = new CompositeTlvFieldParser("48", null);
        mockedLogs.verify(() -> LogsTraces.writeWarning(contains("definiciones de subcampo vacías")));
    }

    // --- TESTS DE PARSEO ---

    @Test
    void parse_shouldReturnJsonStringOfParsedSubFields() {
        Map<String, Field48> subFieldDefinitions = new LinkedHashMap<>();
        subFieldDefinitions.put("01", mockField48Def);

        when(mockField48Def.isVariable()).thenReturn(false);
        when(mockField48Def.getLength()).thenReturn(4);
        when(mockField48Def.getParserStrategy()).thenReturn(mockParserStrategy);
        lenient().when(mockParserStrategy.parse(anyString(), any(IFieldDefinition.class), any()))
                .thenReturn(new ParsedFieldResult("VALUE_01", 8));

        // Mock para lectura de Hex
        mockedIsoUtil.when(() -> ISOUtil.ebcdicToString("F0F1")).thenReturn("01"); // Tag
        mockedIsoUtil.when(() -> ISOUtil.ebcdicToString("F0F4")).thenReturn("4");  // Length

        String rawData = "F0F1F0F4C1C2C3C4";
        parser = new CompositeTlvFieldParser("48", subFieldDefinitions);

        // Simulamos que NO hay estructura anidada para el Tag 01 (es una hoja)
        mockedSubFieldDefs.when(() -> TLVFieldLoadStructure.getSubFieldDefinitionsForComposite("01"))
                .thenReturn(Collections.emptyMap());

        ParsedFieldResult result = parser.parse(rawData, mockFieldDefinition, mastercardHandlerField);

        assertNotNull(result);
        assertTrue(result.value().contains("\"48.01\": \"VALUE_01\""));
        assertEquals(rawData.length(), result.consumedLengthInChars());
    }

    @Test
    void parseToMap_shouldParseFixedAndTlvSubFields() {
        // CORRECCIÓN: Estructura Principal
        Map<String, Field48> mainDefinitions = new LinkedHashMap<>();
        mainDefinitions.put("01", mockField48Def);
        mainDefinitions.put("33", mockNestedField48Def);

        // Configuración Campo 01 (Fijo)
        when(mockField48Def.isVariable()).thenReturn(false);
        when(mockField48Def.getLength()).thenReturn(4);
        when(mockField48Def.getParserStrategy()).thenReturn(mockParserStrategy);
        when(mockParserStrategy.parse("C1C2C3C4", mockField48Def, mastercardHandlerField))
                .thenReturn(new ParsedFieldResult("FIXED_VAL", 8));

        // Configuración Campo 33 (Anidado Variable)
        // IMPORTANTE: Aquí estaba el error "Actual: null". Debemos configurar qué hay DENTRO del 33.
        Field48 innerField01 = mock(Field48.class);
        when(innerField01.isVariable()).thenReturn(true);
        Map<String, Field48> nestedDefinitions = new LinkedHashMap<>();
        nestedDefinitions.put("01", innerField01); // El hijo del 33 es el 01 (variable)

        // Mockeamos la llamada estática para cuando el parser pregunte por los hijos del "33"
        mockedSubFieldDefs.when(() -> TLVFieldLoadStructure.getSubFieldDefinitionsForComposite("33"))
                .thenReturn(nestedDefinitions);

        // Mockeamos la llamada estática para cuando el parser pregunte por los hijos del "01" (hoja)
        mockedSubFieldDefs.when(() -> TLVFieldLoadStructure.getSubFieldDefinitionsForComposite("01"))
                .thenReturn(Collections.emptyMap());

        // Mocks ISOUtil
        mockedIsoUtil.when(() -> ISOUtil.ebcdicToString("F3F3")).thenReturn("33"); // Tag Padre
        mockedIsoUtil.when(() -> ISOUtil.ebcdicToString("F0F6")).thenReturn("6");  // Len Padre

        mockedIsoUtil.when(() -> ISOUtil.ebcdicToString("F0F1")).thenReturn("01"); // SubTag
        mockedIsoUtil.when(() -> ISOUtil.ebcdicToString("F0F2")).thenReturn("2");  // SubLen
        mockedIsoUtil.when(() -> ISOUtil.ebcdicToString("E5E6")).thenReturn("VW"); // SubValue

        // Trama: [FIXED 01 (8chars)] + [TAG 33 (4)] + [LEN 6 (4)] + [SUBTAG 01 (4)] + [SUBLEN 2 (4)] + [VAL VW (4)]
        String rawData = "C1C2C3C4F3F3F0F6F0F1F0F2E5E6";

        parser = new CompositeTlvFieldParser("48", mainDefinitions);

        Map<String, String> resultMap = parser.parseToMap(rawData, mockFieldDefinition, mastercardHandlerField);

        assertEquals("FIXED_VAL", resultMap.get("48.01"));
        assertEquals("VW", resultMap.get("48.33.01"));
    }

    @Test
    void parseToMap_withUndefinedSubField_shouldLogWarningAndContinue() {
        Map<String, Field48> subFieldDefinitions = Collections.emptyMap();
        mockedIsoUtil.when(() -> ISOUtil.ebcdicToString("F9F9")).thenReturn("99");
        mockedIsoUtil.when(() -> ISOUtil.ebcdicToString("F0F2")).thenReturn("2");

        String rawData = "F9F9F0F2C1C2";
        parser = new CompositeTlvFieldParser("48", subFieldDefinitions);

        Map<String, String> resultMap = parser.parseToMap(rawData, mockFieldDefinition, mastercardHandlerField);

        assertTrue(resultMap.isEmpty());
        // CORRECCIÓN: El mensaje debe coincidir exactamente con el código o usar contains
        mockedLogs.verify(() -> LogsTraces.writeWarning(contains("Subcampo desconocido ignorado")));
    }

    // --- TESTS DE ERRORES (CATCH BLOCKS) ---
    // NOTA: Como el parser tiene un try-catch general, NO lanza excepción, sino que loguea y retorna parcial.

    @Test
    void parseToMap_withInsufficientDataForLength_shouldLogWarning() {
        String rawData = "F0F1"; // Tag 01, sin longitud
        parser = new CompositeTlvFieldParser("48", Collections.emptyMap());
        mockedIsoUtil.when(() -> ISOUtil.ebcdicToString("F0F1")).thenReturn("01");

        // Ejecutamos (no esperamos excepción)
        parser.parseToMap(rawData, mockFieldDefinition, mastercardHandlerField);

        // Verificamos que se capturó el error
        mockedLogs.verify(() -> LogsTraces.writeWarning(contains("interrumpido por error")));
    }

    @Test
    void parseToMap_withInsufficientDataForValue_shouldLogWarning() {
        String rawData = "F0F1F0F8"; // Tag 01, Longitud 8, Sin valor
        parser = new CompositeTlvFieldParser("48", Collections.emptyMap());
        mockedIsoUtil.when(() -> ISOUtil.ebcdicToString("F0F1")).thenReturn("01");
        mockedIsoUtil.when(() -> ISOUtil.ebcdicToString("F0F8")).thenReturn("8");

        parser.parseToMap(rawData, mockFieldDefinition, mastercardHandlerField);

        mockedLogs.verify(() -> LogsTraces.writeWarning(contains("interrumpido por error")));
    }

    @Test
    void parseNestedVariableSubField_withInsufficientData_shouldLogWarning() {
        Map<String, Field48> subFieldDefinitions = new LinkedHashMap<>();
        subFieldDefinitions.put("33", mockNestedField48Def);

        // Configuramos para que entre a la lógica anidada
        Field48 innerDummy = mock(Field48.class);
        when(innerDummy.isVariable()).thenReturn(true);
        mockedSubFieldDefs.when(() -> TLVFieldLoadStructure.getSubFieldDefinitionsForComposite("33"))
                .thenReturn(Collections.singletonMap("Dummy", innerDummy));

        mockedIsoUtil.when(() -> ISOUtil.ebcdicToString("F3F3")).thenReturn("33");

        // CAMBIO CLAVE 1: Longitud externa válida
        // Decimos que la longitud es 4 bytes (8 chars) para que coincida con lo que enviamos ("F0F1F0F2")
        // Así el parser exterior NO falla y llama al método privado.
        mockedIsoUtil.when(() -> ISOUtil.ebcdicToString("F0F4")).thenReturn("4");

        // CAMBIO CLAVE 2: Forzar el fallo interno
        // Dentro, lee Tag "F0F1" (01) y Len "F0F2".
        // Hacemos que "F0F2" diga que necesita 5 bytes (10 chars), pero ya no quedan datos.
        mockedIsoUtil.when(() -> ISOUtil.ebcdicToString("F0F1")).thenReturn("01"); // SubTag
        mockedIsoUtil.when(() -> ISOUtil.ebcdicToString("F0F2")).thenReturn("5");  // SubLen (Excesiva)

        // Trama ajustada: Tag(33) + Len(4) + [SubTag(01) + SubLen(Excessive)]
        String rawData = "F3F3F0F4F0F1F0F2";
        parser = new CompositeTlvFieldParser("48", subFieldDefinitions);

        parser.parseToMap(rawData, mockFieldDefinition, mastercardHandlerField);

        // Verificamos warning interno del nested loop
        mockedLogs.verify(() -> LogsTraces.writeWarning(contains("corrupto")));
    }

    @Test
    void build_shouldReturnEmptyString() {
        parser = new CompositeTlvFieldParser("48", Collections.emptyMap());
        String result = parser.build("some_value", mockFieldDefinition,mastercardHandlerField);
        assertEquals("", result);
    }
}