package com.bbva.orchestrator.core.operation.factory;

import com.bbva.gateway.utils.LogsTraces;
import com.bbva.orchestrator.core.operation.OperationCategory;
import com.bbva.orchestrator.core.operation.OperationHandler;
import com.bbva.orchestrator.core.operation.impl.*;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;

/**
 * Factory que crea el {@link OperationHandler} correcto en tiempo de ejecución.
 *
 * <p>Delega la resolución de categoría a {@link OperationCategory#resolve(Map)},
 * que evalúa predicados sobre el Map canónico completo (sin aridad fija).
 * Cada categoría puede requerir 1, 2, o 4+ campos para identificarse.
 *
 * <p>Los handlers NO son beans Spring — se crean como objetos de datos por request
 * usando referencias a constructores ({@code RetiroOperation::new}).
 * Esta factory SÍ es un bean Spring singleton.
 */
@Component
public class OperationHandlerFactory {

    private static final Map<String, Function<Map<String, String>, OperationHandler>> BUILDERS =
            Map.ofEntries(
                    // MTI 0100 — Originales
                    Map.entry(OperationCategory.RETIRO.categoryKey(),          RetiroOperation::new),
                    Map.entry(OperationCategory.COMPRAS.categoryKey(),         ComprasOperation::new),
                    Map.entry(OperationCategory.BILLETERA.categoryKey(),       BilleteraOperation::new),
                    Map.entry(OperationCategory.PAGOS.categoryKey(),           PagosOperation::new),
                    Map.entry(OperationCategory.TRANSFERENCIAS.categoryKey(),  TransferenciasOperation::new),
                    Map.entry(OperationCategory.CASH_ADVANCES.categoryKey(),   CashAdvancesOperation::new),
                    Map.entry(OperationCategory.CONSULTAS.categoryKey(),       ConsultasOperation::new),
                    // MTI 0400 — Anulaciones
                    Map.entry(OperationCategory.ANULACION_RETIRO.categoryKey(),    AnulacionRetiroOperation::new),
                    Map.entry(OperationCategory.ANULACION_COMPRAS.categoryKey(),   AnulacionComprasOperation::new),
                    Map.entry(OperationCategory.ANULACION_BILLETERA.categoryKey(), AnulacionBilleteraOperation::new),
                    // MTI 0120 — Avisos
                    Map.entry(OperationCategory.AVISO_RETIRO.categoryKey(),    AvisoRetiroOperation::new),
                    Map.entry(OperationCategory.AVISO_COMPRAS.categoryKey(),   AvisoComprasOperation::new),
                    Map.entry(OperationCategory.AVISO_BILLETERA.categoryKey(), AvisoBilleteraOperation::new),
                    // Red
                    Map.entry(OperationCategory.NETWORK.categoryKey(),  NetworkOperation::new),
                    // Fallback
                    Map.entry(OperationCategory.DEFAULT.categoryKey(),  DefaultOperation::new)
            );

    /**
     * Resuelve la categoría evaluando predicados sobre el Map canónico completo
     * y crea el {@link OperationHandler} concreto correspondiente.
     *
     * <p>Si la categoría resuelta es {@link OperationCategory#DEFAULT}, emite
     * una advertencia de observabilidad (PGWP-00130) para visibilidad en logs.
     *
     * @param fieldsValues Map canónico producido por el {@code DelegateTransformer}
     * @return handler de la operación, nunca {@code null}
     */
    public OperationHandler handle(Map<String, String> fieldsValues) {
        OperationCategory category = OperationCategory.resolve(fieldsValues);

        if (category == OperationCategory.DEFAULT) {
            LogsTraces.writeWarning("PGWP-00130 - Sin handler específico para messageType=[%s] transactionType=[%s]. Usando DEFAULT."
                    .formatted(fieldsValues.get("messageType"), fieldsValues.get("transactionType")));
        }

        return BUILDERS
                .getOrDefault(category.categoryKey(), DefaultOperation::new)
                .apply(fieldsValues);
    }
}
