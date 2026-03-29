package com.bbva.orchestrator.core.operation.impl;

import com.bbva.orchestrator.core.operation.OperationCategory;

import java.util.Map;

/**
 * Operación de gestión de red (MTI 0800).
 * Cubre inicialización de llaves (transactionType 92) y mensajes de red en general.
 */
public class NetworkOperation extends AbstractOperation {

    public NetworkOperation(Map<String, String> fields) {
        super(OperationCategory.NETWORK, fields);
    }

    @Override
    public Map<String, String> enrichedFields() {
        // TODO: lógica específica de NETWORK
        // Ej: manejo especial de campo 70 (networkManagementInformationCode)
        return fields();
    }
}
