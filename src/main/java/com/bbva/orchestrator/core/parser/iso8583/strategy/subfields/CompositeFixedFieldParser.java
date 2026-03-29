package com.bbva.orchestrator.core.parser.iso8583.strategy.subfields;

import com.bbva.gateway.utils.LogsTraces;
import com.bbva.orchestrator.core.parser.iso8583.ParsedSubFieldResult;
import com.bbva.orchestrator.core.fields.definitions.subfields.fixed.CompositeFieldDefinition;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class CompositeFixedFieldParser {

    private final Map<String, CompositeFieldDefinition> definitions;

    public CompositeFixedFieldParser(List<CompositeFieldDefinition> definitionList) {
        this.definitions = definitionList.stream()
                .collect(Collectors.toMap(CompositeFieldDefinition::getId, d -> d));
    }

    /**
     * Parsea un valor de campo compuesto (ej. Campo 3) en un mapa de subcampos.
     * Los subcampos se procesan en orden, y el startIndex se calcula automáticamente.
     */
    public Map<String, String> buildSubFieldsSpecific(String fieldId, String rawValue) {
        CompositeFieldDefinition definition = definitions.get(fieldId);
        if (definition == null || rawValue == null || rawValue.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> result = new HashMap<>();
        int currentPosition = 0;

        for (ParsedSubFieldResult sub : definition.getSubFields()) {

            if(sub.length() == 0){
                result.put(sub.id(), rawValue.substring(currentPosition));
                break;
            }

            int endPosition = currentPosition + sub.length();

            if (endPosition > rawValue.length()) {
                LogsTraces.writeWarning("No hay suficientes datos para el subcampo: " + sub.id());
                break; // Salir si no hay más datos
            }

            String substring = rawValue.substring(currentPosition, endPosition);
            result.put(sub.id(), substring);

            currentPosition = endPosition; // Mover el puntero
        }

        return result;
    }
}