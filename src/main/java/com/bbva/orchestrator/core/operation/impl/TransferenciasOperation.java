package com.bbva.orchestrator.core.operation.impl;

import com.bbva.orchestrator.core.operation.OperationCategory;

import java.util.Map;

/**
 * Operación de transferencia (MTI 0100, transactionType 40).
 */
public class TransferenciasOperation extends AbstractOperation {

    public TransferenciasOperation(Map<String, String> fields) {
        super(OperationCategory.TRANSFERENCIAS, fields);
    }

    @Override
    public Map<String, String> enrichedFields() {
        // TODO: lógica específica de TRANSFERENCIAS
        // Ej: validar cuentas origen/destino (campos 102/103), tipo de cuenta (03.02/03.03)
        return fields();
    }
}
