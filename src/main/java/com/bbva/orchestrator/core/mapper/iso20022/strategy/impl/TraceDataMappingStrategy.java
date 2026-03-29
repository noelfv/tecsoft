package com.bbva.orchestrator.core.mapper.iso20022.strategy.impl;

import com.bbva.gateway.dto.iso20022.TraceDataDTO;
import com.bbva.gateway.interceptors.GrpcHeadersInfo;
import com.bbva.orchestrator.core.exception.MapperFieldsException;
import com.bbva.orchestrator.core.mapper.iso20022.strategy.SectionMappingStrategy;
import com.bbva.orchestrator.core.mapper.model.CanonicalFields;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TraceDataMappingStrategy implements SectionMappingStrategy<List<TraceDataDTO>> {


    @Override
    public List<TraceDataDTO> mapper(CanonicalFields fields) {

        try{
            // ======== FIELD 63 (MAPPED AS POS ADDITIONAL DATA) ========
            TraceDataDTO posAdditionalData = TraceDataDTO.builder()
                    .key("posAdditionalData")
                    .value(fields.getNetworkData())
                    .build();

            // ======== HEADER OF ISO ========
            TraceDataDTO originHeader = TraceDataDTO.builder()
                    .key("header")
                    .value(fields.getHeader())
                    .build();

            // ======== ID PURCHASE ========
            TraceDataDTO traceDataDTO = TraceDataDTO.builder()
                    .key("PAYMENT_ID")
                    .value(GrpcHeadersInfo.getTraceId())
                    .build();


            List<TraceDataDTO> traceDataList = new ArrayList<>();
            traceDataList.add(posAdditionalData);
            traceDataList.add(originHeader);
            traceDataList.add(traceDataDTO);

            return traceDataList;
        } catch (RuntimeException e) {
            // Manejo de excepciones, puedes lanzar una RuntimeException o una excepción personalizada
            throw new MapperFieldsException("PGWP-00121", "Error al mapear TraceDataDTO desde ISO8583", e);
        }
    }

    @Override
    public Map<String, String> unMapper(String networkName,List<TraceDataDTO> input) {
        Map<String, String> mapValues = new HashMap<>();
        // ======== FIELD 63 (MAPPED AS POS ADDITIONAL DATA) ========
        String posAdditionalData = findValueByKey(input, "posAdditionalData");
        // ======== HEADER OF ISO VISA ========
        String header = findValueByKey(input, "header");
        mapValues.put("networkData", posAdditionalData);
        mapValues.put("header", header);

        return mapValues;
    }



    private String findValueByKey(List<TraceDataDTO> input, String key) {
        if (input == null) {
            return null;
        }
        return input.stream()
                .filter(data -> key.equals(data.getKey()))
                .map(TraceDataDTO::getValue)
                .findFirst()
                .orElse("");
    }


}
