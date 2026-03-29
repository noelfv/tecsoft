package com.bbva.orchestrator.core.mapper.factory;

import com.bbva.gateway.interceptors.GrpcHeadersInfo;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class MapperFactory {

    private static final Map<String, String> PEER_TO_NETWORK = Map.of(
            "peer01", "visa",
            "peer02", "mastercard"
    );
    private final Map<String, ISO20022DelegateMapper> mappers;

    /**
     * Inyección automática de todos los beans que implementan ISO20022DelegateMapper
     * Clave: nombre del bean (VisaISO8583ToISO20022Mapper, MastercardISO8583ToISO20022Mapper, etc.)
     */
    public MapperFactory(List<ISO20022DelegateMapper> parserList) {
        this.mappers = parserList.stream()
                .collect(Collectors.toMap(
                        p -> p.getClass().getSimpleName().replace("DelegateMapper", "").toLowerCase(),
                        p -> p
                ));
    }

    /**
     * Obtiene el parser adecuado según el peerId pasado como parameter (ej. peer01 → Visa)
     */
    public ISO20022DelegateMapper getDelegateMapper(String peerId) {
        if (peerId == null || peerId.isEmpty()) {
            return getDefaultMapper();
        }

        String network = PEER_TO_NETWORK.get(peerId.toLowerCase());
        return mappers.getOrDefault(network, getDefaultMapper());
    }


    /**
     * Obtiene el parser adecuado según el peerId del contexto de GrpcHeadersInfo
     * Si no se encuentra, devuelve el parser por defecto
     */
    public ISO20022DelegateMapper getDelegateMapper() {
        String peerId= GrpcHeadersInfo.getNetwork();
        return getDelegateMapper(peerId);
    }


    private ISO20022DelegateMapper getDefaultMapper() {
        return mappers.getOrDefault("default",
                mappers.values().stream().findFirst().orElseThrow(
                        () -> new IllegalStateException("No hay ningún ISO20022DelegateMapper disponible")
                ));
    }










/*
    public MapperFactory(Map<String, ISO20022DelegateMapper> mapperMap) {
        this.mappers = new ConcurrentHashMap<>();
        mapperMap.forEach((beanName, mapper) -> {
            String simpleName = mapper.getClass().getSimpleName();
            // Extrae el nombre limpio: VisaISO8583ToISO20022Mapper → Visa
            String key = simpleName.replace("ISO20022DelegateMapper", "");
            mappers.put(key, mapper);
        });
    }*/

    /**
     * Obtiene el mapper adecuado según el peerId (ej. PEER01, PEER02)
     * @param peerId Identificador del peer (ej. PEER01)
     * @return Mapper específico o default si no se encuentra
     */
   /* public ISO20022DelegateMapper getDelegateMapper(String peerId) {
        if (peerId == null || peerId.isEmpty()) {
            return getDefaultMapper();
        }

        String network = PEER_TO_NETWORK.get(peerId.trim());
        if (network == null) {
            return getDefaultMapper();
        }

        // Normaliza: "Mastercard" → "Mastercard", pero el bean es "MasterCard..."
        String mapperKey = normalizeNetworkName(network);

        return mappers.getOrDefault(mapperKey, getDefaultMapper());
    }

    private String normalizeNetworkName(String network) {
        return switch (network.trim().toLowerCase()) {
            case "visa" -> "Visa";
            case "Mastercard", "mastercard" -> "MasterCard"; // Ajusta según el nombre real del bean
            default -> network.trim();
        };
    }

    private ISO20022DelegateMapper getDefaultMapper() {
        return mappers.getOrDefault("Default",
                mappers.values().stream().findFirst().orElse(null));
    }*/
}