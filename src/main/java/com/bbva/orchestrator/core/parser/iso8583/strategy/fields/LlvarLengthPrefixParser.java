package com.bbva.orchestrator.core.parser.iso8583.strategy.fields;

import com.bbva.orchestrator.core.exception.ParserFieldsException;
import com.bbva.orchestrator.core.parser.iso8583.strategy.FieldParserStrategy;
import com.bbva.orchestrator.core.fields.definitions.IFieldDefinition;
import com.bbva.orchestrator.core.parser.iso8583.ParsedFieldResult;
import com.bbva.orchestrator.core.parser.iso8583.handlers.NetworkHandlerField;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LlvarLengthPrefixParser implements FieldParserStrategy {

    private final FieldParserStrategy actualValueParser;

    @Override
    public ParsedFieldResult parse(String rawDataSegment,  IFieldDefinition fieldDefinition, NetworkHandlerField networkHandlerField) {
        try{
            int prefixLengthInChars = networkHandlerField.getHeaderFieldVar(fieldDefinition);
            int actualValueDecLength= networkHandlerField.decodeHeaderFieldVar(prefixLengthInChars,rawDataSegment, fieldDefinition);
            String actualFieldDataHex = rawDataSegment.substring(prefixLengthInChars, prefixLengthInChars + actualValueDecLength);
            String parsedValue = networkHandlerField.decode(actualFieldDataHex,fieldDefinition.getTypeData());
            int totalConsumedLength = prefixLengthInChars + actualValueDecLength;
            return new ParsedFieldResult(parsedValue, totalConsumedLength);
        } catch (ParserFieldsException e) {
            throw e;
        }catch (RuntimeException e) {
            throw new ParserFieldsException("PGWP-00106","Error procesando campo " + fieldDefinition.getIdentifier() + " ¨[" + rawDataSegment + "]",e);
        }
    }

    @Override
    public String build(String fieldValue, IFieldDefinition fieldDefinition, NetworkHandlerField networkHandlerField) {
        String prefixHex = networkHandlerField.encodeHeaderFieldVar(fieldValue, fieldDefinition);
        String actualRawValueHex = networkHandlerField.encode(fieldValue, fieldDefinition.getTypeData());
        return prefixHex + actualRawValueHex;
    }
}