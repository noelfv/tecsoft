package com.bbva.orchestrator.core.transformer.impl;

import com.bbva.orchestrator.core.transformer.DelegateTransformer;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Transformer para mensajes XML de UPI PAY (billetera de India).
 *
 * <p><b>Estado:</b> stub — pendiente de especificación técnica de UPI PAY.
 *
 * <p>Al implementar, el Map resultante DEBE usar las mismas claves canónicas
 * que {@code ISO8583Transformer}, por ejemplo:
 * <pre>
 *   {@code <MTI>0100</MTI>}                          →  "messageType"      = "0100"
 *   {@code <TxnType>17</TxnType>}                    →  "transactionType"  = "17"
 *   {@code <Amount>100000</Amount>}                  →  "transactionAmount"= "100000"
 *   {@code <NetworkId>upipay</NetworkId>}            →  "networkName"      = "upipay"
 * </pre>
 */
@Component
public class XMLTransformer implements DelegateTransformer {

    @Override
    public String formatKey() {
        return "xml";
    }

    @Override
    public Map<String, String> toMap(String originalMessage) {
        // TODO: Parsear XML de UPI PAY y mapear campos a claves canónicas ISO8583
        // Las claves del Map deben coincidir con los nombres de campo del DTO ISO8583
        throw new UnsupportedOperationException("UPI PAY XML transformer — pendiente de especificación");
    }
}
