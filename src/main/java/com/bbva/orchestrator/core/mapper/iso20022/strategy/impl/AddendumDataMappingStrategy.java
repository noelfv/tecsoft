package com.bbva.orchestrator.core.mapper.iso20022.strategy.impl;

import com.bbva.gateway.dto.iso20022.*;
import com.bbva.orchestrator.core.exception.MapperFieldsException;
import com.bbva.orchestrator.core.mapper.iso20022.strategy.SectionMappingResponseStrategy;
import com.bbva.orchestrator.core.mapper.iso20022.strategy.SectionMappingStrategy;
import com.bbva.orchestrator.core.mapper.model.CanonicalFields;
import com.bbva.orchestrator.core.utils.MapperUtil;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class AddendumDataMappingStrategy implements SectionMappingStrategy<AddendumDataDTO> , SectionMappingResponseStrategy<AddendumDataDTO> {

    private final MapperUtil mapperUtil;

    public AddendumDataMappingStrategy(MapperUtil mapperUtil) {
        this.mapperUtil = mapperUtil;
    }

    @Override
    public AddendumDataDTO mapper(CanonicalFields fields) {

        try {
            List<AdditionalDataDTO> additionalDataList = new ArrayList<>();
            //TODO Se debe considerar la siguiente logica, para los mensaje de entrada se debe de crear los
            // los objetos addendumData y customData con la siguiente logica:
            // 1. AddendumData:    MGSTYPE, ISO8583_HOST, ISO8583
            // 2. CustomDataLocal: MGSTYPE, ISO8583, FLOWTYPE(para el caso de flowType siempre seria ASYNC)
            String response = "0000".equals(fields.getRejectFlag()) ? fields.getRejectFlag() : fields.getMessageType();
            additionalDataList.add(AdditionalDataDTO.builder()
                    .key("UNSP")
                    .value(response)
                    .build());

            additionalDataList.add(AdditionalDataDTO.builder()
                    .key("ISO8583_HOST")
                    .value(fields.getOriginalMessage())
                    .build());

            additionalDataList.add(AdditionalDataDTO.builder()
                    .key("ISO8583")
                    .value(mapperUtil.getBinDescription(fields.getNetworkName(), fields.getBinCode()))
                    .build());

            return AddendumDataDTO.builder()
                    .additionalData(additionalDataList)
                    .build();

        } catch (RuntimeException e) {
            // Manejo de excepciones, puedes lanzar una RuntimeException o una excepción personalizada
            throw new MapperFieldsException("PGWP-00121", "Error al mapear AddendumDataDTO desde ISO8583", e);
        }
    }

    @Override
    public AddendumDataDTO mapperResponse(CanonicalFields fields) {

        List<AdditionalDataDTO> additionalDataList = new ArrayList<>();

        additionalDataList.add(AdditionalDataDTO.builder()
                .key("ISO8583_HOST")
                .value(fields.getOriginalMessage())
                .build());

        additionalDataList.add(AdditionalDataDTO.builder()
                .key("ISO8583")
                .value(fields.getPlainTextPCI())
                .build());

        String response = "0000".equals(fields.getRejectFlag()) ? fields.getRejectFlag() : fields.getMessageType();
        additionalDataList.add(AdditionalDataDTO.builder()
                .key("UNSP")
                .value(response)
                .build());


        return AddendumDataDTO.builder()
                .additionalData(additionalDataList)
                .build();
    }

    @Override
    public Map<String, String> unMapper(String networkName,AddendumDataDTO input) {
        return Map.of();
    }


}