package com.bbva.orchestrator.core.network.visa;

import com.bbva.gateway.utils.LogsTraces;
import com.bbva.orchestrator.core.exception.ParserFieldsException;
import com.bbva.orchestrator.core.fields.VisaISOField;
import com.bbva.orchestrator.core.parser.iso8583.ParsedFieldResult;
import com.bbva.orchestrator.core.parser.iso8583.handlers.impl.VisaHandlerField;
import com.bbva.orchestrator.core.parser.iso8583.strategy.fields.PlainTextFieldParser;
import com.bbva.orchestrator.core.utils.FieldUtil;
import com.bbva.orchestrator.core.utils.ISOUtil;
import com.bbva.orchestrator.core.utils.ParserUtil;
import com.bbva.orchlib.parser.ParserException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase para parsear mensajes ISO 8583 de Mastercard.
 * Esta clase procesa los campos de un mensaje ISO 8583 y los mapea a un mapa de valores.
 */
@Component
@RequiredArgsConstructor
public class VisaProcessField {

    private static final int LENGTH_BINARY_PRIMARY_BITMAP = 64;
    private static final int LENGTH_BINARY_SECONDARY_BITMAP = 128;
    private static final String END_MESSAGE_VISA = "404040";

    private final PlainTextFieldParser plainTextFieldParserDecorator;
    private final VisaHandlerField visaFieldDefinition;


    /**
     * Mapea los campos de un mensaje ISO 8583 de Mastercard a un mapa de valores.
     *
     * @param originalMessageHex Mensaje ISO 8583 en formato hexadecimal.
     * @return Un mapa donde las claves son los nombres de los campos y los valores son sus respectivos valores.
     * @throws ParserException Si ocurre un error al procesar el mensaje ISO.
     */
    public  Map<String, String> mapFields(String originalMessageHex) {
        Map<String, String> valuesMap = new HashMap<>();
        StringBuilder isoMessage = new StringBuilder(originalMessageHex);
        int position = 0;
        boolean containsSecondaryBitmap = false;

        try {

            String header = processHeaderComplete(isoMessage, valuesMap);
            position += header.length();
            valuesMap.put(VisaISOField.HEADER.getName(), header);
            position = processFieldData(VisaISOField.MESSAGE_TYPE, isoMessage, position, valuesMap);
            position = processFieldData(VisaISOField.BITMAP_PRIMARY, isoMessage, position, valuesMap);

            String binaryBitMapPrimary = valuesMap.get(VisaISOField.BITMAP_PRIMARY.getName());
            StringBuilder fullBitmap = new StringBuilder(binaryBitMapPrimary);

            if (binaryBitMapPrimary.charAt(0) == '1') {
                position = processFieldData(VisaISOField.BITMAP_SECONDARY, isoMessage, position, valuesMap);
                String binaryBitMapSecondary = valuesMap.get(VisaISOField.BITMAP_SECONDARY.getName());
                fullBitmap.append(binaryBitMapSecondary);
                containsSecondaryBitmap = true;
            }

            for (int i = 2; i <= fullBitmap.length(); i++) {
                if (fullBitmap.charAt(i - 1) == '1') {
                    VisaISOField field = VisaISOField.getById(i);

                    if (field == null) {
                        LogsTraces.writeInfo("Campo no permitido: " + i + ". No hay mapeo disponible.");
                        throw new ParserException(ParserUtil.createMessageError(i));
                    }
                    position = processFieldData(field, isoMessage, position, valuesMap);
                }
            }
        } catch (ParserException e) {
            LogsTraces.writeError("parserError: " +FieldUtil.extractSegment(originalMessageHex,containsSecondaryBitmap));
            throw e;
        } catch (Exception e) {
            LogsTraces.writeError("messageError: " +FieldUtil.extractSegment(originalMessageHex,containsSecondaryBitmap));
            throw new ParserException(FieldUtil.formatMessageException("[PGWP-00000]","No se puede parsear el mensaje ISO - "+FieldUtil.processError(originalMessageHex,"peer01", containsSecondaryBitmap),e.getCause()));
        }
        return valuesMap;
    }

