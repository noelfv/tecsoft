package com.bbva.orchestrator.core.parser.iso8583.strategy.fields;

import com.bbva.orchestrator.core.parser.iso8583.ParsedFieldResult;
import com.bbva.orchestrator.core.fields.definitions.IFieldDefinition;
import com.bbva.orchestrator.core.parser.iso8583.strategy.FieldParserStrategy;
import com.bbva.orchestrator.core.parser.iso8583.handlers.NetworkHandlerField;
import org.springframework.stereotype.Component;

@Component
public class PlainTextFieldParser implements FieldParserStrategy {


    @Override
    public String build(String fieldValue, IFieldDefinition fieldDefinition, NetworkHandlerField networkHandlerField) {
        if (fieldValue == null || fieldValue.isEmpty()) {
            return ""; // Puedes devolver "00" para LLVAR vacío si aplica
        }

        // 1. Si es un decorador como LlvarLengthPrefixParser, delegar para obtener el prefijo LL
       if (fieldDefinition.isVariable()) {
            return buildLlvarPlainValue(fieldValue, fieldDefinition.getLength());
        }

        // 2. Si es un campo simple (NUMERIC, ALPHA, etc.)
        return buildSimplePlainValue(fieldValue, fieldDefinition);
    }

    @Override
    public ParsedFieldResult parse(String rawDataSegment, IFieldDefinition fieldDefinition, NetworkHandlerField networkHandlerField) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private String buildLlvarPlainValue(String fieldValue, int llvarLength) {
        //FieldParserStrategy actualValueParser = llvarParser.getActualValueParser();

        // 1. Obtener el valor en claro (sin EBCDIC, sin hex)
        String plainValue = buildSimplePlainValue(fieldValue, null);

        // 2. Calcular la longitud EN CARACTERES (no bytes) para el prefijo LL
        int lengthInChars = plainValue.length();
        String lengthPrefix = String.format("%0" + llvarLength + "d", lengthInChars);

        // 3. Concatenar: LL + valor en claro
        return lengthPrefix + plainValue;
    }

    private String buildSimplePlainValue(String fieldValue, IFieldDefinition fieldDefinition) {
        // Aplicar reglas específicas por tipo
        if (fieldDefinition != null) {
            return switch (fieldDefinition.getTypeData()) {
                case NUMERIC_DECIMAL -> fieldValue.replace(".", ""); // Quitar punto decimal
                default -> fieldValue;
            };
        }
        return fieldValue;
    }
}