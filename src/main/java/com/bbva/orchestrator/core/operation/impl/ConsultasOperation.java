package com.bbva.orchestrator.core.operation.impl;

import com.bbva.orchestrator.core.operation.OperationCategory;

import java.util.Map;

/**
 * Operación de consulta de puntos (MTI 0100, transactionType 16).
 */
public class ConsultasOperation extends AbstractOperation {

    public ConsultasOperation(Map<String, String> fields) {
        super(OperationCategory.CONSULTAS, fields);
    }

    @Override
    public Map<String, String> enrichedFields() {
        // TODO: lógica específica de CONSULTAS
        // Ej: enriquecimiento con saldo de puntos disponibles
        return fields();
    }
}
