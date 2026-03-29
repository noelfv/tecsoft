package com.bbva.orchestrator.core.operation.impl;

import com.bbva.orchestrator.core.operation.OperationCategory;

import java.util.Map;

/**
 * Aviso de compra (MTI 0120, transactionType 00 / 18).
 * Relacionado directamente con {@link ComprasOperation} (MTI 0100).
 */
public class AvisoComprasOperation extends AbstractOperation {

    public AvisoComprasOperation(Map<String, String> fields) {
        super(OperationCategory.AVISO_COMPRAS, fields);
    }

    @Override
    public Map<String, String> enrichedFields() {
        // TODO: lógica específica de AVISO_COMPRAS
        return fields();
    }
}
