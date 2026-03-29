package com.bbva.orchestrator.core.logic.factory.impl;

import com.bbva.orchestrator.configuration.ApplicationDataLocalCache;
import com.bbva.orchestrator.core.dto.ISO8583;
import com.bbva.orchestrator.core.fields.VisaISOField;
import com.bbva.orchestrator.core.logic.factory.NetworkDelegateFieldLogic;
import com.bbva.orchestrator.core.logic.process.VisaProcessSubField;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class VisaDelegateFieldLogic implements NetworkDelegateFieldLogic {

    private final ApplicationDataLocalCache applicationDataLocalCache;
    private final VisaProcessSubField visaProcessSubField;

    public VisaDelegateFieldLogic(ApplicationDataLocalCache applicationDataLocalCache, VisaProcessSubField visaProcessSubField) {
        this.applicationDataLocalCache = applicationDataLocalCache;
        this.visaProcessSubField = visaProcessSubField;
    }

    @Override
    public Map<String, String> parseSubfields(ISO8583 iso8583) {
        return visaProcessSubField.parseSubfields(iso8583);
    }


    @Override
    public Map<String, String> applyLogicFields(Map<String, String> mapValues) {
        String networkName= mapValues.get("networkName");
        String messageType= mapValues.get("messageType");
        Map<Integer,String> mapFieldsResponse = applicationDataLocalCache.getFieldsResponse(networkName,messageType);
        Map<String, String> mapValuesResponse = new HashMap<>();
        for (Map.Entry<Integer, String> entry : mapFieldsResponse.entrySet()) {
            Integer fieldId = entry.getKey();
            String condition = entry.getValue();
            String fieldName = VisaISOField.getById(fieldId).getName();

            // Comprueba si el campo existe en mapValues y lo añade al nuevo mapa.
            if (mapValues.containsKey(fieldName)) {
                mapValuesResponse.put(fieldName, mapValues.get(fieldName));
            }

            // Aquí puedes mantener tu lógica de validación.
            // Por ejemplo, si el campo es mandatorio y no se encontró.
            if ("M".equals(condition) && !mapValues.containsKey(fieldName)) {
                // Lanza una excepción o maneja el error como necesites.
                System.out.println("Error: Campo mandatorio faltante: " + fieldName + " (ID: " + fieldId + ")");
            }
        }

        mapValuesResponse.put("header",mapValues.get("header"));
        mapValuesResponse.put("messageType",mapValues.get("messageType"));
        return mapValuesResponse;
    }

}