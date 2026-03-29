package com.bbva.orchestrator.core.mapper.iso20022.strategy;

import com.bbva.orchestrator.core.dto.ISO8583;
import java.util.Map;

public interface SectionMappingStrategy<T> {
    String DEFAULT_EMPTY_VALUE = "";

    T mapper(ISO8583 input, Map<String, String> subFields);

    Map<String,String> unMapper(String networkName,T input);
}
