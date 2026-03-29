package com.bbva.orchestrator.core.operation.impl;

import com.bbva.orchestrator.core.operation.OperationCategory;

import java.util.Map;

/**
 * Operación de compra (MTI 0100, transactionType 00 / 18).
 * Cubre compras estándar (00) y compras con puntos (18).
 */
public class ComprasOperation extends AbstractOperation {

    public ComprasOperation(Map<String, String> fields) {
        super(OperationCategory.COMPRAS, fields);
    }

    @Override
    public Map<String, String> enrichedFields() {
        // TODO: lógica específica de COMPRAS
        // Ej: validación ECI para e-commerce (campos 48.42, 48.43),
        //     diferenciación compra estándar vs compra con puntos por transactionType "18"
        return fields();
    }
}
