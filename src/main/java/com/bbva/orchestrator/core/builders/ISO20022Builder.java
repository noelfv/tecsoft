package com.bbva.orchestrator.core.builders;

import com.bbva.gateway.dto.iso20022.ISO20022;
import com.bbva.gateway.utils.LogsTraces;
import com.bbva.orchestrator.core.mapper.factory.impl.DefaultDelegateMapper;
import com.bbva.orchestrator.core.mapper.model.CanonicalFields;
import com.bbva.orchestrator.core.operation.OperationHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Construye el objeto {@link ISO20022} a partir de un {@link OperationHandler}.
 *
 * <p>Actúa como thin adapter entre el modelo de operaciones y el
 * {@link DefaultDelegateMapper} (con sus 10 strategies de mapeo).
 * Envuelve el Map canónico del handler en un {@link CanonicalFields}
 * y delega al mapper — sin reconstruir ningún DTO de protocolo.
 */
@Component
@RequiredArgsConstructor
public class ISO20022Builder {

    private final DefaultDelegateMapper delegateMapper;

    /**
     * Construye el ISO20022 a partir del handler de la operación ya categorizada.
     *
     * @param operation handler con categoría y Map canónico de campos
     * @return objeto ISO20022 construido con las strategies de mapeo
     */
    public ISO20022 build(OperationHandler operation) {
        Map<String, String> fields = operation.enrichedFields();

        LogsTraces.writeInfo("ISO20022Builder [%s] messageType=%s transactionType=%s"
                .formatted(
                        operation.category(),
                        fields.get("messageType"),
                        fields.get("transactionType")
                ));

        return delegateMapper.mapper(CanonicalFields.of(fields));
    }
}
