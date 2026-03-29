package com.bbva.orchestrator.core.logic.process;

import com.bbva.orchestrator.core.dto.ISO8583;
import com.bbva.orchestrator.core.parser.iso8583.strategy.subfields.CompositeVariableFieldParser;
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
public class VisaProcessSubField {

    private final CommonsProcessSubField commonsProcessSubField;
    private final CompositeVariableFieldParser compositeVariableFieldParser;

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
        Map<String, String> mapSubField60 = compositeVariableFieldParser.buildSubFieldsSpecific("60", iso8583.getPosTerminalData());
        allParsedSubfields.putAll(mapSubField60);

        //TODO: Validar de ser necesario el tratamiento del campo 48 para Visa
        //FIXME Si se agrega se debe de validar ya que el formatodel 48 de visa es diferente al de mastercard


        return allParsedSubfields;
    }

}