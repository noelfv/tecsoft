package com.bbva.orchestrator.core.mapper.factory;

import com.bbva.gateway.dto.iso20022.ISO20022;
import com.bbva.orchestrator.core.mapper.model.CanonicalFields;
import java.util.Map;

public interface ISO20022DelegateMapper {

    ISO20022 mapper(CanonicalFields fields);

    Map<String,String> unMapper(ISO20022 input);
}
