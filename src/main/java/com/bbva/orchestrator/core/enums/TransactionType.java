package com.bbva.orchestrator.core.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum TransactionType {

    COMPRAS("00","COMPRAS"),
    RETIRO_DE_CAJERO("01","RETIROS"),
    ANULACIONES("04","ANULACIONES"),
    CASH_ADVANCES("09","CASH ADVANCES"),
    CONSULTA_PUNTOS("16","CONSULTA PUNTOS"),
    DINERO_MOVIL("17","DINERO MOVIL"),
    COMPRAS_PUNTOS("18","COMPRAS PUNTOS"),
    PAGOS("28","PAGOS"),
    TRANSFERENCIAS("40","TRANSFERENCIAS"),
    RETIRO_CORRESPONSALES("41","RETIRO CORRESPONSALES"),
    MULTIPAGOS("50","MULTIPAGOS"),
    INICIALIZACION_LLAVES("92","INICIALIZACION LLAVES");

    private final String key;
    private final String value;


    TransactionType(String key, String value) {
        this.key = key;
        this.value = value;
    }

    private static final Map<String, String> lookupMap = new HashMap<>();

    static {
        for (TransactionType mf : TransactionType.values()) {
            lookupMap.put(mf.getKey(), mf.getValue());
        }
    }

    public static String getTransactionType(String key) {
        return lookupMap.get(key);
    }

}
