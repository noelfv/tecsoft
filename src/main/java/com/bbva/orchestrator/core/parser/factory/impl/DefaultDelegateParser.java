package com.bbva.orchestrator.core.parser.factory.impl;

import com.bbva.gateway.interceptors.GrpcHeadersInfo;
import com.bbva.orchestrator.core.dto.ISO8583;
import com.bbva.orchestrator.core.parser.factory.ISO8583DelegateParser;
import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * Implementación por defecto de ISO8583DelegateParser que lanza una excepción
 * para indicar que la red no es soportada.
 */
@Component
public class DefaultDelegateParser implements ISO8583DelegateParser {

    @Override
    public Map<String, String> parser(String originalMessage) {
        throw new UnsupportedOperationException("RED NO SOPORTADA : " + GrpcHeadersInfo.getNetwork());
    }

    @Override
    public String unParser(Map<String, String> mappedFields) {
        throw new UnsupportedOperationException("RED NO SOPORTADA : " + GrpcHeadersInfo.getNetwork());
    }

    @Override
    public String unParserPlainText(Map<String, String> mappedFields) {
        throw new UnsupportedOperationException("RED NO SOPORTADA : " + GrpcHeadersInfo.getNetwork());
    }
/*
    @Override
    public Map<String, String> parserSubFields(ISO8583 iso8583) {
        throw new UnsupportedOperationException("RED NO SOPORTADA : " + GrpcHeadersInfo.getNetwork());
    }
*/
}
