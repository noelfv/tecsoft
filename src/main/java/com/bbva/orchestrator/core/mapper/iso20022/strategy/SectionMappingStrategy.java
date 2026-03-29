package com.bbva.orchestrator.core.mapper.iso20022.strategy;

import com.bbva.orchestrator.core.mapper.model.CanonicalFields;
import java.util.Map;

public interface SectionMappingStrategy<T> {
    String DEFAULT_EMPTY_VALUE = "";

    T mapper(CanonicalFields fields);

    Map<String,String> unMapper(String networkName,T input);
}
