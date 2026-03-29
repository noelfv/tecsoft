package com.bbva.orchestrator.core.parser.iso8583.strategy.fields;

import com.bbva.orchestrator.core.exception.ParserFieldsException;
import com.bbva.orchestrator.core.fields.definitions.IFieldDefinition;
import com.bbva.orchestrator.core.fields.definitions.ISODataType;
import com.bbva.orchestrator.core.parser.iso8583.ParsedFieldResult;
import com.bbva.orchestrator.core.parser.iso8583.strategy.FieldParserStrategy;
import com.bbva.orchestrator.core.utils.ISOUtil;
import com.bbva.orchestrator.core.parser.iso8583.handlers.NetworkHandlerField;


public class AlphaNumericDecoratorFieldParser implements FieldParserStrategy {

    @Override
    public ParsedFieldResult parse(String rawDataSegment, IFieldDefinition fieldDefinition, NetworkHandlerField networkHandlerField) {
        try{
            String decodedValue = networkHandlerField.decode(rawDataSegment, fieldDefinition.getTypeData());
            int consumed = rawDataSegment.length();

            return new ParsedFieldResult(decodedValue, consumed);
        } catch (RuntimeException e) {
            throw new ParserFieldsException("PGWP-00107","Error procesando campo " + fieldDefinition.getIdentifier() + " ¨[" + rawDataSegment + "]",e);
        }
    }

    @Override
    public String build(String processedDataSegment, IFieldDefinition fieldDefinition, NetworkHandlerField networkHandlerField) {
        try{
            String encodeValue;
            if(fieldDefinition.getTypeData().equals(ISODataType.ALPHA_NUMERIC)){
                encodeValue = ISOUtil.stringToEBCDICHex(processedDataSegment);
            }else {
                encodeValue=processedDataSegment;
            }
            return encodeValue;
        } catch (RuntimeException e) {
            throw new ParserFieldsException("PGWP-00107","Error procesando campo " + fieldDefinition.getIdentifier() + " (" + fieldDefinition.getName() + ")" ,e);
        }
    }
}