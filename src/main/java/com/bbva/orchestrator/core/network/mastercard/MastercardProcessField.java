package com.bbva.orchestrator.core.network.mastercard;

import com.bbva.gateway.utils.LogsTraces;
import com.bbva.orchestrator.core.parser.iso8583.handlers.impl.MastercardHandlerField;
import com.bbva.orchestrator.core.parser.iso8583.strategy.fields.PlainTextFieldParser;
import com.bbva.orchestrator.core.fields.MastercardISOField;
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
public class MastercardProcessField {

    private static final int LENGTH_BINARY_PRIMARY_BITMAP = 64;
    private static final int LENGTH_BINARY_SECONDARY_BITMAP = 128;

    private final PlainTextFieldParser plainTextFieldParserDecorator;
    private final MastercardHandlerField mastercardHandlerField;

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
            position = ParserUtil.processFieldData(MastercardISOField.MESSAGE_TYPE, isoMessage, position, valuesMap, mastercardHandlerField);
            position = ParserUtil.processFieldData(MastercardISOField.BITMAP_PRIMARY, isoMessage, position, valuesMap, mastercardHandlerField);

            String binaryBitMapPrimary = valuesMap.get(MastercardISOField.BITMAP_PRIMARY.getName());
            StringBuilder fullBitmap = new StringBuilder(binaryBitMapPrimary);

            if (binaryBitMapPrimary.charAt(0) == '1') {
                position = ParserUtil.processFieldData(MastercardISOField.BITMAP_SECONDARY, isoMessage, position, valuesMap, mastercardHandlerField);
                String binaryBitMapSecondary = valuesMap.get(MastercardISOField.BITMAP_SECONDARY.getName());
                fullBitmap.append(binaryBitMapSecondary);
                containsSecondaryBitmap = true;
            }

            for (int i = 2; i <= fullBitmap.length(); i++) {
                if (fullBitmap.charAt(i - 1) == '1') {
                    MastercardISOField field = MastercardISOField.getById(i);

                    if (field == null) {
                        LogsTraces.writeInfo("Campo no permitido: " + i + ". No hay mapeo disponible.");
                        throw new ParserException(ParserUtil.createMessageError(i));
                    }

                    position = ParserUtil.processFieldData(field, isoMessage, position, valuesMap,mastercardHandlerField);
                }
            }
        } catch (ParserException e) {
            throw e;
        } catch (Exception e) {
            throw new ParserException(FieldUtil.formatMessageException("[PGWP-00000]","No se puede parsear el mensaje ISO - "+FieldUtil.processError(originalMessageHex,"peer02", containsSecondaryBitmap),e.getCause()));
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

        boolean hasSecondaryBitmap = false;

        // Iterar todos los campos del 2 al 128
        for (int i = 2; i <= LENGTH_BINARY_SECONDARY_BITMAP; i++) {
            MastercardISOField field = MastercardISOField.getById(i);
            if (field == null) {
                binaryBitmap.append('0');
                continue;
            }

            String fieldName = field.getName();
            String value = mapValues.get(fieldName);

            if (value != null && !value.isEmpty()) {
                binaryBitmap.append('1'); // Campo presente

                // Usar la estrategia de unparse
                String isoValueHex = field.getParserStrategy().build(value, field,mastercardHandlerField);
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
        String messageTypeHex = ISOUtil.stringToEBCDICHex(messageType);

        // Construir trama final
        return messageTypeHex + bitmapHex + isoValues;
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
            MastercardISOField field = MastercardISOField.getById(i);
            if (field == null) {
                binaryBitmap.append('0');
                continue;
            }

            String fieldName = field.getName();
            String value = mapValues.get(fieldName);

            if (value != null && !value.isEmpty()) {
                binaryBitmap.append('1'); // Campo presente

                // Aquí se usa el PlainTextFieldParserDecorator
                String isoValue=plainTextFieldParserDecorator.build(value, field,mastercardHandlerField);
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

        // Construir trama final
        return messageType + bitmapHex + isoValues;
    }
}