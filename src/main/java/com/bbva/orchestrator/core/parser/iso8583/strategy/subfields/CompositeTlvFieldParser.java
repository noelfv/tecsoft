package com.bbva.orchestrator.core.parser.iso8583.strategy.subfields;

import com.bbva.gateway.utils.LogsTraces;
import com.bbva.orchestrator.core.fields.definitions.IFieldDefinition;
import com.bbva.orchestrator.core.parser.iso8583.ParsedFieldResult;
import com.bbva.orchestrator.core.parser.iso8583.strategy.FieldParserStrategy;
import com.bbva.orchestrator.core.utils.ISOUtil;
import com.bbva.orchestrator.core.fields.definitions.subfields.tlv.TLVFieldLoadStructure;
import com.bbva.orchestrator.core.fields.definitions.subfields.tlv.Field48;
import com.bbva.orchestrator.core.parser.iso8583.handlers.NetworkHandlerField;
import com.bbva.orchlib.parser.ParserException;
import java.util.LinkedHashMap;
import java.util.Map;

public class CompositeTlvFieldParser implements FieldParserStrategy {

    private static final int TAG_LENGTH_HEX = 4;        // 2 bytes → 4 caracteres hex
    private static final int LENGTH_PREFIX_HEX = 4;     // 2 bytes → 4 caracteres hex
    private final String compositeFieldId;
    private final Map<String, Field48> subFieldDefinitions;

    public CompositeTlvFieldParser(String compositeFieldId) {
        this.compositeFieldId = compositeFieldId;
        this.subFieldDefinitions = TLVFieldLoadStructure.getSubFieldDefinitionsForComposite(compositeFieldId);
        if (this.subFieldDefinitions == null || this.subFieldDefinitions.isEmpty()) {
            LogsTraces.writeWarning("CompositeSubFieldParser inicializado con definiciones de subcampo vacías o nulas para: " + compositeFieldId);
        }
    }

    public CompositeTlvFieldParser(String compositeFieldId, Map<String, Field48> subFieldDefinitions) {
        this.compositeFieldId = compositeFieldId;
        this.subFieldDefinitions = subFieldDefinitions;
        if (this.subFieldDefinitions == null || this.subFieldDefinitions.isEmpty()) {
            LogsTraces.writeWarning("CompositeSubFieldParser inicializado con definiciones de subcampo vacías o nulas para: " + compositeFieldId);
        }
    }

    @Override
    public String build(String fieldValue, IFieldDefinition fieldDefinition, NetworkHandlerField networkProfile) {
        return "";
    }

    @Override
    public ParsedFieldResult parse(String rawDataSegment, IFieldDefinition fieldDefinition, NetworkHandlerField networkProfile) {
        Map<String, String> parsedSubFieldsMap = parseToMap(rawDataSegment, fieldDefinition,networkProfile);

        // Convertir el mapa a JSON string
        StringBuilder resultBuilder = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : parsedSubFieldsMap.entrySet()) {
            if (!first) resultBuilder.append(", ");
            resultBuilder.append("\"").append(entry.getKey()).append("\": \"").append(entry.getValue()).append("\"");
            first = false;
        }
        resultBuilder.append("}");

