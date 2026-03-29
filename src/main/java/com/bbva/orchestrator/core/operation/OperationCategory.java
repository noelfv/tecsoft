package com.bbva.orchestrator.core.operation;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Categorías de operación de pago basadas en la especificación Visa ISO8583.
 *
 * <p>Cada valor del enum lleva un {@link Predicate} sobre el Map canónico de campos.
 * Esto permite que cada operación defina exactamente cuántos y cuáles campos necesita
 * para identificarse (1, 2, 3 o 4+ campos), sin estar limitada a una firma fija.
 *
 * <p><b>Regla de orden crítica:</b> los predicados más específicos (más condiciones AND)
 * deben declararse ANTES que los generales. {@code DEFAULT} siempre al final.
 * {@link #resolve(Map)} evalúa en orden de declaración y retorna el primer match.
 *
 * <p><b>Para agregar una operación nueva:</b>
 * <ol>
 *   <li>Agregar el valor al enum con su predicado (antes del general si es más específico)</li>
 *   <li>Crear la clase {@code XxxOperation extends AbstractOperation}</li>
 *   <li>Registrar en {@code OperationHandlerFactory.BUILDERS}</li>
 * </ol>
 */
public enum OperationCategory {

    // =========================================================================
    // MTI 0100 — Originales
    // Para variantes más específicas (ej: RETIRO con accountFrom="20" y 48.01="01")
    // declararlas ANTES de la categoría general correspondiente.
    // =========================================================================
    RETIRO(fields ->
            "0100".equals(fields.get("messageType")) &&
            Set.of("01", "41").contains(fields.get("transactionType"))
    ),
    COMPRAS(fields ->
            "0100".equals(fields.get("messageType")) &&
            Set.of("00", "18").contains(fields.get("transactionType"))
    ),
    BILLETERA(fields ->
            "0100".equals(fields.get("messageType")) &&
            "17".equals(fields.get("transactionType"))
    ),
    PAGOS(fields ->
            "0100".equals(fields.get("messageType")) &&
            Set.of("28", "50").contains(fields.get("transactionType"))
    ),
    TRANSFERENCIAS(fields ->
            "0100".equals(fields.get("messageType")) &&
            "40".equals(fields.get("transactionType"))
    ),
    CASH_ADVANCES(fields ->
            "0100".equals(fields.get("messageType")) &&
            "09".equals(fields.get("transactionType"))
    ),
    CONSULTAS(fields ->
            "0100".equals(fields.get("messageType")) &&
            "16".equals(fields.get("transactionType"))
    ),

    // =========================================================================
    // MTI 0400 — Anulaciones (relación directa con las originales 0100)
    // =========================================================================
    ANULACION_RETIRO(fields ->
            "0400".equals(fields.get("messageType")) &&
            Set.of("01", "41").contains(fields.get("transactionType"))
    ),
    ANULACION_COMPRAS(fields ->
            "0400".equals(fields.get("messageType")) &&
            Set.of("00", "18").contains(fields.get("transactionType"))
    ),
    ANULACION_BILLETERA(fields ->
            "0400".equals(fields.get("messageType")) &&
            "17".equals(fields.get("transactionType"))
    ),

    // =========================================================================
    // MTI 0120 — Avisos (relación directa con las originales 0100)
    // =========================================================================
    AVISO_RETIRO(fields ->
            "0120".equals(fields.get("messageType")) &&
            Set.of("01", "41").contains(fields.get("transactionType"))
    ),
    AVISO_COMPRAS(fields ->
            "0120".equals(fields.get("messageType")) &&
            Set.of("00", "18").contains(fields.get("transactionType"))
    ),
    AVISO_BILLETERA(fields ->
            "0120".equals(fields.get("messageType")) &&
            "17".equals(fields.get("transactionType"))
    ),

    // =========================================================================
    // MTI 0800 — Gestión de red (solo 1 parámetro: messageType)
    // =========================================================================
    NETWORK(fields ->
            "0800".equals(fields.get("messageType"))
    ),

    // =========================================================================
    // Fallback — siempre al final, siempre coincide (predicado = true)
    // =========================================================================
    DEFAULT(fields -> true);

    private final Predicate<Map<String, String>> matcher;

    OperationCategory(Predicate<Map<String, String>> matcher) {
        this.matcher = matcher;
    }

    /**
     * Evalúa los predicados en orden de declaración del enum y retorna
     * la primera categoría cuyo predicado coincide con el Map canónico.
     *
     * <p>El Map completo se pasa a cada predicado, permitiendo que cada uno
     * acceda a los campos que necesita: 1 campo (NETWORK), 2 campos (RETIRO),
     * o 4+ campos para variantes específicas (ver Javadoc de la clase).
     *
     * @param fields Map canónico producido por el {@code DelegateTransformer}
     * @return categoría que mejor describe la operación; nunca {@code null}
     */
    public static OperationCategory resolve(Map<String, String> fields) {
        return Arrays.stream(values())
                .filter(c -> c.matcher.test(fields))
                .findFirst()
                .orElse(DEFAULT); // inalcanzable — DEFAULT.matcher = true
    }

    /**
     * Clave en minúsculas del nombre del enum.
     * Usada como key en {@code OperationHandlerFactory.BUILDERS}.
     * Ejemplo: {@code ANULACION_RETIRO} → {@code "anulacion_retiro"}.
     */
    public String categoryKey() {
        return this.name().toLowerCase();
    }
}
