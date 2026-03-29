package com.bbva.orchestrator.core.mapper.iso20022.strategy;

import com.bbva.orchestrator.core.mapper.model.CanonicalFields;

public interface SectionMappingResponseStrategy<T> {

    T mapperResponse(CanonicalFields fields);
}
