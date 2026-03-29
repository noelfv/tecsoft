package com.bbva.orchestrator.core.parser.iso8583.strategy.subfields;

import com.bbva.gateway.interceptors.GrpcHeadersInfo;
import com.bbva.gateway.utils.LogsTraces;
import com.bbva.orchestrator.core.fields.definitions.IFieldDefinition;
import com.bbva.orchestrator.core.fields.definitions.ISODataType;
import com.bbva.orchestrator.core.fields.definitions.subfields.tlv.Field48;
import com.bbva.orchestrator.core.fields.definitions.subfields.tlv.TLVFieldLoadStructure;
import com.bbva.orchestrator.core.parser.iso8583.ParsedFieldResult;
import com.bbva.orchestrator.core.parser.iso8583.handlers.NetworkHandlerField;
import com.bbva.orchestrator.core.parser.iso8583.strategy.FieldParserStrategy;
import com.bbva.orchestrator.core.utils.ISOUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompositeTlvFieldParserTest {

    private CompositeTlvFieldParser parser;

    @Mock private IFieldDefinition fieldDefinition;
    @Mock private NetworkHandlerField networkHandler;
    @Mock private Field48 subFieldDefMock;

    // Mocks estáticos necesarios
    private MockedStatic<LogsTraces> logsTracesMock;
    private MockedStatic<TLVFieldLoadStructure> tlvStaticMock;
    private MockedStatic<ISOUtil> isoUtilMock;

    private Map<String, Field48> subFieldDefinitions;

    @BeforeEach
    void setUp() {
        // 1. ORDEN CRÍTICO: Mockear LogsTraces primero para evitar el NullPointerException del static block
        logsTracesMock = mockStatic(LogsTraces.class);

        // 2. Mockear TLVFieldLoadStructure (ahora es seguro)
        tlvStaticMock = mockStatic(TLVFieldLoadStructure.class);

        // 3. Mockear ISOUtil
        isoUtilMock = mockStatic(ISOUtil.class);

        subFieldDefinitions = new LinkedHashMap<>();
    }

    @AfterEach
    void tearDown() {
        // Cerrar mocks en orden inverso para limpieza segura
        if (tlvStaticMock != null) tlvStaticMock.close();
        if (isoUtilMock != null) isoUtilMock.close();
        if (logsTracesMock != null) logsTracesMock.close();
    }

    // --- 1. TESTS DE CONSTRUCTORES Y BUILD ---

    @Test
    void testConstructors_WithNullOrEmptyDefinitions_LogsWarning() {
        // Test constructor con ID
        tlvStaticMock.when(() -> TLVFieldLoadStructure.getSubFieldDefinitionsForComposite("99"))
                .thenReturn(null);
        new CompositeTlvFieldParser("99");
        logsTracesMock.verify(() -> LogsTraces.writeWarning(contains("definiciones de subcampo vacías")));

        // Test constructor con Map explícito
        new CompositeTlvFieldParser("99", new HashMap<>());
        logsTracesMock.verify(() -> LogsTraces.writeWarning(contains("definiciones de subcampo vacías")), times(2));
    }

    @Test
    void testBuild_ReturnsEmptyString() {
        parser = new CompositeTlvFieldParser("48", subFieldDefinitions);
        assertEquals("", parser.build("any", fieldDefinition, networkHandler));
    }

    // --- 2. TESTS DEL FLUJO PRINCIPAL Y PARSE ---

    @Test
    void testParse_SimpleLeafTlv_ReturnsJson() {
        // Preparamos datos
        subFieldDefinitions.put("10", subFieldDefMock);
        parser = new CompositeTlvFieldParser("48", subFieldDefinitions);
        String rawData = "TAG_LEN_VALU";

        // Mocks de comportamiento
        isoUtilMock.when(() -> ISOUtil.ebcdicToString("TAG_")).thenReturn("10");
        isoUtilMock.when(() -> ISOUtil.ebcdicToString("LEN_")).thenReturn("02"); // 2 bytes -> 4 hex chars

        when(subFieldDefMock.getTypeData()).thenReturn(ISODataType.ALPHA_NUMERIC);
        when(networkHandler.decode("VALU", ISODataType.ALPHA_NUMERIC)).thenReturn("HOLA");

        // Sin hijos anidados
        tlvStaticMock.when(() -> TLVFieldLoadStructure.getSubFieldDefinitionsForComposite("10"))
                .thenReturn(Collections.emptyMap());

        // Ejecución
        ParsedFieldResult result = parser.parse(rawData, fieldDefinition, networkHandler);

        // Verificación
        assertNotNull(result);
        assertEquals(rawData.length(), result.consumedLengthInChars());
        // Verificamos el formato JSON
        assertTrue(result.value().contains("\"48.10\": \"HOLA\""));
    }

    // --- 3. TEST CASO ESPECIAL 48.01 (FIJO AL INICIO) ---

    @Test
    void testParseToMap_SpecialCase4801() {
        // Configuración del subcampo 01
        Field48 field01 = mock(Field48.class);
        FieldParserStrategy strategy01 = mock(FieldParserStrategy.class);

        when(field01.isVariable()).thenReturn(false);
        when(field01.getLength()).thenReturn(2); // 2 bytes -> 4 chars hex
        when(field01.getParserStrategy()).thenReturn(strategy01);

        // Simular respuesta del parser interno del 01
        when(strategy01.parse(anyString(), any(), any()))
                .thenReturn(new ParsedFieldResult("VALOR_FIJO", 4));

        subFieldDefinitions.put("01", field01);
        parser = new CompositeTlvFieldParser("48", subFieldDefinitions);

        // Trama: "AAAA" (Fijo) + Resto vacío
        String rawData = "AAAA";

        // Ejecución
        Map<String, String> result = parser.parseToMap(rawData, fieldDefinition, networkHandler);

        // Verificación
        assertEquals("VALOR_FIJO", result.get("48.01"));
    }

    @Test
    void testParseToMap_SpecialCase4801_Truncated_ThrowsExceptionCaught() {
        // Configuración del subcampo 01
        Field48 field01 = mock(Field48.class);
        when(field01.isVariable()).thenReturn(false);
        when(field01.getLength()).thenReturn(10); // Espera 20 chars

        subFieldDefinitions.put("01", field01);
        parser = new CompositeTlvFieldParser("48", subFieldDefinitions);

        String rawData = "FF"; // Muy corto

        // Ejecución
        Map<String, String> result = parser.parseToMap(rawData, fieldDefinition, networkHandler);

        // Verificación: Debe capturar la excepción y loguear warning
        logsTracesMock.verify(() -> LogsTraces.writeWarning(contains("interrumpido por error")));
    }

    // --- 4. TEST SUB-CAMPOS VARIABLES (NESTED VARIABLE) ---

    @Test
    void testParseNestedVariableSubField() {
        // Setup Tag Padre (33)
        subFieldDefinitions.put("33", subFieldDefMock);
        parser = new CompositeTlvFieldParser("48", subFieldDefinitions);

        // Setup Hijo Variable
        Field48 childVariable = mock(Field48.class);
        when(childVariable.isVariable()).thenReturn(true);
        Map<String, Field48> nestedDefs = Collections.singletonMap("XX", childVariable); // "XX" es dummy aquí, importa el firstChild

        tlvStaticMock.when(() -> TLVFieldLoadStructure.getSubFieldDefinitionsForComposite("33"))
                .thenReturn(nestedDefs);

        // Trama: TAG(33) + LEN(Total) + [ SUBTAG + SUBLEN + SUBVAL ]
        String rawData = "TAG_LEN_SUBTSLENVALU";

        // Mocks ISOUtil
        isoUtilMock.when(() -> ISOUtil.ebcdicToString("TAG_")).thenReturn("33");
        isoUtilMock.when(() -> ISOUtil.ebcdicToString("LEN_")).thenReturn("06"); // Longitud total valor

        // Mocks Loop Interno
        isoUtilMock.when(() -> ISOUtil.ebcdicToString("SUBT")).thenReturn("01"); // Subtag 01
        isoUtilMock.when(() -> ISOUtil.ebcdicToString("SLEN")).thenReturn("02"); // SubLen 2 bytes
        isoUtilMock.when(() -> ISOUtil.ebcdicToString("VALU")).thenReturn("DATA"); // Valor

        // Ejecución
        Map<String, String> result = parser.parseToMap(rawData, fieldDefinition, networkHandler);

        // Verificación
        assertEquals("DATA", result.get("48.33.01"));
    }

    @Test
    void testParseNestedVariableSubField_ErrorInternal_StopsGracefully() {
        // 1. Configurar que el Tag 33 existe y es variable
        subFieldDefinitions.put("33", subFieldDefMock);
        parser = new CompositeTlvFieldParser("48", subFieldDefinitions);

        Field48 childVariable = mock(Field48.class);
        when(childVariable.isVariable()).thenReturn(true);
        // Retornamos un mapa dummy para que entre en la lógica anidada
        tlvStaticMock.when(() -> TLVFieldLoadStructure.getSubFieldDefinitionsForComposite("33"))
                .thenReturn(Collections.singletonMap("Dummy", childVariable));

        // 2. Trama Controlada: TAG (4) + LEN (4) + VAL (4)
        // Usamos strings simples en lugar de hex complejo para facilitar el mock
        String rawData = "TAG_LEN_BAD_";

        // 3. Configurar Mocks para el bucle EXTERNO (parseToMap)
        // Esto asegura que llegue hasta la llamada de 'parseNestedVariableSubField'
        isoUtilMock.when(() -> ISOUtil.ebcdicToString("TAG_")).thenReturn("33");
        isoUtilMock.when(() -> ISOUtil.ebcdicToString("LEN_")).thenReturn("02"); // 2 bytes = 4 chars ("BAD_")

        // 4. Configurar Mocks para el bucle INTERNO (parseNestedVariableSubField)
        // Hacemos que falle intencionalmente al intentar leer el sub-tag "BAD_"
        isoUtilMock.when(() -> ISOUtil.ebcdicToString("BAD_")).thenThrow(new RuntimeException("Fallo interno simulado"));

        // Ejecución
        parser.parseToMap(rawData, fieldDefinition, networkHandler);

        // Verificación
        // Ahora sí, verificamos que el catch INTERNO capturó el error y escribió el log específico
        logsTracesMock.verify(() -> LogsTraces.writeWarning(contains("Sub-subcampo variable 33 corrupto")));
    }

    // --- 5. TEST SUB-CAMPOS FIJOS/REPETITIVOS (NESTED FIXED) ---

    @Test
    void testParseNestedFixedSubField_RepetitiveBlocks() {
        // Tag Padre (61)
        subFieldDefinitions.put("61", subFieldDefMock);
        parser = new CompositeTlvFieldParser("48", subFieldDefinitions);

        // Definición de bloques internos: 01 y 02 (ambos de longitud 2 bytes = 4 chars)
        Field48 f01 = mock(Field48.class);
        when(f01.getLength()).thenReturn(2);
        when(f01.isVariable()).thenReturn(false); // ESTE SÍ SE USA (es el firstChild)

        Field48 f02 = mock(Field48.class);
        when(f02.getLength()).thenReturn(2);
        // when(f02.isVariable()).thenReturn(false); <--- ELIMINAR ESTA LÍNEA (Nunca se llama)

        // Usamos LinkedHashMap para asegurar orden de iteración
        Map<String, Field48> fixedDefs = new LinkedHashMap<>();
        fixedDefs.put("01", f01);
        fixedDefs.put("02", f02);

        tlvStaticMock.when(() -> TLVFieldLoadStructure.getSubFieldDefinitionsForComposite("61"))
                .thenReturn(fixedDefs);

        // Trama: TAG + LEN + [ BLOQUE 1 (01+02) ] + [ BLOQUE 2 (01+02) ]
        // Bloque 1: AAAA BBBB
        // Bloque 2: CCCC DDDD
        String rawData = "TAG_LEN_AAAABBBBCCCCDDDD";

        isoUtilMock.when(() -> ISOUtil.ebcdicToString("TAG_")).thenReturn("61");
        isoUtilMock.when(() -> ISOUtil.ebcdicToString("LEN_")).thenReturn("08"); // 4 campos * 2 bytes = 8 bytes

        when(networkHandler.decode(anyString(), any())).thenAnswer(inv -> "DEC_" + inv.getArgument(0));

        // Ejecución
        Map<String, String> result = parser.parseToMap(rawData, fieldDefinition, networkHandler);

        // Verificación
        assertEquals("DEC_AAAA", result.get("48.61.01"));
        assertEquals("DEC_BBBB", result.get("48.61.02"));
        assertEquals("DEC_CCCC", result.get("48.61.03"));
        assertEquals("DEC_DDDD", result.get("48.61.04"));
    }

    @Test
    void testParseNestedFixedSubField_NonNumericTag_UsesUnderscore() {
        // Caso Borde: Subcampo fijo cuyo ID no es numérico (ej: "AA").
        // La lógica de ID virtual falla y usa fallback "TAG_INDEX".

        subFieldDefinitions.put("61", subFieldDefMock);
        parser = new CompositeTlvFieldParser("48", subFieldDefinitions);

        Field48 fAA = mock(Field48.class);
        when(fAA.getLength()).thenReturn(1);
        when(fAA.isVariable()).thenReturn(false);

        tlvStaticMock.when(() -> TLVFieldLoadStructure.getSubFieldDefinitionsForComposite("61"))
                .thenReturn(Collections.singletonMap("AA", fAA));

        String rawData = "TAG_LEN_1122"; // Dos bloques de 1 byte (2 chars)
        isoUtilMock.when(() -> ISOUtil.ebcdicToString("TAG_")).thenReturn("61");
        isoUtilMock.when(() -> ISOUtil.ebcdicToString("LEN_")).thenReturn("02");

        when(networkHandler.decode("11", null)).thenReturn("VAL1");
        when(networkHandler.decode("22", null)).thenReturn("VAL2");

        Map<String, String> result = parser.parseToMap(rawData, fieldDefinition, networkHandler);

        // Bloque 0: AA
        assertEquals("VAL1", result.get("48.61.AA"));
        // Bloque 1: AA_1
        assertEquals("VAL2", result.get("48.61.AA_1"));
    }

    @Test
    void testParseNestedFixedSubField_ExceptionCaught() {
        subFieldDefinitions.put("61", subFieldDefMock);
        parser = new CompositeTlvFieldParser("48", subFieldDefinitions);

        Field48 f01 = mock(Field48.class); when(f01.getLength()).thenReturn(10); // Pide mucho
        when(f01.isVariable()).thenReturn(false);

        tlvStaticMock.when(() -> TLVFieldLoadStructure.getSubFieldDefinitionsForComposite("61"))
                .thenReturn(Collections.singletonMap("01", f01));

        // Trama corta
        String rawData = "TAG_LEN_FF";
        isoUtilMock.when(() -> ISOUtil.ebcdicToString("TAG_")).thenReturn("61");
        isoUtilMock.when(() -> ISOUtil.ebcdicToString("LEN_")).thenReturn("01");

        parser.parseToMap(rawData, fieldDefinition, networkHandler);

        logsTracesMock.verify(() -> LogsTraces.writeWarning(contains("truncado o malformado")));
    }

    // --- 6. TESTS DE ERRORES GENERALES ---

    @Test
    void testParseToMap_UnknownTag_LogsAndContinues() {
        subFieldDefinitions.clear(); // Sin definiciones
        parser = new CompositeTlvFieldParser("48", subFieldDefinitions);

        // Trama con Tag desconocido pero válida
        String rawData = "TAG_LEN_VALU";

        isoUtilMock.when(() -> ISOUtil.ebcdicToString("TAG_")).thenReturn("99");
        isoUtilMock.when(() -> ISOUtil.ebcdicToString("LEN_")).thenReturn("02");

        Map<String, String> result = parser.parseToMap(rawData, fieldDefinition, networkHandler);

        assertTrue(result.isEmpty());
        logsTracesMock.verify(() -> LogsTraces.writeWarning(contains("Subcampo desconocido ignorado")));
    }

    @Test
    void testParseToMap_MainLoopException_TruncatedTag() {
        // Trama que acaba abruptamente mientras leía Tag
        parser = new CompositeTlvFieldParser("48", subFieldDefinitions);
        String rawData = "TA"; // Menos de 4 chars

        Map<String, String> result = parser.parseToMap(rawData, fieldDefinition, networkHandler);
        // Debe salir limpiamente sin excepción (break en validación de bordes) o catch
        assertTrue(result.isEmpty());
    }

    // --- 7. TESTS PARA CUBRIR RAMAS CONDICIONALES (IF/ELSE) RESTANTES ---

    @Test
    void testParseToMap_CompositeIdNot48_SkipsSpecialCase01() {
        // Escenario: El ID del campo NO es "48" (ej. "49").

        Field48 field01 = mock(Field48.class);

        // --- CORRECCIÓN: NO STUBBEAR MÉTODOS QUE NO SE VAN A USAR ---
        // Como el ID es "49", el parser nunca preguntará si es variable o su longitud.
        // Solo lo ponemos en el mapa para que exista si alguien lo busca, pero nada más.

        subFieldDefinitions.put("01", field01);

        // Inicializamos con ID "49"
        parser = new CompositeTlvFieldParser("49", subFieldDefinitions);

        // Trama: "AAAA" se intentará leer como TAG del bucle principal
        String rawData = "AAAA0000";

        // Mockeamos ISOUtil para el bucle principal
        isoUtilMock.when(() -> ISOUtil.ebcdicToString("AAAA")).thenReturn("99"); // Tag desconocido
        isoUtilMock.when(() -> ISOUtil.ebcdicToString("0000")).thenReturn("00"); // Longitud dummy

        // Ejecución
        Map<String, String> result = parser.parseToMap(rawData, fieldDefinition, networkHandler);

        // Verificación
        assertTrue(result.isEmpty());
        logsTracesMock.verify(() -> LogsTraces.writeWarning(contains("Subcampo desconocido ignorado")));
    }

    @Test
    void testParseToMap_Field01IsVariable_SkipsSpecialCaseFixedParsing() {
        // Escenario: Estamos en el campo "48", PERO el subcampo "01" está definido como VARIABLE.
        // La condición !sf01Def.isVariable() será falsa, y debe saltarse el bloque fijo inicial.

        Field48 field01 = mock(Field48.class);
        when(field01.isVariable()).thenReturn(true); // <--- ESTO ES LA CLAVE DEL TEST
        // Nota: No necesitamos mockear getLength() ni getParserStrategy() porque el if debe fallar antes.

        subFieldDefinitions.put("01", field01);
        parser = new CompositeTlvFieldParser("48", subFieldDefinitions);

        // Trama: "TAG_" + ...
        String rawData = "TAG_LEN_VALU";

        // Mocks para bucle principal (demuestra que saltó la lectura fija inicial)
        isoUtilMock.when(() -> ISOUtil.ebcdicToString("TAG_")).thenReturn("99");
        isoUtilMock.when(() -> ISOUtil.ebcdicToString("LEN_")).thenReturn("02");

        // Ejecución
        Map<String, String> result = parser.parseToMap(rawData, fieldDefinition, networkHandler);

        // Verificación
        assertTrue(result.isEmpty());
        // Si hubiera intentado leer el 01 fijo, habría consumido bytes o fallado antes del bucle while.
    }

    @Test
    void testParseNestedVariableSubField_WithTrailingGarbage_BreaksCleanly() {
        // Escenario: Bucle interno (Tag 33). Hay datos válidos, pero sobran 2 caracteres al final ("XX").

        subFieldDefinitions.put("33", subFieldDefMock);
        parser = new CompositeTlvFieldParser("48", subFieldDefinitions);

        Field48 childVariable = mock(Field48.class);
        when(childVariable.isVariable()).thenReturn(true);
        tlvStaticMock.when(() -> TLVFieldLoadStructure.getSubFieldDefinitionsForComposite("33"))
                .thenReturn(Collections.singletonMap("Dummy", childVariable));

        // Trama: TAG(4) + LEN(4) + SUBT(4) + SLEN(4) + VALU(4) + XX(2) = 22 caracteres
        // Contenido interno (Value del Tag 33): SUBT(4) + SLEN(4) + VALU(4) + XX(2) = 14 chars hex = 7 bytes
        String rawData = "TAG_LEN_SUBTSLENVALUXX";

        isoUtilMock.when(() -> ISOUtil.ebcdicToString("TAG_")).thenReturn("33");
        isoUtilMock.when(() -> ISOUtil.ebcdicToString("LEN_")).thenReturn("07"); // 7 bytes de contenido total

        // Parseo interno
        isoUtilMock.when(() -> ISOUtil.ebcdicToString("SUBT")).thenReturn("01");

        // --- CORRECCIÓN AQUÍ ---
        // Cambiamos "01" por "02".
        // "02" bytes significa que el valor ocupa 4 caracteres Hex ("VALU").
        isoUtilMock.when(() -> ISOUtil.ebcdicToString("SLEN")).thenReturn("02");

        isoUtilMock.when(() -> ISOUtil.ebcdicToString("VALU")).thenReturn("V");

        // Ejecución
        Map<String, String> result = parser.parseToMap(rawData, fieldDefinition, networkHandler);

        // Verificación
        assertEquals("V", result.get("48.33.01"));
    }
}