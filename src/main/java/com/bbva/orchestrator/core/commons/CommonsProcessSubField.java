package com.bbva.orchestrator.core.commons;

import com.bbva.orchestrator.core.dto.ISO8583;
import com.bbva.orchestrator.core.parser.iso8583.strategy.subfields.CompositeFixedFieldParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CommonsProcessSubField {

    private final CompositeFixedFieldParser compositeFieldParser;


    public Map<String, String> parseSubfields(ISO8583 iso8583) {
        Map<String, String> allParsedSubfields = new HashMap<>();

        //TODO: Agregar mas campos compuestos si es necesario 61, 22, 54
        Map<String, String> mapSubField03 = compositeFieldParser.buildSubFieldsSpecific("03", iso8583.getProcessingCode());
        Map<String, String> mapSubField22 = compositeFieldParser.buildSubFieldsSpecific("22", iso8583.getPointServiceEntryMode());
        Map<String, String> mapSubField54 = compositeFieldParser.buildSubFieldsSpecific("54", iso8583.getAdditionalAmounts());
        Map<String, String> mapSubField61 = compositeFieldParser.buildSubFieldsSpecific("61", iso8583.getPosCardIssuer());
        allParsedSubfields.putAll(mapSubField03);
        allParsedSubfields.putAll(mapSubField22);
        allParsedSubfields.putAll(mapSubField54);
        allParsedSubfields.putAll(mapSubField61);
        return allParsedSubfields;
    }
}
