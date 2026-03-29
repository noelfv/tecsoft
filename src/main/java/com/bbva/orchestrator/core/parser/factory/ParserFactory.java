package com.bbva.orchestrator.core.parser.factory;

import com.bbva.gateway.interceptors.GrpcHeadersInfo;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ParserFactory {

    private static final Map<String, String> PEER_TO_NETWORK = Map.of(
            "peer01", "visa",
            "peer02", "mastercard"
    );
    private final Map<String, ISO8583DelegateParser> parsers;

    /**
     * Inyección automática de todos los beans que implementan ISO8583DelegateParser
     * Clave: nombre del bean (visaDelegateParser, mastercardDelegateParser, etc.)
     */
    public ParserFactory(List<ISO8583DelegateParser> parserList) {
        this.parsers = parserList.stream()
                .collect(Collectors.toMap(
                        p -> p.getClass().getSimpleName().replace("DelegateParser", "").toLowerCase(),
                        p -> p
                ));
    }

    /**
     * Obtiene el parser adecuado según el peerId pasado como parameter (ej. peer01 → Visa)
     */
    public ISO8583DelegateParser getDelegateParser(String peerId) {
        if (peerId == null || peerId.isEmpty()) {
            return getDefaultParser();
        }

        String network = PEER_TO_NETWORK.get(peerId.toLowerCase());
        return parsers.getOrDefault(network, getDefaultParser());
    }


    /**
     * Obtiene el parser adecuado según el peerId del contexto de GrpcHeadersInfo
     * Si no se encuentra, devuelve el parser por defecto
     */
    public ISO8583DelegateParser getDelegateParser() {
        String peerId= GrpcHeadersInfo.getNetwork();
        return getDelegateParser(peerId);
    }


    private ISO8583DelegateParser getDefaultParser() {
        return parsers.getOrDefault("default",
                parsers.values().stream().findFirst().orElseThrow(
                        () -> new IllegalStateException("No hay ningún ISO8583DelegateParser disponible")
                ));
    }
}
