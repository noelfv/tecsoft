package com.bbva.orchestrator.core.parser.iso8583.strategy.subfields;

import com.bbva.gateway.utils.LogsTraces;
import com.bbva.orchestrator.core.fields.definitions.subfields.fixed.CompositeFieldDefinition;
import com.bbva.orchestrator.core.parser.iso8583.ParsedSubFieldResult;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class CompositeVariableFieldParser {

    private final Map<String, CompositeFieldDefinition> definitions;

    public CompositeVariableFieldParser(List<CompositeFieldDefinition> definitionList) {
        this.definitions = definitionList.stream()
                .collect(Collectors.toMap(CompositeFieldDefinition::getId, d -> d));
    }

    /**
     * Parsea un valor de campo compuesto permitiendo truncado (Variable Length).
     * Usa un bucle WHILE que consume la trama hasta que se agota la longitud.
     */
    public Map<String, String> buildSubFieldsSpecific(String fieldId, String rawValue) {
        CompositeFieldDefinition definition = definitions.get(fieldId);

        if (definition == null || rawValue == null || rawValue.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> result = new LinkedHashMap<>();

        List<ParsedSubFieldResult> subFields = definition.getSubFields();
        int totalLength = rawValue.length();
        int currentPosition = 0;
        int subFieldIndex = 0; // Índice para recorrer la lista de definiciones

        // BUCLE PRINCIPAL: Mientras quede data por consumir
        while (currentPosition < totalLength) {

            if (subFieldIndex >= subFields.size()) {
                LogsTraces.writeWarning("Data restante en campo " + fieldId + " sin definición de subcampo asociada. Se ignora.");
                break;
            }

            ParsedSubFieldResult sub = subFields.get(subFieldIndex);

            int endPosition = currentPosition + sub.length();

            // VERIFICACIÓN DE INTEGRIDAD (TRUNCADO):
            // Si el siguiente corte se pasa de la longitud total
            if (endPosition > totalLength) {
                break;
            }

            // Cortar y guardar
            String substring = rawValue.substring(currentPosition, endPosition);
            result.put(sub.id(), substring);

            // Actualizamos punteros
            currentPosition = endPosition;
            subFieldIndex++;
        }
        return result;
    }
}