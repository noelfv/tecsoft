package com.bbva.orchestrator.core.operation.impl;

import com.bbva.orchestrator.core.operation.OperationCategory;
import com.bbva.orchestrator.core.operation.OperationHandler;

import java.util.Collections;
import java.util.Map;

/**
 * Clase base para todas las implementaciones de {@link OperationHandler}.
 *
 * <p>Almacena la categoría y el Map canónico de campos de forma inmutable.
 * Las subclases concretas reciben el Map desde {@code OperationHandlerFactory}
 * via referencia a constructor (ej. {@code RetiroOperation::new}).
 */
public abstract class AbstractOperation implements OperationHandler {

    private final OperationCategory category;
    private final Map<String, String> fields;

    protected AbstractOperation(OperationCategory category, Map<String, String> fields) {
        this.category = category;
        this.fields = Collections.unmodifiableMap(fields);
    }

    @Override
    public OperationCategory category() {
        return category;
    }

    @Override
    public Map<String, String> fields() {
        return fields;
    }
}
