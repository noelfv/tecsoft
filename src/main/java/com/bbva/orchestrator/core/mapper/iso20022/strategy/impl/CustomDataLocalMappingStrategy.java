package com.bbva.orchestrator.core.mapper.iso20022.strategy.impl;

import com.bbva.gateway.dto.iso20022.*;
import com.bbva.orchestrator.core.dto.ISO8583;
import com.bbva.orchestrator.core.exception.MapperFieldsException;
import com.bbva.orchestrator.core.mapper.iso20022.strategy.SectionMappingStrategy;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class CustomDataLocalMappingStrategy implements SectionMappingStrategy<CustomDataLocalDTO> {

    @Override
    public CustomDataLocalDTO mapper(ISO8583 input, Map<String, String> subFields) {

        try {
            //TODO Se debe considerar la siguiente logica, para los mensaje de entrada se debe de crear los
            // los objetos addendumData y customData con la siguiente logica:
            // 1. AddendumData:    MGSTYPE, ISO8583_HOST, ISO8583
            // 2. CustomDataLocal: MGSTYPE, ISO8583
            List<AdditionalDataCustomDataLocalDTO> additionalDataCustomDataLocalList = new ArrayList<>();

            // 1. Crear y añadir el primer par (MSGTYPE)
            RequestDTO msgTypeRequest = RequestDTO.builder()
                    .key("MSGTYPE")
                    .value(input.getMessageType())
                    .build();
            // 2. Crear y añadir el segundo par (ISO8583)
            RequestDTO iso8583Request = RequestDTO.builder()
                    .key("ISO8583")
                    .value(input.getPlainTextPCI())
                    .build();
            // 3. Guardar el campo 48 original
            RequestDTO iso8583AdditionalData_DE48 = RequestDTO.builder()
                    .key("DE_48")
                    .value(input.getAdditionalDataRetailer())
                    .build();

            additionalDataCustomDataLocalList.add(AdditionalDataCustomDataLocalDTO.builder()
                    .request(iso8583Request)
                    .build());

            additionalDataCustomDataLocalList.add(AdditionalDataCustomDataLocalDTO.builder()
                    .request(msgTypeRequest)
                    .build());

            additionalDataCustomDataLocalList.add(AdditionalDataCustomDataLocalDTO.builder()
                    .request(iso8583AdditionalData_DE48)
                    .build());

            // 4. Construir el DTO final con la lista poblada
            return CustomDataLocalDTO.builder()
                    .additionalData(additionalDataCustomDataLocalList)
                    .build();
        } catch (RuntimeException e) {
            // Manejo de excepciones, puedes lanzar una RuntimeException o una excepción personalizada
            throw new MapperFieldsException("PGWP-00121", "Error al mapear CustomDataLocalDTO desde ISO8583", e);
        }
    }

    @Override
    public Map<String, String> unMapper(String networkName,CustomDataLocalDTO input) {
        return Map.of();
    }
}