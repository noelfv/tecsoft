package com.bbva.orchestrator.core.operation.impl;

import com.bbva.orchestrator.core.operation.OperationCategory;

import java.util.Map;

/**
 * Operación de retiro de cajero (MTI 0100, transactionType 01 / 41).
 * Cubre retiro de cajero automático (01) y retiro por corresponsales (41).
 */
public class RetiroOperation extends AbstractOperation {

    public RetiroOperation(Map<String, String> fields) {
        super(OperationCategory.RETIRO, fields);
    }

    @Override
    public Map<String, String> enrichedFields() {
        // TODO: lógica específica de RETIRO
        // Ej: validar campos de terminal ATM (campo 60), calcular surcharge,
        //     diferenciar cajero propio vs foráneo por binCode
        return fields();
    }
}
