package com.bbva.orchestrator.core.logic.factory.impl;

import com.bbva.gateway.utils.LogsTraces;
import com.bbva.orchestrator.configuration.ApplicationDataLocalCache;
import com.bbva.orchestrator.core.dto.ISO8583;
import com.bbva.orchestrator.core.fields.MastercardISOField;
import com.bbva.orchestrator.core.logic.factory.NetworkDelegateFieldLogic;
import com.bbva.orchestrator.core.logic.process.MastercardProcessSubField;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class MastercardDelegateFieldLogic implements NetworkDelegateFieldLogic {

    private final ApplicationDataLocalCache applicationDataLocalCache;
    private final MastercardProcessSubField mastercardISOSubFieldParser;

    public MastercardDelegateFieldLogic(ApplicationDataLocalCache applicationDataLocalCache, MastercardProcessSubField mastercardISOSubFieldParser) {
        this.applicationDataLocalCache = applicationDataLocalCache;
        this.mastercardISOSubFieldParser = mastercardISOSubFieldParser;
    }

    @Override
    public Map<String, String> parseSubfields(ISO8583 iso8583) {
        return mastercardISOSubFieldParser.parseSubfields(iso8583);
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
            String fieldName = MastercardISOField.getById(fieldId).getName();

            // Comprueba si el campo existe en mapValues y lo añade al nuevo mapa.
            if (mapValues.containsKey(fieldName)) {
                mapValuesResponse.put(fieldName, mapValues.get(fieldName));
            }

            // Aquí puedes mantener tu lógica de validación.
            // Por ejemplo, si el campo es mandatorio y no se encontró.
            if ("M".equals(condition) && !mapValues.containsKey(fieldName)) {
                // Lanza una excepción o maneja el error como necesites.
                //throw new MandatoryFieldsException();
                //LogsTraces.writeError("Error: Campo mandatorio faltante: " + fieldName + " (ID: " + fieldId + ")");
                System.out.println("Error: Campo mandatorio faltante: " + fieldName + " (ID: " + fieldId + ")");
            }
        }

        mapValuesResponse.put("messageType",mapValues.get("messageType"));
        mapValuesResponse.put("additionalDataRetailer",applyLogicField48(messageType,mapValuesResponse));
        return mapValuesResponse;

    }

    private String applyLogicField48(String messageType, Map<String, String> mapValues) {
        String field48 = mapValues.get("additionalDataRetailer");

        StringBuilder result = new StringBuilder();


        if (messageType.contains("0110") && field48 != null) {

            Map<String, String> subFields48 = basicSubfieldGenerator48(field48);
            List<String> campos = Arrays.asList("01","33", "42", "43", "63", "92", "95");
            for (String campo : campos) {
                String subFieldValue = subFields48.get(campo);
                if (subFieldValue != null) {
                    result.append(subFieldValue);
                }
            }

            String field48Tag87 = mapValues.get("48.87");
            String field48Tag87D = mapValues.get("48.87_d");
            String field48Tag87_2 = mapValues.get("48.87_2");
            String field48Tag87I = mapValues.get("48.87_i");

            if ("true".equalsIgnoreCase(field48Tag87) || "true".equalsIgnoreCase(field48Tag87D) ||
                    "true".equalsIgnoreCase(field48Tag87_2) || "true".equalsIgnoreCase(field48Tag87I)) {
                result.append("F8F7F0F1D4");
            } else if ("false".equalsIgnoreCase(field48Tag87) || "false".equalsIgnoreCase(field48Tag87D) ||
                    "false".equalsIgnoreCase(field48Tag87_2) || "false".equalsIgnoreCase(field48Tag87I)) {
                result.append("F8F7F0F1D5");
            }

        }else{
            result.append(field48);
        }

        return result.toString();
    }

    private Map<String, String> basicSubfieldGenerator48(String input){
        Map<String, String> resultMap = new HashMap<>();

        if (input == null || input.length() < 2) {
            return resultMap; // Retorna mapa vacío si la entrada es inválida
        }

        // Los primeros 2 caracteres son el valor para la clave "01"
        String firstValue = input.substring(0, 2);
        resultMap.put("01", firstValue);

        int currentIndex = 2; // Empezamos a leer después del primer elemento

        while (currentIndex + 8 <= input.length()) { // 4 chars para key + 4 chars para

            // Los primeros 4 caracteres son la clave (ej. "F2F3")
            String keyBytes = input.substring(currentIndex, currentIndex + 4);
            currentIndex += 4;

            // Los siguientes 4 caracteres son la longitud (ej. "F0F2")
            String lengthBytes = input.substring(currentIndex, currentIndex + 4);
            currentIndex += 4;

            // Asumimos que FxFy se convierte en "xy"
            // Ej: "F2F3" -> "23"
            String mapKey = String.valueOf(keyBytes.charAt(1)) + keyBytes.charAt(3);

            // Asumimos que FxFy se convierte en el entero xy
            // Ej: "F0F2" -> "02" -> 2
            int dataPairs;
            try {
                String lengthStr = String.valueOf(lengthBytes.charAt(1)) + lengthBytes.charAt(3);
                dataPairs = Integer.parseInt(lengthStr);
            } catch (NumberFormatException e) {
                // Error si la longitud no es Fx decimal (ej. "E3F2")
                LogsTraces.writeError("Error: Formateador campo 48: Formato de longitud inválido " + lengthBytes);
                break; // Detener el procesamiento
            }

            // La longitud (dataPairs) indica cuántos pares de caracteres leer.
            // Longitud en caracteres = dataPairs * 2.
            int dataChars = dataPairs * 2;

            if (currentIndex + dataChars > input.length()) {
                // Si la cadena se corta y no hay suficientes datos
                LogsTraces.writeError("Error: Formateador campo 48 longitud de cadena superada");
                break; // Detener el procesamiento
            }

            // Extraer los datos
            String dataBytes = input.substring(currentIndex, currentIndex + dataChars);
            currentIndex += dataChars;

            // El valor es la concatenación de [Key] + [Length] + [Data]
            String mapValue = keyBytes + lengthBytes + dataBytes;

            resultMap.put(mapKey, mapValue);
        }

        return resultMap;

    }

}
