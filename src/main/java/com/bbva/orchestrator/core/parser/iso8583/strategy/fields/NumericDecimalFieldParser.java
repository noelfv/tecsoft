package com.bbva.orchestrator.core.parser.iso8583.strategy.fields;

import com.bbva.orchestrator.core.exception.ParserFieldsException;
import com.bbva.orchestrator.core.parser.iso8583.strategy.FieldParserStrategy;
import com.bbva.orchestrator.core.fields.definitions.IFieldDefinition;
import com.bbva.orchestrator.core.parser.iso8583.ParsedFieldResult;
import com.bbva.orchestrator.core.utils.FieldUtil;
import com.bbva.orchestrator.core.parser.iso8583.handlers.NetworkHandlerField;

public class NumericDecimalFieldParser implements FieldParserStrategy {

    @Override
    public ParsedFieldResult parse(String rawDataSegment, IFieldDefinition fieldDefinition, NetworkHandlerField networkHandlerField) {
        try{
            int expectedHexLengthCalculated = networkHandlerField.decodeLengthField(fieldDefinition);
            String extractedHex = rawDataSegment.substring(0, expectedHexLengthCalculated);
            String decodedValue = networkHandlerField.decode(extractedHex,fieldDefinition.getTypeData());
            String decodedDecimalValue = FieldUtil.validAmount(decodedValue);
            return new ParsedFieldResult(decodedDecimalValue, expectedHexLengthCalculated);
        } catch (RuntimeException e) {
            throw new ParserFieldsException("PGWP-00103","Error procesando campo " + fieldDefinition.getIdentifier() + " ¨[" + rawDataSegment + "]",e);
        }
    }

    @Override
    public String build(String processedDataSegment, IFieldDefinition fieldDefinition, NetworkHandlerField networkHandlerField) {
        String cleanValue = FieldUtil.revertValidAmount(processedDataSegment);
        return networkHandlerField.encode(cleanValue,fieldDefinition.getTypeData());
    }

}