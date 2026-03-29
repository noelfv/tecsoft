package com.bbva.orchestrator.core.parser.iso8583.handlers.impl;

import com.bbva.orchestrator.core.exception.ParserFieldsException;
import com.bbva.orchestrator.core.fields.definitions.IFieldDefinition;
import com.bbva.orchestrator.core.fields.definitions.ISODataType;
import com.bbva.orchestrator.core.parser.iso8583.handlers.NetworkHandlerField;
import com.bbva.orchestrator.core.utils.ISOUtil;
import org.springframework.stereotype.Component;

@Component
public class MastercardHandlerField implements NetworkHandlerField {

    @Override
    public int getHeaderFieldVar(IFieldDefinition fieldDefinition) {
        return fieldDefinition.getLength() * 2;
    }

    @Override
    public int decodeHeaderFieldVar(int lengthHeader, String rawDataSegment, IFieldDefinition fieldDefinition) {
        try {
            String lengthHeaderEncode = rawDataSegment.substring(0, lengthHeader);
            int lengthHeaderDecode = Integer.parseInt(ISOUtil.ebcdicToString(lengthHeaderEncode));
            return lengthHeaderDecode * 2;
        } catch (RuntimeException e) {
            throw new ParserFieldsException("[PGWP-00141]", "Error en decodeHeaderFieldVar lengthHeader=[" + lengthHeader + "]  rawData=[" + rawDataSegment + "]", e);
        }
    }

    @Override
    public int decodeLengthField(IFieldDefinition fieldDefinition) {
        return fieldDefinition.getLength() * 2;
    }

    @Override
    public String decode(String valueEncode, ISODataType dataType) {
        return switch (dataType) {
            case NUMERIC, NUMERIC_DECIMAL -> ISOUtil.ebcdicToString(valueEncode);
            case ALPHA_NUMERIC -> ISOUtil.convertHEXtoEBCDIC(valueEncode);
            case BINARY_STRING -> ISOUtil.convertHEXtoBITMAP(valueEncode);
            default -> valueEncode;
        };
    }

    @Override
    public String encodeHeaderFieldVar(String lengthHeaderDecode, IFieldDefinition fieldDefinition) {
        int actualLengthInBytes = switch (fieldDefinition.getTypeData()) {
            case HEXADECIMAL -> lengthHeaderDecode.length() / 2;
            default -> lengthHeaderDecode.length();
        };
        String lengthHeaderFormatted = String.format("%0" + fieldDefinition.getLength() + "d", actualLengthInBytes);
        return ISOUtil.stringToEBCDICHex(lengthHeaderFormatted);
    }

    @Override
    public String encode(String processedDataSegment, ISODataType dataType) {
        return switch (dataType) {
            case NUMERIC, NUMERIC_DECIMAL, ALPHA_NUMERIC -> ISOUtil.stringToEBCDICHex(processedDataSegment);
            case BINARY_STRING -> ISOUtil.convertBITMAPtoHEX(processedDataSegment);
            default -> processedDataSegment;
        };
    }
}