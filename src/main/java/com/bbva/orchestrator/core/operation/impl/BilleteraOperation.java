package com.bbva.orchestrator.core.operation.impl;

import com.bbva.orchestrator.core.operation.OperationCategory;

import java.util.Map;

/**
 * Operación de billetera / dinero móvil (MTI 0100, transactionType 17).
 * Cubre transacciones de DINERO MOVIL y pagos con billetera digital.
 */
public class BilleteraOperation extends AbstractOperation {

    public BilleteraOperation(Map<String, String> fields) {
        super(OperationCategory.BILLETERA, fields);
    }

    @Override
    public Map<String, String> enrichedFields() {
        // TODO: lógica específica de BILLETERA
        // Ej: subcampo 48.77 (transactionSubtype para dinero móvil),
        //     enriquecimiento con datos de UPI PAY si networkName = "upipay"
        return fields();
    }
}
