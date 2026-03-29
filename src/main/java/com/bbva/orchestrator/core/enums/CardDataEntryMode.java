package com.bbva.orchestrator.core.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum CardDataEntryMode {

    // Define los valores raw y enums
    ENUM_00("00", "UNSP"),
    ENUM_01("01", "MLEY"),
    ENUM_02("02", "MGST"),
    ENUM_03("03", "OPTC"),
    ENUM_04("04", "OCRR"),
    ENUM_05("05", "ICCY"),
    ENUM_07("07", "ICPY"),
    ENUM_09("09", "OTHN"),
    ENUM_10("10", "DFLE"),
    ENUM_80("80", "MGST"),
    ENUM_81("81", "OTHN"),
    ENUM_82("82", "OTHN"),
    ENUM_90("90", "MGST"),
    ENUM_91("91", "MGST"),
    ENUM_95("95", "PEND"), // No definido en la tabla

    // Define los valores enums y raw
    RAW_UNSP_00("UNSP","00"),
    RAW_MLEY_01("MLEY","01"),
    RAW_MGST_02("MGST","02"),
    RAW_OPTC_03("OPTC","03"),
    RAW_OCRR_04("OCRR","04"),
    RAW_ICCY_05("ICCY","05"),
    RAW_ICPY_07("ICPY","07"),
    RAW_OTHN_09("OTHN","09"),
    RAW_DFLE_10("DFLE","10"),
    RAW_MGST_80("MGST","80"),
    RAW_OTHN_81("OTHN","81"),
    RAW_OTHN_82("OTHN","82"),
    RAW_MGST_90("MGST","90"),
    RAW_MGST_91("MGST","91"),
    RAW_PEND_95("PEND","95");

    private final String key;
    private final String value;

    CardDataEntryMode(String key, String value) {
        this.key = key;
        this.value = value;
    }

    // Método estático para buscar eficientemente por clave
    private static final Map<String, String> lookupMap = new HashMap<>();
    static {
        for (CardDataEntryMode mf : CardDataEntryMode.values()) {
            lookupMap.put(mf.getKey(), mf.getValue());
        }
    }

    public static String convertCardDataEntryMode(String typeMessage) {
        return lookupMap.get(typeMessage);
    }

    public static String convertTypeCardDataEntryMode(String cardDataEntryMode) {
        return lookupMap.get(cardDataEntryMode);
    }

}
