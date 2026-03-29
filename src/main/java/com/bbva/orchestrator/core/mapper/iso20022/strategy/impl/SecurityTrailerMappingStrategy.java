package com.bbva.orchestrator.core.mapper.iso20022.strategy.impl;

import com.bbva.gateway.dto.iso20022.MacDataDTO;
import com.bbva.gateway.dto.iso20022.SecurityTrailerDTO;
import com.bbva.orchestrator.core.dto.ISO8583;
import com.bbva.orchestrator.core.exception.MapperFieldsException;
import com.bbva.orchestrator.core.mapper.iso20022.strategy.SectionMappingStrategy;
import com.bbva.orchestrator.core.utils.MapperUtil;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class SecurityTrailerMappingStrategy implements SectionMappingStrategy<SecurityTrailerDTO> {

    private final MapperUtil mapperUtil;

    public SecurityTrailerMappingStrategy(MapperUtil mapperUtil) {
        this.mapperUtil = mapperUtil;
    }

    @Override
    public SecurityTrailerDTO mapper(ISO8583 input, Map<String, String> subFields) {

        try {
            String secControlInformation = input.getSecurityControlInformation();

            if (secControlInformation == null || secControlInformation.isEmpty()) {
                return null; // Retorna null si no hay información de control de seguridad
            }

            MacDataDTO macData = MacDataDTO.builder()
                    // 53.1 Security Format Code / Security Type Code
                    .keyProtection(mapperUtil.isNullOrEmptySubstring(secControlInformation, 0, 2))
                    // 53.2 PIN Encryption Code
                    .algorithm(mapperUtil.isNullOrEmptySubstring(secControlInformation, 2, 4))
                    // 53.3 PIN Block Format Code
                    .derivedInformation(mapperUtil.isNullOrEmptySubstring(secControlInformation, 4, 6))
                    // 53.4 Key Index Number
                    .keyIndex(mapperUtil.isNullOrEmptySubstring(secControlInformation, 6, secControlInformation.length()))
                    .build();

            return SecurityTrailerDTO.builder()
                    .macData(macData)
                    .build();

        } catch (RuntimeException e) {
            // Manejo de excepciones, puedes lanzar una RuntimeException o una excepción personalizada
            throw new MapperFieldsException("PGWP-00121", "Error al mapear SecurityTrailerDTO desde ISO8583", e);
        }
    }

    @Override
    public Map<String, String> unMapper(String networkName,SecurityTrailerDTO input) {
        Map<String, String> mapValues = new HashMap<>();

        /*MacDataDTO macData = input.getMacData();

        String securityControlInfo = mapperUtil.getFieldValue(macData, MacDataDTO::getKeyProtection, DEFAULT_EMPTY_VALUE) +
                mapperUtil.getFieldValue(macData, MacDataDTO::getAlgorithm, DEFAULT_EMPTY_VALUE) +
                mapperUtil.getFieldValue(macData, MacDataDTO::getDerivedInformation, DEFAULT_EMPTY_VALUE) +
                mapperUtil.getFieldValue(macData, MacDataDTO::getKeyIndex, DEFAULT_EMPTY_VALUE);

        mapValues.put("securityControlInformation", securityControlInfo);

        return mapValues*/;
        return Map.of();
    }
}
