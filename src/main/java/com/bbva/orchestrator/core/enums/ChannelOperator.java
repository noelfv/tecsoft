package com.bbva.orchestrator.core.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum ChannelOperator {

    ECOMMER("ECOMMER","ECOMMER"),
    TPV("POST","TPV"),
    ATMT("ATMT","ATM");

    private final String key;
    private final String value;


    ChannelOperator(String key, String value) {
        this.key = key;
        this.value = value;
    }

    private static final Map<String, String> lookupMap = new HashMap<>();

    static {
        for (ChannelOperator mf : ChannelOperator.values()) {
            lookupMap.put(mf.getKey(), mf.getValue());
        }
    }

    public static String getChannelOperator(String key) {
        return lookupMap.get(key);
    }

}
