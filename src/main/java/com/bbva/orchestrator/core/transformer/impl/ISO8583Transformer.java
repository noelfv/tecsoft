package com.bbva.orchestrator.core.transformer.impl;

import com.bbva.gateway.interceptors.GrpcHeadersInfo;
import com.bbva.orchestrator.core.builders.ISO8583Builder;
import com.bbva.orchestrator.core.dto.ISO8583;
import com.bbva.orchestrator.core.logic.factory.FieldLogicFactory;
import com.bbva.orchestrator.core.logic.factory.NetworkDelegateFieldLogic;
import com.bbva.orchestrator.core.parser.factory.ISO8583DelegateParser;
import com.bbva.orchestrator.core.parser.factory.ParserFactory;
import com.bbva.orchestrator.core.transformer.DelegateTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Transformer para mensajes ISO8583 (Visa y Mastercard).
 *
 * <p>Absorbe el pipeline que anteriormente vivía en {@code OrchestratorFlowProcess}:
 * <ol>
 *   <li>Parseo del mensaje binario/hex → Map de campos ISO8583</li>
 *   <li>Construcción del DTO {@code ISO8583}</li>
 *   <li>Parseo de subcampos (campos 3, 22, 48, 60, 61, etc.)</li>
 *   <li>Serialización del DTO a Map canónico vía {@code ISO8583Builder.buildMapISO8583()}</li>
 * </ol>
 *
 * <p>El Map resultante incluye todas las claves del DTO ISO8583 más los subcampos
 * con notación {@code "NN.NN"} (ej. {@code "03.01"}, {@code "48.42"}, {@code "61.04"}).
 */
@Component
@RequiredArgsConstructor
public class ISO8583Transformer implements DelegateTransformer {

    private final ParserFactory parserFactory;
    private final FieldLogicFactory fieldLogicFactory;

    @Override
    public String formatKey() {
        return "iso8583";
    }

    @Override
    public Map<String, String> toMap(String originalMessage) {
        // 1. Parseo del mensaje binario/hex → Map de campos ISO8583
        ISO8583DelegateParser delegateParser = parserFactory.getDelegateParser(GrpcHeadersInfo.getNetwork());
        Map<String, String> fieldsValues = delegateParser.parser(originalMessage);

        // 2. Construcción del DTO ISO8583
        ISO8583 iso8583 = ISO8583Builder.buildISO8583(originalMessage, fieldsValues);

        // 3. Parseo de subcampos (campo 3.01/02/03, campo 48, campo 22, campo 60, campo 61, etc.)
        NetworkDelegateFieldLogic delegateFieldLogic = fieldLogicFactory.getDelegateFieldLogic(iso8583.getNetworkName());
        Map<String, String> subFieldsValues = delegateFieldLogic.parseSubfields(iso8583);

        // 4. Map canónico = campos ISO8583 + metadatos adicionales + subcampos
        Map<String, String> canonicalMap = new HashMap<>(ISO8583Builder.buildMapISO8583(iso8583));
        canonicalMap.put("originalMessage", originalMessage);
        canonicalMap.put("networkName",     iso8583.getNetworkName());
        canonicalMap.put("binCode",         iso8583.getBinCode());
        canonicalMap.put("transactionType", iso8583.getTransactionType()); // ya resuelto desde 03.01
        canonicalMap.put("plainTextPCI",    iso8583.getPlainTextPCI());
        canonicalMap.putAll(subFieldsValues); // "03.01","03.02","03.03","48.01","61.04", etc.

        return canonicalMap;
    }
}
