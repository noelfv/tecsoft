package com.bbva.orchestrator.core.mapper.factory.impl;

import com.bbva.gateway.dto.iso20022.ISO20022;
import com.bbva.orchestrator.core.mapper.factory.ISO20022DelegateMapper;
import com.bbva.orchestrator.core.mapper.model.CanonicalFields;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class VisaDelegateMapper implements ISO20022DelegateMapper {

    private final DefaultDelegateMapper delegate;

    public VisaDelegateMapper(DefaultDelegateMapper delegate) {
        this.delegate = delegate;
    }

    @Override
    public ISO20022 mapper(CanonicalFields fields) {
        // Puedes personalizar el comportamiento para Visa
        // Ej: modificar ciertos campos, agregar reglas de negocio, etc.
        return delegate.mapper(fields);
    }

    @Override
    public Map<String, String> unMapper(ISO20022 input) {
        // Puedes personalizar el comportamiento para Visa
        return delegate.unMapper( input);
    }
}
