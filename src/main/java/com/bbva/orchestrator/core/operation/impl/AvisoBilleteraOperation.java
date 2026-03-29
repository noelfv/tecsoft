package com.bbva.orchestrator.core.operation.impl;

import com.bbva.orchestrator.core.operation.OperationCategory;

import java.util.Map;

/**
 * Aviso de billetera / dinero móvil (MTI 0120, transactionType 17).
 * Relacionado directamente con {@link BilleteraOperation} (MTI 0100).
 */
public class AvisoBilleteraOperation extends AbstractOperation {

    public AvisoBilleteraOperation(Map<String, String> fields) {
        super(OperationCategory.AVISO_BILLETERA, fields);
    }

    @Override
    public Map<String, String> enrichedFields() {
        // TODO: lógica específica de AVISO_BILLETERA
        return fields();
    }
}
