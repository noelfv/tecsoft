package com.bbva.orchestrator.core.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum FilterOperator {

    PURCHASE("00","PURCHASE"),
    WTHDMON("01","WTHDMON"),
    NA("02","NA"),
    REFUND("04","REFUND");

    private final String key;
    private final String value;


    FilterOperator(String key, String value) {
        this.key = key;
        this.value = value;
    }

    private static final Map<String, String> lookupMap = new HashMap<>();

    static {
        for (FilterOperator mf : FilterOperator.values()) {
            lookupMap.put(mf.getKey(), mf.getValue());
        }
    }

    public static String getFilterOperator(String key) {
        return lookupMap.get(key);
    }

}
