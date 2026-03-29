package com.bbva.orchestrator.core.parser.factory.impl;

import com.bbva.orchestrator.core.parser.factory.ISO8583DelegateParser;
import com.bbva.orchestrator.core.network.visa.VisaProcessField;
import com.bbva.orchestrator.core.utils.ParserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class VisaDelegateParser implements ISO8583DelegateParser {

    private static final String NETWORK_VISA = "PEER01";
    private final VisaProcessField fieldParser;

    @Override
    public Map<String, String> parser(String originalMessage) {
        Map<String,String> mappedFields = fieldParser.mapFields(originalMessage);
        adjustFields(mappedFields);
        mappedFields.put("networkName",NETWORK_VISA);
        mappedFields.put("plainTextPCI", unParserPlainTextPCI(mappedFields));
        mappedFields.put("transactionType",ParserUtil.getTransactionType(mappedFields));
        mappedFields.put("binCode",ParserUtil.getBinCode(mappedFields));
        return mappedFields;
    }

    @Override
    public String unParser(Map<String, String> mappedFields) {
        return fieldParser.unMapFields(mappedFields);
    }

    @Override
    public String unParserPlainText(Map<String, String> mappedFields) {
        return fieldParser.unMapFieldsPlainText(mappedFields);
    }

    //@Override
    //public Map<String, String> parserSubFields(ISO8583 iso8583) {
      //  return subFieldParser.parseSubfields(iso8583);
    //}

    /**
     * Ajusta los campos específicos en el mapa de valores.
     * Este método procesa ciertos campos de moneda para extraer solo los tres caracteres relevantes.
     * Solo aplica para visa ya que para los numeric impares, visa adiciona un '0' al inicio.
     * @param mapValues Mapa de valores donde se ajustarán los campos específicos.
     */
    public void adjustFields(Map<String, String> mapValues) {
        // Lista de claves a procesar
        String[] currencyKeys = {
                "acquirerCountryCode",
                "primaryAccountNumberCountryCode",
                "forwardingInstitutionCountryCode",
                "cardSequenceNumber",
                "networkInternationalId",
                "transactionCurrencyCode",
                "settlementCurrencyCode",
                "cardholderBillingCurrencyCode",
                "receivingInstitutionCountryCode",
                "settlementInstitutionCountryCode",
                "networkManagementInformationCode"
        };

        for (String key : currencyKeys) {
            String value = mapValues.get(key);
            // Comprueba si el valor no es nulo y no está vacío antes de procesarlo
            if (value != null && !value.isEmpty()) {
                mapValues.put(key, value.substring(1, 4));
            }
        }
    }

    private String unParserPlainTextPCI(Map<String, String> mapFieldsValue) {

        if (mapFieldsValue.get("messageType").startsWith("08") || mapFieldsValue.get("messageType").startsWith("019")) {
            return mapFieldsValue.get("messageType");
        }
        try{
            Map<String, String> maskedFields = ParserUtil.maskSensitiveFields(mapFieldsValue);
            return fieldParser.unMapFieldsPlainText(maskedFields);
        }catch (Exception e){
            //SI HAY ERROR EN EL UNPARSER DEL PLAIN TEXT PCI, SE DEVUELVE SOLO EL MESSAGE TYPE
            return mapFieldsValue.get("messageType");
        }

    }
}