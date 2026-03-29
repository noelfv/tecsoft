package com.bbva.orchestrator.core.logic.process;

import com.bbva.orchestrator.core.dto.ISO8583;
import com.bbva.orchestrator.core.exception.ParserFieldsException;
import com.bbva.orchestrator.core.fields.MastercardISOField;
import com.bbva.orchestrator.core.parser.iso8583.handlers.impl.MastercardHandlerField;
import com.bbva.orchestrator.core.parser.iso8583.strategy.subfields.CompositeTlvFieldParser;
import com.bbva.orchestrator.core.fields.definitions.subfields.tlv.TLVFieldLoadStructure;
import com.bbva.orchestrator.core.utils.FieldUtil;
import com.bbva.orchestrator.core.commons.CommonsProcessSubField;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase para parsear los subcampos del Campo 48 (additionalDataRetailer) de mensajes ISO 8583 de Mastercard.
 * Esta clase procesa los subcampos de un mensaje ISO 8583 y los mapea a un mapa de valores.
 */
@Component
@RequiredArgsConstructor
public class MastercardProcessSubField  {

    private final CommonsProcessSubField commonsProcessSubField;
    private final MastercardHandlerField mastercardHandlerField;

    /**
     * Parsea los subcampos de campos variables específicos (como el Campo 48)
     * a partir de un mapa de campos principales ya parseados.
     *
     * @param iso8583 objeto iso8583 con los campos principales parseados,
     * donde el Campo 48 (si está presente) contiene su valor hexadecimal crudo.
     * @return Un mapa que contiene todos los subcampos y sub-subcampos parseados,
     * con claves como "48.01", "48.11.01", etc.
     * Si el Campo 48 no está presente, devuelve un mapa vacío.
     */
    public  Map<String, String> parseSubfields(ISO8583 iso8583) {
        Map<String, String> allParsedSubfields = new HashMap<>();

        if(!FieldUtil.requiredProcess(iso8583.getMessageType())){
            return allParsedSubfields;
        }

        allParsedSubfields = commonsProcessSubField.parseSubfields(iso8583);

        Map<String, String> mapSubField48 = processField48(iso8583);
        allParsedSubfields.putAll(mapSubField48);

        return allParsedSubfields;
    }

    /**
     * Parsea los subcampos de campos variables específicos (como el Campo 48)
     * a partir de un mapa de campos principales ya parseados.
     *
     * @param iso8583 Un mapa con los campos principales parseados,
     * donde el Campo 48 (si está presente) contiene su valor hexadecimal crudo.
     * @return Un mapa que contiene todos los subcampos y sub-subcampos parseados,
     * con claves como "48.01", "48.11.01", etc.
     * Si el Campo 48 no está presente, devuelve un mapa vacío.
     */
    private Map<String,String> processField48(ISO8583 iso8583){

        Map<String, String> mapField48 = new HashMap<>();
        // 1. Obtener la data hexadecimal cruda del Campo 48
        String field48RawHex = iso8583.getAdditionalDataRetailer();

        if (field48RawHex == null || field48RawHex.isEmpty()) {
            return mapField48;
        }

        try {
            CompositeTlvFieldParser field48Parser = new CompositeTlvFieldParser("48", TLVFieldLoadStructure.getDirectSubFieldDefinitionsForField48());
            Map<String, String> parsedInternalSubfields = field48Parser.parseToMap(field48RawHex, MastercardISOField.ADDITIONAL_DATA_48,mastercardHandlerField);
            mapField48.putAll(parsedInternalSubfields);

        } catch (RuntimeException e) {
            throw new ParserFieldsException("PGWP-00140","Error al procesar subcampos del Campo 48 :" +field48RawHex,e);
        }

        return mapField48;
    }
}