package com.bbva.orchestrator.core.network.mastercard;

import java.util.Map;
import java.util.Set;

public class MastercardAxisOperator {

    private static final String VAR_0301 = "03.01";
    private static final String VAR_2201 = "22.01";
    private static final String VAR_6111 = "61.11";
    private static final String VAR_4842 = "48.42";
    private static final String VAR_6104 = "61.04";
    private static final String VAR_6105 = "61.05";
    private static final String VAR_6110 = "61.10";
    private static final String VAR_4801 = "48.01";
    private static final Set<String> VALID_VALUES = Set.of("2","3","4","5","7","8","9");

    private static final String RETAIL = "R";
    private static final String TCC = "T";

    private MastercardAxisOperator() {
    }

    public static Boolean channelECommerceIndicator(Map<String, String> subFields) {

        if (!subFields.containsKey(VAR_4801) || !subFields.containsKey(VAR_2201) || !subFields.containsKey(VAR_4842) ||
                !subFields.containsKey(VAR_6104) || !subFields.containsKey(VAR_6105) || !subFields.containsKey(VAR_6110)) {
            return false;
        }

        return (
                        subFields.containsKey(VAR_4801) && subFields.containsKey(VAR_2201) && subFields.containsKey(VAR_4842) &&
                        TCC.equals(subFields.get(VAR_4801)) &&
                        (subFields.get(VAR_2201).equals("10") || subFields.get(VAR_2201).equals("01") || subFields.get(VAR_2201).equals("81")) &&
                        subFields.get(VAR_6104).equals("4") &&
                        subFields.get(VAR_6105).equals("1") &&
                        subFields.get(VAR_6110).equals("6")
                )
                ||
                (
                        subFields.containsKey(VAR_4801) && subFields.containsKey(VAR_2201) && subFields.containsKey(VAR_4842) &&
                        TCC.equals(subFields.get(VAR_4801)) &&
                        (subFields.get(VAR_2201).equals("81") || subFields.get(VAR_2201).equals("01") || subFields.get(VAR_2201).equals("10")) &&
                        subFields.get(VAR_6104).equals("5") &&
                        subFields.get(VAR_6105).equals("1") &&
                        subFields.get(VAR_6110).equals("6")
                );
    }

    public static String valueElectronicCommerceIndicators(Map<String, String> subFields) {
        if (subFields == null || subFields.isEmpty()) {
            return null;
        }

        if (!subFields.containsKey(VAR_4801) || !subFields.containsKey(VAR_2201) ||
                !subFields.containsKey(VAR_6110) || !subFields.containsKey(VAR_4842)){
            return null;
        }

        boolean condition1 = "T".equals(subFields.get(VAR_4801));
        boolean condition2 = "81".equals(subFields.get(VAR_2201));
        boolean condition3 = "6".equals(subFields.get(VAR_6110));

        if (condition1 && condition2 && condition3) {
            String valueToCut = subFields.get(VAR_4842);

            if (valueToCut != null && valueToCut.length() >= 7) {
                return valueToCut.substring(4, 7);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static String securityLevelECI(String ECI) {

        if (ECI == null) {
            return null;
        }

        return switch (ECI.substring(1, 3)) {
            case "12","14" -> "05";
            case "11","13" -> "06";
            case "21","22","23" -> "07";
            default -> "08";
        };
    }

    public static String channelTPVIndicator(Map<String, String> subFields, String merchantType){

        if(RETAIL.equals(subFields.get(VAR_4801)) && subFields.containsKey(VAR_2201) && subFields.containsKey(VAR_6111) && subFields.containsKey(VAR_6105)
                &&(subFields.get(VAR_2201).equals("05") || subFields.get(VAR_2201).equals("07") || subFields.get(VAR_2201).equals("02"))
                && VALID_VALUES.contains(subFields.get(VAR_6111))
                && subFields.get(VAR_6105).equals("0")){
            return "POST";
        }else if(subFields.containsKey(VAR_0301) && subFields.get(VAR_0301).equals("01") && merchantType != null && merchantType.equals("6011")){
            return "ATMT";
        }else if(subFields.containsKey(VAR_0301) && subFields.get(VAR_0301).equals("01") && merchantType != null && merchantType.equals("6010")){
            return "OTHNRETV";
        }else{
            return "OTHN";
        }
    }

    public static String entryModeIndicator(Map<String, String> subFields, String cardDataEntryMode) {

        if ( cardDataEntryMode == null || cardDataEntryMode.isEmpty()) {
            return null;
        }

        return switch (cardDataEntryMode) {
            case "MLEY" -> "MANUAL";
            case "CICC" -> "CARDCHIP";
            case "CTLS" -> "CARDCTLS";
            case "MGST" -> "CARDSTRP";
            case "MBNK" -> "PHONE";
            case "QRCD" -> "QR";
            default -> "ALL";
        };
    }

}
