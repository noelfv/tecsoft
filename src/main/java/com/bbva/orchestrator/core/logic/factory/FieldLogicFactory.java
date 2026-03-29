package com.bbva.orchestrator.core.logic.factory;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FieldLogicFactory {

    private static final Map<String, String> PEER_TO_NETWORK = Map.of(
            "peer01", "visa",
            "peer02", "mastercard"
    );
    private final Map<String, NetworkDelegateFieldLogic> delegateFieldLogic;

    /**
     * Inyección automática de todos los beans que implementan ISO8583DelegateParser
     * Clave: nombre del bean (visaDelegateParser, mastercardDelegateParser, etc.)
     */
    public FieldLogicFactory(List<NetworkDelegateFieldLogic> parserList) {
        this.delegateFieldLogic = parserList.stream()
                .collect(Collectors.toMap(
                        p -> p.getClass().getSimpleName().replace("DelegateFieldLogic", "").toLowerCase(),
                        p -> p
                ));
    }

    /**
     * Obtiene el parser adecuado según el peerId pasado como parameter (ej. peer01 → Visa)
     */
    public NetworkDelegateFieldLogic getDelegateFieldLogic(String peerId) {
        if (peerId == null || peerId.isEmpty()) {
            return getDefaultFieldLogic();
        }

        String network = PEER_TO_NETWORK.get(peerId.toLowerCase());
        return delegateFieldLogic.getOrDefault(network, getDefaultFieldLogic());
    }

    private NetworkDelegateFieldLogic getDefaultFieldLogic() {
        return delegateFieldLogic.getOrDefault("default",
                delegateFieldLogic.values().stream().findFirst().orElseThrow(
                        () -> new IllegalStateException("No hay ningún ISO8583DelegateParser disponible")
                ));
    }
}
