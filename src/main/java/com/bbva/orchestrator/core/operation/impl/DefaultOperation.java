package com.bbva.orchestrator.core.operation.impl;

import com.bbva.orchestrator.core.operation.OperationCategory;

import java.util.Map;

/**
 * Operación fallback para combinaciones de messageType + transactionType no reconocidas.
 *
 * <p>El {@code ISO20022Builder} procesará la operación con el flujo estándar y
 * {@code OperationHandlerFactory} emitirá un log de advertencia (PGWP-00130)
 * cuando se use esta clase, para visibilidad en observabilidad.
 */
public class DefaultOperation extends AbstractOperation {

    public DefaultOperation(Map<String, String> fields) {
        super(OperationCategory.DEFAULT, fields);
    }
}