        // Devolver el JSON como String + longitud consumida
        return new ParsedFieldResult(resultBuilder.toString(), rawDataSegment.length());
    }

    /**
     * Parsea la trama y devuelve un mapa de subcampos (clave: "48.33.01", valor: "valor_decodificado")
     */
    public Map<String, String> parseToMap(String rawDataSegment, IFieldDefinition fieldDefinition, NetworkHandlerField networkHandlerField) {
        Map<String, String> parsedSubFieldsMap = new LinkedHashMap<>();
        int currentOffset = 0;

        try {
            // --- 1. Caso especial: subcampo fijo 48.01 (Si aplica) ---
            if ("48".equals(compositeFieldId)) {
                Field48 sf01Def = subFieldDefinitions.get("01");
                if (sf01Def != null && !sf01Def.isVariable() && sf01Def.getLength() > 0) {
                    int lenHex = sf01Def.getLength() * 2;
                    // Validación rápida: si no alcanza, lanzamos excepción para ir al catch
                    if (currentOffset + lenHex > rawDataSegment.length()) {
                        throw new ParserException("Campo 48.01 truncado.");
                    }
                    String valueHex = rawDataSegment.substring(currentOffset, currentOffset + lenHex);
                    ParsedFieldResult result = sf01Def.getParserStrategy().parse(valueHex, sf01Def, networkHandlerField);
                    parsedSubFieldsMap.put(compositeFieldId + ".01", result.value());
                    currentOffset += lenHex;
                }
            }

            // --- 2. Bucle Principal TLV ---
            while (currentOffset < rawDataSegment.length()) {

                // VALIDACIÓN DE BORDES (Un solo chequeo preventivo)
                // Si queda basura al final (menos de 4 chars para un tag), salimos limpiamente.
                if (currentOffset + TAG_LENGTH_HEX > rawDataSegment.length()) break;

                // A. Leer TAG
                String tagHex = rawDataSegment.substring(currentOffset, currentOffset + TAG_LENGTH_HEX);
                String tag = ISOUtil.ebcdicToString(tagHex).trim();
                currentOffset += TAG_LENGTH_HEX;

                // B. Leer LONGITUD
                // Si falla substring o parseInt, se va al catch general
                String lenHex = rawDataSegment.substring(currentOffset, currentOffset + LENGTH_PREFIX_HEX);
                int valueLengthInBytes = Integer.parseInt(ISOUtil.ebcdicToString(lenHex));
                int valueLengthInHex = valueLengthInBytes * 2;
                currentOffset += LENGTH_PREFIX_HEX;

                // C. Leer VALOR
                // Si substring falla por overflow, se va al catch general
                String valueHex = rawDataSegment.substring(currentOffset, currentOffset + valueLengthInHex);
                currentOffset += valueLengthInHex;

                // D. Procesar Subcampo
                Field48 subFieldDef = subFieldDefinitions.get(tag);
                if (subFieldDef == null) {
                    LogsTraces.writeWarning("Subcampo desconocido ignorado: " + compositeFieldId + "." + tag);
                    continue;
                }

                Map<String, Field48> subSubFields = TLVFieldLoadStructure.getSubFieldDefinitionsForComposite(tag);

                if (!subSubFields.isEmpty()) {
                    // Estrategia compuesta (Nested)
                    Field48 firstChild = subSubFields.values().iterator().next();
                    if (firstChild.isVariable()) {
                        parseNestedVariableSubField(tag, valueHex, parsedSubFieldsMap);
                    } else {
                        parseNestedFixedSubField(tag, valueHex, parsedSubFieldsMap, networkHandlerField, subSubFields);
                    }
                } else {
                    // Estrategia simple (Hoja)
                    String result = networkHandlerField.decode(valueHex, subFieldDef.getTypeData());
                    parsedSubFieldsMap.put(compositeFieldId + "." + tag, result);
                }
            }

        } catch (Exception e) {
            // === LA RED DE SEGURIDAD ÚNICA ===
            // Captura: IndexOutOfBoundsException, NumberFormatException, ParserException, etc.
            LogsTraces.writeWarning("Parseo del Campo 48 interrumpido por error de formato/longitud. Se retorna resultado parcial. Detalle: " + e.getMessage());
        }

        // Retornamos lo que hayamos logrado capturar hasta el error
        return parsedSubFieldsMap;
    }

    /**
     * Maneja subcampos variables (TLV anidado) como 48.33.
     * Este método trabaja sobre un 'valueHex' ya aislado, por lo que un error aquí
     * no debería afectar a otros tags hermanos en el nivel superior.
     */
    private void parseNestedVariableSubField(String parentTag, String valueHex, Map<String, String> resultContainer) {
        int offset = 0;

        try {
            while (offset < valueHex.length()) {

                // 1. LEER TAG
                // Si no hay suficientes caracteres para el tag, substring lanza IndexOutOfBounds -> catch
                if (offset + TAG_LENGTH_HEX > valueHex.length()) break; // Salida limpia si sobra basura mínima

                String subTagHex = valueHex.substring(offset, offset + TAG_LENGTH_HEX);
                String subTag = ISOUtil.ebcdicToString(subTagHex).trim();
                offset += TAG_LENGTH_HEX;

                // 2. LEER LONGITUD
                // Si falta data o no es número, lanza Excepción -> catch
                String lenHex = valueHex.substring(offset, offset + LENGTH_PREFIX_HEX);
                int valueLengthInBytes = Integer.parseInt(ISOUtil.ebcdicToString(lenHex));
                int valueLengthInHex = valueLengthInBytes * 2;
                offset += LENGTH_PREFIX_HEX;

                // 3. LEER VALOR
                // Si el valor calculado excede la trama disponible, substring lanza Excepción -> catch
                String subValueHex = valueHex.substring(offset, offset + valueLengthInHex);
                offset += valueLengthInHex;

                // 4. DECODIFICAR Y GUARDAR
                String decodedValue = ISOUtil.ebcdicToString(subValueHex);
                String fullKey = compositeFieldId + "." + parentTag + "." + subTag;
                resultContainer.put(fullKey, decodedValue.trim());
            }

        } catch (Exception e) {
            // === LA RED DE SEGURIDAD LOCAL ===
            // Si el contenido interno del Tag 33 (por ejemplo) está corrupto,
            // logueamos el warning y terminamos este método.
            // El bucle principal (parseToMap) NO SE ENTERA y puede seguir con el Tag 34.
            LogsTraces.writeWarning("Sub-subcampo variable " + parentTag + " corrupto o truncado. Se detiene el procesamiento de este subcampo." );
        }
    }


    /**
     * Maneja subcampos de CONCATENACIÓN FIJA (Posicionales) anidados dentro de un Tag padre.
     * Ejemplo: Campo 48.61 (Concatenación simple) o 48.71 (Bloques Repetitivos).
     */
    private void parseNestedFixedSubField(String parentTag, String valueHex, Map<String, String> resultContainer,
                                          NetworkHandlerField networkHandlerField, Map<String, Field48> subSubFields) {
        int offset = 0;
        int blockIndex = 0;
        int blockSize = subSubFields.size();

        try {
            // Bucle para bloques repetitivos
            while (offset < valueHex.length()) {

                // Iterar definiciones (01, 02, 03...)
                for (Map.Entry<String, Field48> entry : subSubFields.entrySet()) {
                    String originalSubTag = entry.getKey();
                    Field48 subDef = entry.getValue();

                    int lengthToCut = subDef.getLength() * 2;

                    // CORTAR Y DECODIFICAR
                    // Si 'offset + lengthToCut' se pasa del largo, substring() lanza IndexOutOfBoundsException
                    String subValueHex = valueHex.substring(offset, offset + lengthToCut);
                    String decodedValue = networkHandlerField.decode(subValueHex, subDef.getTypeData());

                    // CALCULAR ID VIRTUAL
                    String finalSubTag;
                    try {
                        int idNum = Integer.parseInt(originalSubTag);
                        int virtualId = idNum + (blockIndex * blockSize);
                        finalSubTag = String.format("%02d", virtualId);
                    } catch (NumberFormatException e) {
                        finalSubTag = (blockIndex == 0) ? originalSubTag : originalSubTag + "_" + blockIndex;
                    }

                    // GUARDAR
                    String fullKey = compositeFieldId + "." + parentTag + "." + finalSubTag;
                    resultContainer.put(fullKey, decodedValue);

                    offset += lengthToCut;
                }
                blockIndex++;
            }

        } catch (Exception e) {
            // === LA RED DE SEGURIDAD ===
            // Si falta un byte, si el formato está mal, o cualquier error:
            LogsTraces.writeWarning("Subcampo anidado " + parentTag + " truncado o malformado. Se detiene el procesamiento de este subcampo.");
        }
    }
}