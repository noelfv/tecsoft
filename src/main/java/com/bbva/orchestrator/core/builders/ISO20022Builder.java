package com.bbva.orchestrator.core.builders;

import com.bbva.gateway.dto.iso20022.ISO20022;
import com.bbva.gateway.utils.LogsTraces;
import com.bbva.orchestrator.core.dto.ISO8583;
import com.bbva.orchestrator.core.mapper.factory.impl.DefaultDelegateMapper;
import com.bbva.orchestrator.core.operation.OperationHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Construye el objeto {@link ISO20022} a partir de un {@link OperationHandler}.
 *
 * <p>Actúa como thin adapter entre el nuevo modelo de operaciones y el
 * {@link DefaultDelegateMapper} existente (con sus 9 strategies de mapeo).
 * No duplica lógica de mapeo.
 *
 * <p>Flujo interno:
 * <ol>
 *   <li>Llama a {@link OperationHandler#enrichedFields()} para obtener el Map enriquecido</li>
 *   <li>Reconstruye el DTO {@code ISO8583} desde el Map canónico
 *       (para compatibilidad con las strategies existentes)</li>
 *   <li>Extrae los subcampos del Map (claves con formato {@code "NN.NN"} y especiales)</li>
 *   <li>Delega a {@link DefaultDelegateMapper#mapper(ISO8583, Map)}</li>
 * </ol>
 */
@Component
@RequiredArgsConstructor
public class ISO20022Builder {

    private final DefaultDelegateMapper delegateMapper;

    /**
     * Construye el ISO20022 a partir del handler de la operación ya categorizada.
     *
     * @param operation handler con categoría y Map canónico de campos
     * @return objeto ISO20022 construido con las strategies existentes
     */
    public ISO20022 build(OperationHandler operation) {
        Map<String, String> fields = operation.enrichedFields();

        LogsTraces.writeInfo("ISO20022Builder [%s] messageType=%s transactionType=%s"
                .formatted(
                        operation.category(),
                        fields.get("messageType"),
                        fields.get("transactionType")
                ));

        // Reconstruir DTO ISO8583 desde el Map canónico
        // Las strategies existentes (EnvironmentMappingStrategy, TransactionMappingStrategy, etc.)
        // esperan un objeto ISO8583, no un Map directo.
        ISO8583 iso8583 = ISO8583Builder.buildISO8583(
                fields.getOrDefault("originalMessage", ""),
                fields
        );

        // Separar los subcampos del Map canónico para pasarlos al mapper
        Map<String, String> subFields = extractSubFields(fields);

        return delegateMapper.mapper(iso8583, subFields);
    }

    /**
     * Extrae los subcampos del Map canónico.
     *
     * <p>Criterios de subcampo:
     * <ul>
     *   <li>Claves con formato {@code "NN.NN"} o {@code "NN.NNsufijo"}: {@code "03.01"}, {@code "48.42"}, {@code "61.04"}</li>
     *   <li>Claves especiales en MAYÚSCULAS que el mapper espera: {@code "ADDITIONAL_AMOUNT_DOUBLE"}, {@code "ADDITIONAL_ACCOUNT_TYPE"}, etc.</li>
     *   <li>Clave {@code "ECI"}</li>
     * </ul>
     */
    private Map<String, String> extractSubFields(Map<String, String> fields) {
        return fields.entrySet().stream()
                .filter(e -> isSubFieldKey(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private boolean isSubFieldKey(String key) {
        return key.matches("\\d{2}\\.\\d{2}.*")   // "03.01", "48.42", "61.04", "22.01"
                || key.startsWith("ADDITIONAL_")   // "ADDITIONAL_AMOUNT_DOUBLE", etc.
                || key.equals("ECI");
    }
}
