package com.bbva.orchestrator.core.operation.impl;

import com.bbva.orchestrator.core.operation.OperationCategory;

import java.util.Map;

/**
 * Operación de avance en efectivo (MTI 0100, transactionType 09).
 */
public class CashAdvancesOperation extends AbstractOperation {

    public CashAdvancesOperation(Map<String, String> fields) {
        super(OperationCategory.CASH_ADVANCES, fields);
    }

    @Override
    public Map<String, String> enrichedFields() {
        // TODO: lógica específica de CASH_ADVANCES
        return fields();
    }
}
