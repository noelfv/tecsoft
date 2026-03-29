package com.bbva.orchestrator.core.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum ResultaDataType {

    APPR_0100("0100", "APPR"),
    APPR_0101("0101","APPR"),
    APPR_0110("0110", "APPR"),
    PRCS_0120("0120", "PRCS"),
    PRCS_0130("0130", "PRCS"),
    SUCC_0400("0400", "SUCC"),
    SUCC_0401("0401","SUCC"),
    SUCC_0410("0410", "SUCC"),
    PRCS_0420("0420", "PRCS"),
    PRCS_0430("0430", "PRCS"),
    UKNW_0302("0302", "UKNW"),
    UKNW_0312("0312", "UKNW");

    private final String key;
    private final String value;

    ResultaDataType(String key, String value) {
        this.key = key;
        this.value = value;
    }

    // Método estático para buscar eficientemente por clave
    private static final Map<String, String> lookupMap = new HashMap<>();
    static {
        for (ResultaDataType mf : ResultaDataType.values()) {
            lookupMap.put(mf.getKey(), mf.getValue());
        }
    }

    public static String convertResultDataType(String resultDataType) {
        return lookupMap.get(resultDataType);
    }

}
