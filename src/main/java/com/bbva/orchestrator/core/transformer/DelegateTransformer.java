package com.bbva.orchestrator.core.transformer;

import java.util.Map;

/**
 * Convierte cualquier formato de mensaje entrante (ISO8583, XML, JSON) a un Map canónico.
 *
 * <p>Las claves del Map resultante son los nombres de campo del DTO {@code ISO8583},
 * tal como los define {@code ISO8583Builder}. Esto permite que el resto del pipeline
 * (OperationHandlerFactory, ISO20022Builder) opere de forma agnóstica al formato de origen.
 *
 * <p>Claves mínimas garantizadas por toda implementación:
 * <ul>
 *   <li>{@code "messageType"}     — MTI: 0100 | 0120 | 0400 | 0800</li>
 *   <li>{@code "transactionType"} — campo 03.01: 00 | 01 | 17 | 28 | ...</li>
 *   <li>{@code "networkName"}     — visa | mastercard | upipay</li>
 *   <li>{@code "originalMessage"} — mensaje original sin procesar</li>
 * </ul>
 */
public interface DelegateTransformer {

    /**
     * Clave de formato que identifica esta implementación.
     * Ejemplos: {@code "iso8583"}, {@code "xml"}, {@code "json"}.
     * Usada por {@link com.bbva.orchestrator.core.transformer.factory.TransformerFactory}
     * para construir el mapa de transformers al arrancar.
     */
    String formatKey();

    /**
     * Transforma el mensaje original al Map canónico de campos.
     *
     * @param originalMessage mensaje entrante en el formato nativo de la red
     * @return Map canónico con las claves del DTO ISO8583 + subcampos (03.01, 48.x, etc.)
     */
    Map<String, String> toMap(String originalMessage);
}
