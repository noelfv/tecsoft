package com.bbva.orchestrator.core.mapper.iso20022.strategy;

import com.bbva.orchestrator.core.dto.ISO8583;

public interface SectionMappingResponseStrategy<T> {

    T mapperResponse(ISO8583 iso8583);
}
