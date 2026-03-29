package com.bbva.orchestrator.core.operation.impl;

import com.bbva.orchestrator.core.operation.OperationCategory;

import java.util.Map;

/**
 * Anulación de compra (MTI 0400, transactionType 00 / 18).
 * Relacionada directamente con {@link ComprasOperation} (MTI 0100).
 */
public class AnulacionComprasOperation extends AbstractOperation {

    public AnulacionComprasOperation(Map<String, String> fields) {
        super(OperationCategory.ANULACION_COMPRAS, fields);
    }

    @Override
    public Map<String, String> enrichedFields() {
        // TODO: lógica específica de ANULACION_COMPRAS
        // Ej: validar campo 90 (originalDataElements),
        //     revertir puntos canjeados si transactionType original era "18"
        return fields();
    }
}
