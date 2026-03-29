package com.bbva.orchestrator.core.utils;

import com.bbva.orchestrator.core.exception.ParserFieldsException;
import com.bbva.orchestrator.core.fields.definitions.IFieldDefinition;
import com.bbva.orchestrator.core.fields.definitions.ISOField;
import com.bbva.orchestrator.core.parser.iso8583.ParsedFieldResult;
import com.bbva.orchestrator.core.parser.iso8583.handlers.NetworkHandlerField;
import com.bbva.orchlib.parser.ParserException;
import lombok.NoArgsConstructor;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
public class ParserUtil {

    private static final String PRIMARY_ACCOUNT_NUMBER = "primaryAccountNumber";
    private static final String MASK_CHAR = "*";
    private static final String DEFAULT_MASK_CHAR = "0";
    // ---- Metodos para Fields ----

    public static int processFieldData(ISOField isoField, StringBuilder isoMessage, int currentPosition, Map<String, String> valuesMap, NetworkHandlerField networkHandlerField) {
        try {
            String remainingMessageSegment = isoMessage.substring(currentPosition);
            ParsedFieldResult result;

            result = isoField.getParserStrategy().parse(remainingMessageSegment,  isoField,networkHandlerField);

            valuesMap.put(isoField.getName(), result.value());

            currentPosition += result.consumedLengthInChars(); // Usar getConsumedLength()

            return currentPosition;

        } catch (ParserFieldsException e) {
            throw new ParserException(FieldUtil.formatMessageException(e.getCode(),e.getDescription(),e));
        }
    }

    // -- > falta agregar el plain text, eso falta

    public static String createMessageError(int fieldId) {
        return "Error en campo " + fieldId + ": " + "No hay mapeo disponible";
    }

    // ---- Metodos para Subfields ----
    public static ParsedFieldResult processFixedLengthSubField(String rawDataSegment, IFieldDefinition fieldDefinition, NetworkHandlerField networkHandlerField, String errorCode) {
        try {
            int expectedHexLengthCalculated = networkHandlerField.decodeLengthField(fieldDefinition);
            String extractedHex = rawDataSegment.substring(0, expectedHexLengthCalculated);
            String decodedValue = networkHandlerField.decode(extractedHex, fieldDefinition.getTypeData());
            return new ParsedFieldResult(decodedValue, expectedHexLengthCalculated);
        } catch (RuntimeException e) {
            throw new ParserFieldsException(errorCode, "Error procesando campo " + fieldDefinition.getIdentifier() + " ¨[" + rawDataSegment + "]", e);
        }
    }

    public static String buildFixedLengthSubField(String processedDataSegment, IFieldDefinition fieldDefinition, NetworkHandlerField networkHandlerField) {
        return networkHandlerField.encode(processedDataSegment, fieldDefinition.getTypeData());
    }

    public static Map<String, String> maskSensitiveFields(Map<String, String> mapValues) {
        Map<String, String> mapMaskedValues = new HashMap<>(mapValues);
        // Define the fields to mask
        String[] sensitiveFields = {
                "primaryAccountNumber",
                "trackOneData",
                "trackTwoData"
        };

        for (String fieldName : sensitiveFields) {
            mapMaskedValues.computeIfPresent(fieldName, (key, value) -> {
                if (PRIMARY_ACCOUNT_NUMBER.equals(key)) {
                    return obfuscate(value, "left", MASK_CHAR, 6, 6);
                } else {
                    return maskWithDefaultChar(value);
                }
            });
        }

        return mapMaskedValues;
    }

    public static String getTransactionType(Map<String,String> mapValues) {
        String processingCode = mapValues.get("processingCode");
        if (processingCode == null || processingCode.length() < 2) {
            return "";
        }
        return processingCode.substring(0, 2);
    }

    public static String getBinCode(Map<String,String> mapValues) {
        String primaryAccountNumber = mapValues.get("primaryAccountNumber");
        if (primaryAccountNumber == null || primaryAccountNumber.length() < 6) {
            return "";
        }
        return primaryAccountNumber.substring(0, 6);
    }

    private static  String maskWithDefaultChar(String value) {
        return value == null ? null : DEFAULT_MASK_CHAR.repeat(value.length());
    }

    /**
     * Obfuscate a string by replacing characters with a specified character
     *
     * @param value         The original string
     * @param direction     The direction to start obfuscation ("left" or "right")
     * @param character     The character to use for obfuscation
     * @param startPosition The position to start obfuscation
     * @param numberOfChars The number of characters to obfuscate
     * @return The obfuscated string
     */
    public static String obfuscate(String value, String direction, String character, int startPosition, int numberOfChars) {
        if (value == null) return "";

        character = character.substring(0, 1); // Only 1 character for obfuscation needed
        StringBuilder obfuscatedString = new StringBuilder(value);

        if (direction.equals("left")) {
            int start = Math.max(startPosition, 0);
            int end = Math.min(start + numberOfChars, obfuscatedString.length());
            obfuscatedString.replace(start, end, character.repeat(end - start));
        } else {
            int end = Math.max(obfuscatedString.length() - startPosition, 0);
            int start = Math.max(end - numberOfChars, 0);
            obfuscatedString.replace(start, end, character.repeat(end - start));
        }

        return obfuscatedString.toString();
    }

}
