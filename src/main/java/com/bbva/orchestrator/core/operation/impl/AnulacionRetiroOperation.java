package com.bbva.orchestrator.core.operation.impl;

import com.bbva.orchestrator.core.operation.OperationCategory;

import java.util.Map;

/**
 * Anulación de retiro de cajero (MTI 0400, transactionType 01 / 41).
 * Relacionada directamente con {@link RetiroOperation} (MTI 0100).
 */
public class AnulacionRetiroOperation extends AbstractOperation {

    public AnulacionRetiroOperation(Map<String, String> fields) {
        super(OperationCategory.ANULACION_RETIRO, fields);
    }

    @Override
    public Map<String, String> enrichedFields() {
        // TODO: lógica específica de ANULACION_RETIRO
        // Ej: validar campo 90 (originalDataElements) — referencia a la transacción original,
        //     verificar que el monto de anulación coincida con la transacción original
        return fields();
    }
}
