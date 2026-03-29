package com.bbva.orchestrator.core.operation.impl;

import com.bbva.orchestrator.core.operation.OperationCategory;

import java.util.Map;

/**
 * Anulación de billetera / dinero móvil (MTI 0400, transactionType 17).
 * Relacionada directamente con {@link BilleteraOperation} (MTI 0100).
 */
public class AnulacionBilleteraOperation extends AbstractOperation {

    public AnulacionBilleteraOperation(Map<String, String> fields) {
        super(OperationCategory.ANULACION_BILLETERA, fields);
    }

    @Override
    public Map<String, String> enrichedFields() {
        // TODO: lógica específica de ANULACION_BILLETERA
        // Ej: validar campo 90 (originalDataElements), revertir transacción de billetera
        return fields();
    }
}
