package com.bbva.orchestrator.core.parser.iso8583.strategy.fields;

import com.bbva.orchestrator.core.parser.iso8583.strategy.FieldParserStrategy;
import com.bbva.orchestrator.core.fields.definitions.IFieldDefinition;
import com.bbva.orchestrator.core.parser.iso8583.ParsedFieldResult;
import com.bbva.orchestrator.core.parser.iso8583.handlers.NetworkHandlerField;
import com.bbva.orchestrator.core.utils.ParserUtil;

public class AlphaNumericFieldParser implements FieldParserStrategy {

    @Override
    public ParsedFieldResult parse(String rawDataSegment, IFieldDefinition fieldDefinition, NetworkHandlerField networkHandlerField) {
        return ParserUtil.processFixedLengthSubField(rawDataSegment, fieldDefinition, networkHandlerField, "PGWP-00101");
    }

    @Override
    public String build(String processedDataSegment, IFieldDefinition fieldDefinition, NetworkHandlerField networkHandlerField) {
        return ParserUtil.buildFixedLengthSubField(processedDataSegment, fieldDefinition, networkHandlerField);
    }
}