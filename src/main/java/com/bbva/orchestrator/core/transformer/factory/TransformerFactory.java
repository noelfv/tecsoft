package com.bbva.orchestrator.core.transformer.factory;

import com.bbva.orchestrator.core.transformer.DelegateTransformer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Factory que selecciona el {@link DelegateTransformer} adecuado según el
 * peerId de red.
 *
 * <p>
 * Sigue el mismo patrón de {@code ParserFactory}: recibe
 * {@code List<DelegateTransformer>}
 * por inyección de Spring y construye el mapa de lookup al arrancar.
 *
 * <p>
 * Mapa de peers a formato:
 * <ul>
 * <li>peer01 → iso8583 (Visa)</li>
 * <li>peer02 → iso8583 (Mastercard)</li>
 * <li>peer03 → json (UPI PAY JSON)</li>
 * </ul>
 */
@Component
public class TransformerFactory {

        private static final Map<String, String> PEER_TO_FORMAT = Map.of(
                        "peer01", "iso8583",
                        "peer02", "iso8583",
                        "peer03", "json");

        private final Map<String, DelegateTransformer> transformers;

        public TransformerFactory(List<DelegateTransformer> transformerList) {
                this.transformers = transformerList.stream()
                                .collect(Collectors.toUnmodifiableMap(
                                                DelegateTransformer::formatKey,
                                                t -> t));
        }

        /**
         * Obtiene el transformer para el peerId dado.
         * Si el peerId no está registrado, devuelve el transformer ISO8583 por defecto.
         *
         * @param peerId identificador de red (ej. "peer01", "peer03")
         * @return transformer apropiado para el formato de esa red
         */
        public DelegateTransformer getDelegateTransformer(String peerId) {
                String format = PEER_TO_FORMAT.getOrDefault(
                                peerId != null ? peerId.toLowerCase() : "",
                                "iso8583");
                return transformers.getOrDefault(format, getDefault());
        }

        private DelegateTransformer getDefault() {
                return transformers.getOrDefault("iso8583",
                                transformers.values().stream().findFirst()
                                                .orElseThrow(() -> new IllegalStateException(
                                                                "No hay DelegateTransformer disponible")));
        }
}
