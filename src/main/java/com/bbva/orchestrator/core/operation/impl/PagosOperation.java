package com.bbva.orchestrator.core.operation.impl;

import com.bbva.orchestrator.core.operation.OperationCategory;

import java.util.Map;

/**
 * Operación de pago (MTI 0100, transactionType 28 / 50).
 * Cubre pagos simples (28) y multipagos (50).
 */
public class PagosOperation extends AbstractOperation {

    public PagosOperation(Map<String, String> fields) {
        super(OperationCategory.PAGOS, fields);
    }

    @Override
    public Map<String, String> enrichedFields() {
        // TODO: lógica específica de PAGOS
        // Ej: para MULTIPAGOS (50) extraer datos adicionales del campo 60
        return fields();
    }
}
