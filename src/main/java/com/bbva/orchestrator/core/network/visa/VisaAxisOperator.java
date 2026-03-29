package com.bbva.orchestrator.core.network.visa;

import java.util.Map;
import java.util.Set;

public class VisaAxisOperator {

    private static final String VAR_6008 = "60.08";
    private static final String VAR_2201 = "22.01";
    private static final String VAR_6001 = "60.01";
    private static final String VAR_6002 = "60.02";
    private static final String VAR_0301 = "03.01";

    private static final Set<String> VALID_VALUES_6008 = Set.of("02","05","06","07","08");
    private static final Set<String> VALID_VALUES_TPV = Set.of("05","07","02","90","91","95");
    private static final Set<String> VALID_VALUES_6001 = Set.of("0","3","4","5");
    private static final Set<String> VALID_VALUES_6002 = Set.of("1","0","2","5","8");

    // TODO: Revisar implementacion especifica para VISA, momentaneamente se devuelve null y valores por defecto en duro.
    private VisaAxisOperator() {
    }
    public static Boolean channelECommerceIndicator(Map<String, String> subFields, String pointServiceConditionCode) {

        if (subFields==null || !subFields.containsKey(VAR_6008) || pointServiceConditionCode == null) {
            return false;
        }

        String val6008 = subFields.get(VAR_6008);

        if (val6008 == null) {
            return false;
        }

        return VALID_VALUES_6008.contains(val6008) && ("59".equals(pointServiceConditionCode) || "08".equals(pointServiceConditionCode));
    }

    public static String valueElectronicCommerceIndicators(Map<String, String> subFields) {
        return null;
    }

    public static String securityLevelECI(String ECI) {
        return null;
    }

    public static String channelTPVIndicator(Map<String, String> subFields, String merchantType){

        if (subFields == null) {
            return "UNSP";
        }

        String val2201 = subFields.get(VAR_2201);
        String val6001 = subFields.get(VAR_6001);
        String val6002 = subFields.get(VAR_6002);
        String val0301 = subFields.get(VAR_0301);

        boolean isPost = (val2201 != null && VALID_VALUES_TPV.contains(val2201)) &&
                         (val6001 != null && VALID_VALUES_6001.contains(val6001)) &&
                        (val6002 != null && VALID_VALUES_6002.contains(val6002));

        if (isPost) {
            return "POST";
        }

        if ("01".equals(val0301)) {
            if ("6011".equals(merchantType)) {
                return "ATMT";
            }
            if ("6010".equals(merchantType)) {
                return "OTHP";
            }
        }

        return "UNSP";
    }

    public static String entryModeIndicator(Map<String, String> subFields, String cardDataEntryMode) {
        return "ALL";
    }
}
