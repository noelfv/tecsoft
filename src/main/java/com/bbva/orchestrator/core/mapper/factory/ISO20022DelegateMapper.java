package com.bbva.orchestrator.core.mapper.factory;

import com.bbva.gateway.dto.iso20022.ISO20022;
import com.bbva.orchestrator.core.dto.ISO8583;
import java.util.Map;

public interface ISO20022DelegateMapper {

    ISO20022 mapper(ISO8583 input, Map<String, String> subFields);

    Map<String,String> unMapper(ISO20022 input);
}
