package com.bbva.orchestrator.core.operation.impl;

import com.bbva.orchestrator.core.operation.OperationCategory;

import java.util.Map;

/**
 * Aviso de retiro de cajero (MTI 0120, transactionType 01 / 41).
 * Relacionado directamente con {@link RetiroOperation} (MTI 0100).
 */
public class AvisoRetiroOperation extends AbstractOperation {

    public AvisoRetiroOperation(Map<String, String> fields) {
        super(OperationCategory.AVISO_RETIRO, fields);
    }

    @Override
    public Map<String, String> enrichedFields() {
        // TODO: lógica específica de AVISO_RETIRO
        return fields();
    }
}
