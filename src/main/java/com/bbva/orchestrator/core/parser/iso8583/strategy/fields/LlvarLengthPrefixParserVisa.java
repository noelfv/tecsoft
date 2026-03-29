package com.bbva.orchestrator.core.parser.iso8583.strategy.fields;

import com.bbva.orchestrator.core.fields.definitions.IFieldDefinition;
import com.bbva.orchestrator.core.parser.iso8583.ParsedFieldResult;
import com.bbva.orchestrator.core.parser.iso8583.handlers.NetworkHandlerField;
import com.bbva.orchestrator.core.parser.iso8583.strategy.FieldParserStrategy;
import lombok.RequiredArgsConstructor;

/**
 * Estrategia de parseo específica de VISA (implementada como Decorator).
 * Envuelve al parser base (ej. LlvarLengthPrefixParser) para aplicar la corrección
 * del '0' a la izquierda en campos NUMERIC_ODD_VARIABLE
 */
@RequiredArgsConstructor
public class LlvarLengthPrefixParserVisa implements FieldParserStrategy {

    private final FieldParserStrategy decoratedParser;

    @Override
    public ParsedFieldResult parse(String rawDataSegment, IFieldDefinition fieldDefinition, NetworkHandlerField networkHandlerField) {
        // Delega el parseo a la estrategia base (haciendo uso del "default")
        ParsedFieldResult baseResult = decoratedParser.parse(rawDataSegment, fieldDefinition, networkHandlerField);
        String resultValue = validateZeroLeftParse(rawDataSegment,baseResult.value());

        return new ParsedFieldResult(resultValue, baseResult.consumedLengthInChars());
    }

    @Override
    public String build(String fieldValue, IFieldDefinition fieldDefinition, NetworkHandlerField networkHandlerField) {
        return decoratedParser.build(fieldValue, fieldDefinition, networkHandlerField);
    }

    private String validateZeroLeftParse(String rawDataSegment, String result){
        int lengthDecode = Integer.parseInt(rawDataSegment.substring(0, 2), 16);
        return lengthDecode != result.length() ? result.substring(1): result;
    }
}