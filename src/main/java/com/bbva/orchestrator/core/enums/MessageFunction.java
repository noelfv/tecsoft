package com.bbva.orchestrator.core.enums;

import lombok.Getter;
import java.util.HashMap;
import java.util.Map;

// Nuevo enum para gestionar las funciones de mensaje
@Getter
public enum MessageFunction {

    // Define los MTI y sus valores de función
    MTI_0100_TO_FUNC("0100", "AUTQ"),
    MTI_0101_TO_FUNC("0101", "AUTQ"),
    MTI_0120_TO_FUNC("0120", "FAUQ"),
    MTI_0400_TO_FUNC("0400", "RVRA"),
    MTI_0401_TO_FUNC("0401", "RVRA"),
    MTI_0420_TO_FUNC("0420", "FRVA"),
    MTI_0800_TO_FUNC("0800", "CMPV"),
    MTI_0190_TO_FUNC("0190", "UNKW"),
    MTI_0302_TO_FUNC("0302", "TEXC"),
    // Define los valores de función y sus MTI de respuesta
    FUNC_AUTQ_TO_MTI("AUTQ", "0110"),
    FUNC_FAUQ_TO_MTI("FAUQ", "0130"),
    FUNC_RVRA_TO_MTI("RVRA", "0410"),
    FUNC_FRVA_TO_MTI("FRVA", "0430"),
    FUNC_CMPV_TO_MTI("CMPV", "0810"),
    FUNC_0312_TO_MTI("TEXC", "0312");

    private final String key;
    private final String value;

    MessageFunction(String key, String value) {
        this.key = key;
        this.value = value;
    }

    // Método estático para buscar eficientemente por clave
    private static final Map<String, String> lookupMap = new HashMap<>();
    static {
        for (MessageFunction mf : MessageFunction.values()) {
            lookupMap.put(mf.getKey(), mf.getValue());
        }
    }

    public static String convertMessageFunction(String typeMessage) {
        return lookupMap.get(typeMessage);
    }

    public static String convertTypeMessageResponse(String messageFunction) {
        return lookupMap.get(messageFunction);
    }

}