    public String unMapFields(Map<String, String> mapValues) {
        if (mapValues == null || mapValues.isEmpty()) {
            throw new ParserException("No se puede generar ISO8583: mapa vacío o nulo");
        }

        StringBuilder binaryBitmap = new StringBuilder();
        binaryBitmap.append('0'); // Bit 1 del bitmap primario (0 = sin bitmap secundario)
        StringBuilder isoValues = new StringBuilder();
        String header = mapValues.get(VisaISOField.HEADER.getName());

        boolean hasSecondaryBitmap = false;

        // Iterar todos los campos del 2 al 128
        for (int i = 2; i <= LENGTH_BINARY_SECONDARY_BITMAP; i++) {
            VisaISOField field = VisaISOField.getById(i);
            if (field == null) {
                binaryBitmap.append('0');
                continue;
            }

            String fieldName = field.getName();
            String value = mapValues.get(fieldName);

            if (value != null && !value.isEmpty()) {
                binaryBitmap.append('1'); // Campo presente

                // Usar la estrategia de unparse
                String isoValueHex = field.getParserStrategy().build(value, field,visaFieldDefinition);

                isoValues.append(isoValueHex);

                // Verificar si se necesita bitmap secundario
                if (i > LENGTH_BINARY_PRIMARY_BITMAP) {
                    hasSecondaryBitmap = true;
                }
            } else {
                binaryBitmap.append('0'); // Campo ausente
            }
        }

        // Ajustar bitmap: si hay campos del 65-128, activar el bit 1
        if (hasSecondaryBitmap) {
            binaryBitmap.setCharAt(0, '1');
        } else {
            binaryBitmap.setLength(64); // Solo bitmap primario
        }

        // Convertir bitmap a hex
        String bitmapHex = ISOUtil.convertBITMAPtoHEX(binaryBitmap.toString());

        // Obtener message type (en hex)
        String messageType = mapValues.get("messageType");

        // Construir trama final
        return header + messageType + bitmapHex + isoValues + END_MESSAGE_VISA;
    }

    private  int processFieldData(VisaISOField isoField, StringBuilder isoMessage, int currentPosition, Map<String, String> valuesMap) {
        try {

            String remainingMessageSegment = isoMessage.substring(currentPosition);
            ParsedFieldResult result;

            result = isoField.getParserStrategy().parse(remainingMessageSegment, isoField,visaFieldDefinition);

            valuesMap.put(isoField.getName(), result.value());

            currentPosition += result.consumedLengthInChars();

            return currentPosition;

        } catch (ParserFieldsException e) {
            throw new ParserException(FieldUtil.formatMessageException(e.getCode(),e.getDescription(),e));
        }
    }

    public String unMapFieldsPlainText(Map<String, String> mapValues) {
        if (mapValues == null || mapValues.isEmpty()) {
            throw new ParserException("No se puede generar ISO8583: mapa vacío o nulo");
        }

        StringBuilder binaryBitmap = new StringBuilder();
        binaryBitmap.append('0'); // Bit 1 del bitmap primario (0 = sin bitmap secundario)
        StringBuilder isoValues = new StringBuilder();

        boolean hasSecondaryBitmap = false;

        // Iterar todos los campos del 2 al 128
        for (int i = 2; i <= LENGTH_BINARY_SECONDARY_BITMAP; i++) {
            VisaISOField field = VisaISOField.getById(i);
            if (field == null) {
                binaryBitmap.append('0');
                continue;
            }

            String fieldName = field.getName();
            String value = mapValues.get(fieldName);

            if (value != null && !value.isEmpty()) {
                binaryBitmap.append('1'); // Campo presente

                // Aquí se usa el PlainTextFieldParserDecorator
                String isoValue=plainTextFieldParserDecorator.build(value, field,visaFieldDefinition);
                isoValues.append(isoValue);

                // Verificar si se necesita bitmap secundario
                if (i > LENGTH_BINARY_PRIMARY_BITMAP) {
                    hasSecondaryBitmap = true;
                }
            } else {
                binaryBitmap.append('0'); // Campo ausente
            }
        }

        // Ajustar bitmap: si hay campos del 65-128, activar el bit 1
        if (hasSecondaryBitmap) {
            binaryBitmap.setCharAt(0, '1');
        } else {
            binaryBitmap.setLength(64); // Solo bitmap primario
        }

        String messageType = mapValues.getOrDefault("messageType", "0000");
        String bitmapHex = ISOUtil.convertBITMAPtoHEX(binaryBitmap.toString());

        return messageType + bitmapHex + isoValues;
    }

    public String processHeaderComplete(StringBuilder isoMessage, Map<String, String> valuesMap) {

        if (isoMessage.charAt(0) != '1' || isoMessage.charAt(1) != 'A') {
            return processHeader(isoMessage.toString());
        }

        String isoString = isoMessage.toString();
        String firstPart = processHeader(isoString);
        valuesMap.put("rejectFlag", "0000");

        String remaining = isoString.substring(firstPart.length());
        String secondPart = processHeader(remaining);

        return new StringBuilder(secondPart.length() + firstPart.length())
                .append(secondPart)
                .append(firstPart)
                .toString();
    }

    public String processHeader(String isoMessage) {
        int start = 0;
        int length = VisaISOField.HEADER.getLength();
        String longHeader = isoMessage.substring(start, length);
        int dec = Integer.parseInt(longHeader, 16);
        length = dec * 2;

        return isoMessage.substring(0, length);
    }


}