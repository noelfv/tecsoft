package com.bbva.orchestrator.core.parser.iso8583.handlers.impl;

import com.bbva.orchestrator.core.exception.ParserFieldsException;
import com.bbva.orchestrator.core.fields.definitions.IFieldDefinition;
import com.bbva.orchestrator.core.fields.definitions.ISODataType;
import com.bbva.orchestrator.core.parser.iso8583.handlers.NetworkHandlerField;
import com.bbva.orchestrator.core.utils.ISOUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class VisaHandlerField implements NetworkHandlerField {

    @Override
    public int getHeaderFieldVar(IFieldDefinition fieldDefinition) {
        return fieldDefinition.getLength();
    }

    @Override
    public int decodeHeaderFieldVar(int lengthHeader,String rawDataSegment, IFieldDefinition fieldDefinition) {
        try{
        String lengthEncode = rawDataSegment.substring(0, lengthHeader);
        int lengthDecode = Integer.parseInt(lengthEncode, 16);
        ISODataType dataType = fieldDefinition.getTypeData();
        return switch (dataType) {
            case ALPHA_NUMERIC, HEXADECIMAL -> lengthDecode * 2;
            case NUMERIC_ODD_VARIABLE -> ISOUtil.alignToEvenLength(lengthDecode);
            default -> lengthDecode;
        };

        } catch (RuntimeException e) {
            throw new ParserFieldsException("[PGWP-00141]","Error en decodeHeaderFieldVar lengthHeader=["+lengthHeader+"]  rawData=["+rawDataSegment+"]", e);
        }
    }

    @Override
    public int decodeLengthField(IFieldDefinition fieldDefinition) {
        int fieldLength = fieldDefinition.getLength();
        ISODataType dataType = fieldDefinition.getTypeData();
        return switch (dataType) {
            case ALPHA_NUMERIC, HEXADECIMAL, BINARY_STRING -> fieldLength * 2;
            case NUMERIC_ODD -> fieldLength + 1;
            default -> fieldLength;
        };
    }


    @Override
    public String decode(String valueHex, ISODataType dataType) {
        return switch (dataType) {
            case ALPHA_NUMERIC -> ISOUtil.convertHEXtoEBCDIC(valueHex);
            case BINARY_STRING -> ISOUtil.convertHEXtoBITMAP(valueHex);
            default -> valueHex;
        };
    }

    @Override
    public String encodeHeaderFieldVar(String fieldValue, IFieldDefinition fieldDefinition) {
        int actualLengthInBytes = switch (fieldDefinition.getTypeData()) {
            case HEXADECIMAL -> fieldValue.length() / 2;
            default -> fieldValue.length();
        };
        String lengthHeaderEncode = Integer.toHexString(actualLengthInBytes).toUpperCase();
        return StringUtils.leftPad(lengthHeaderEncode, fieldDefinition.getLength(), '0');
    }

    @Override
    public String encode(String processedDataSegment, ISODataType dataType) {
        return switch (dataType) {
            case NUMERIC_ODD -> "0" + processedDataSegment;
            case ALPHA_NUMERIC -> ISOUtil.stringToEBCDICHex(processedDataSegment);
            case BINARY_STRING -> ISOUtil.convertBITMAPtoHEX(processedDataSegment);
            case NUMERIC_ODD_VARIABLE -> ISOUtil.prefixZeroIfOdd(processedDataSegment);
            default -> processedDataSegment;
        };
    }
}