package com.bbva.orchestrator.core.operation;

import java.util.Map;

/**
 * Representa una operación de pago ya categorizada, lista para ser procesada.
 *
 * <p>Es un objeto de datos creado por request (NO un bean Spring singleton).
 * Encapsula el {@link OperationCategory} resuelto y el Map canónico de campos
 * producido por el {@code DelegateTransformer}.
 *
 * <p>El método {@link #enrichedFields()} es un hook de extensión: cada implementación
 * concreta puede sobrescribirlo para aplicar transformaciones de campos específicas de
 * la operación antes de que el {@code ISO20022Builder} construya el ISO20022.
 */
public interface OperationHandler {

    /** Categoría de operación resuelta (RETIRO, COMPRAS, ANULACION_RETIRO, etc.) */
    OperationCategory category();

    /** Map canónico de campos tal como lo produjo el transformer */
    Map<String, String> fields();

    /**
     * Map de campos enriquecido con lógica específica de la operación.
     * Por defecto devuelve {@link #fields()} sin modificación.
     * Sobrescribir en implementaciones concretas para:
     * <ul>
     *   <li>Validar campos obligatorios de la operación</li>
     *   <li>Calcular o normalizar valores derivados</li>
     *   <li>Enriquecer con datos adicionales (surcharge, puntos, etc.)</li>
     * </ul>
     */
    default Map<String, String> enrichedFields() {
        return fields();
    }
}
