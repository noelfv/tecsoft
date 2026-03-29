package com.bbva.orchestrator.core.mapper.iso20022.strategy.impl;

import com.bbva.gateway.dto.iso20022.*;
import com.bbva.orchestrator.core.exception.MapperFieldsException;
import com.bbva.orchestrator.core.mapper.iso20022.strategy.SectionMappingStrategy;
import com.bbva.orchestrator.core.mapper.model.CanonicalFields;
import com.bbva.orchestrator.core.utils.MapperUtil;
import org.springframework.stereotype.Component;
import java.util.*;


@Component
public class ProtectedDataMappingStrategy implements SectionMappingStrategy<List<ProtectedDataDTO>> {

    private final MapperUtil mapperUtil;

    public ProtectedDataMappingStrategy(MapperUtil mapperUtil) {
        this.mapperUtil = mapperUtil;
    }

    @Override
    public List<ProtectedDataDTO> mapper(CanonicalFields fields) {
        List<ProtectedDataDTO> protectedDataList = new ArrayList<>();

        try {

            String cryptographicServiceMessage = fields.getCryptographicServiceMessage();
            if (cryptographicServiceMessage == null || cryptographicServiceMessage.isEmpty()) {
                return protectedDataList; // Retorna lista vacía si no hay datos
            }

            // Construcción del objeto ProtectedDataDTO
            KEKIdDTO kekId = KEKIdDTO.builder()
                    .keyId(cryptographicServiceMessage)
                    .build();

            KEKDTO kek = KEKDTO.builder()
                    .kekId(kekId)
                    .build();

            RecipientDTO recipient = RecipientDTO.builder()
                    .kek(kek)
                    .build();

            EnvelopedDataDTO envelopedData = EnvelopedDataDTO.builder()
                    .recipient(List.of(recipient))
                    .build();

            ProtectedDataDTO protectedData = ProtectedDataDTO.builder()
                    .envelopedData(envelopedData)
                    .build();

            protectedDataList.add(protectedData);

            return protectedDataList;
        } catch (RuntimeException e) {
            // Manejo de excepciones, puedes lanzar una RuntimeException o una excepción personalizada
            throw new MapperFieldsException("PGWP-00121", "Error al mapear ProtectedDataDTO desde ISO8583", e);
        }
    }

    @Override
    public Map<String, String> unMapper(String networkName,List<ProtectedDataDTO> input) {
        Map<String, String> mapValues = new HashMap<>();

        KEKIdDTO kekIdObject = findFirstKekId(input);
        mapValues.put("cryptographicServiceMessage", mapperUtil.getFieldValue(kekIdObject, KEKIdDTO::getKeyId, DEFAULT_EMPTY_VALUE));

        return mapValues;
    }

    private KEKIdDTO findFirstKekId(List<ProtectedDataDTO> protectedDataList) {
        if (protectedDataList == null) {
            return null; // Retorna null si el parámetro es null
        }
        return protectedDataList.stream()
                .filter(Objects::nonNull)
                .map(ProtectedDataDTO::getEnvelopedData)
                .filter(Objects::nonNull)
                .map(EnvelopedDataDTO::getRecipient)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .map(RecipientDTO::getKek)
                .filter(Objects::nonNull)
                .map(KEKDTO::getKekId)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

}