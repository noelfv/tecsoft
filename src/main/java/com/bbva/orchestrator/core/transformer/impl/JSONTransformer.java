package com.bbva.orchestrator.core.transformer.impl;

import com.bbva.orchestrator.core.transformer.DelegateTransformer;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Transformer para mensajes JSON de UPI PAY (billetera de India).
 *
 * <p><b>Estado:</b> stub — pendiente de especificación técnica de UPI PAY.
 *
 * <p>Al implementar, el Map resultante DEBE usar las mismas claves canónicas
 * que {@code ISO8583Transformer}, por ejemplo:
 * <pre>
 *   {@code {"mti": "0100"}}           →  "messageType"      = "0100"
 *   {@code {"txnType": "17"}}         →  "transactionType"  = "17"
 *   {@code {"amount": "100000"}}      →  "transactionAmount"= "100000"
 *   {@code {"network": "upipay"}}     →  "networkName"      = "upipay"
 * </pre>
 */
@Component
public class JSONTransformer implements DelegateTransformer {

    @Override
    public String formatKey() {
        return "json";
    }

    @Override
    public Map<String, String> toMap(String originalMessage) {
        // TODO: Parsear JSON de UPI PAY y mapear campos a claves canónicas ISO8583
        // Las claves del Map deben coincidir con los nombres de campo del DTO ISO8583
        throw new UnsupportedOperationException("UPI PAY JSON transformer — pendiente de especificación");
    }
}
