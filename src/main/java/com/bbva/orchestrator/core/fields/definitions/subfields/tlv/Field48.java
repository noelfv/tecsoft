package com.bbva.orchestrator.core.fields.definitions.subfields.tlv;

import com.bbva.orchestrator.core.fields.definitions.ISODataType;
import com.bbva.orchestrator.core.fields.definitions.ISOSubField;
import com.bbva.orchestrator.core.parser.iso8583.strategy.FieldParserStrategy;
import com.bbva.orchestrator.core.parser.iso8583.strategy.fields.*;

public enum Field48 implements ISOSubField {

    // GRUPO 1: SIN SUBCAMPOS
    // isVariable = true, se toma la longitud de la trama
    // Length referencial, siempre se tomara los 4 primeros dígitos para longitud

    SF_48_01("01", "AdditionalDataRetailer.01", ISODataType.ALPHA_NUMERIC, false, 1, new AlphaNumericFieldParser()),
    SF_48_05("05", "AdditionalDataRetailer.05", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_09("09", "AdditionalDataRetailer.09", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_10("10", "AdditionalDataRetailer.10", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_11("11", "AdditionalDataRetailer.11", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_12("12", "AdditionalDataRetailer.12", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_13("13", "AdditionalDataRetailer.13", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_14("14", "AdditionalDataRetailer.14", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_15("15", "AdditionalDataRetailer.15", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_16("16", "AdditionalDataRetailer.16", ISODataType.NUMERIC, true, 2, new NumericFieldParser()),
    SF_48_17("17", "AdditionalDataRetailer.17", ISODataType.NUMERIC, true, 2, new NumericFieldParser()),
    SF_48_18("18", "AdditionalDataRetailer.18", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_20("20", "AdditionalDataRetailer.20", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_21("21", "AdditionalDataRetailer.21", ISODataType.NUMERIC, true, 2, new NumericFieldParser()),
    SF_48_22("22", "AdditionalDataRetailer.22", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_23("23", "AdditionalDataRetailer.23", ISODataType.ALPHA_NUMERIC, false, 2, new AlphaNumericFieldParser()),
    SF_48_24("24", "AdditionalDataRetailer.24", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_25("25", "AdditionalDataRetailer.25", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_26("26", "AdditionalDataRetailer.26", ISODataType.ALPHA_NUMERIC, false, 3, new AlphaNumericFieldParser()),
    SF_48_27("27", "AdditionalDataRetailer.27", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_28("28", "AdditionalDataRetailer.28", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_29("29", "AdditionalDataRetailer.29", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_30("30", "AdditionalDataRetailer.30", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_32("32", "AdditionalDataRetailer.32", ISODataType.NUMERIC, true, 2, new NumericFieldParser()),
    SF_48_34("34", "AdditionalDataRetailer.34", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_36("36", "AdditionalDataRetailer.36", ISODataType.NUMERIC, true, 2, new NumericFieldParser()),
    SF_48_37("37", "AdditionalDataRetailer.37", ISODataType.NUMERIC, true, 2, new NumericFieldParser()),
    SF_48_40("40", "AdditionalDataRetailer.40", ISODataType.NUMERIC, true, 2, new NumericFieldParser()),
    SF_48_41("41", "AdditionalDataRetailer.41", ISODataType.NUMERIC, true, 2, new NumericFieldParser()),
    SF_48_42("42", "AdditionalDataRetailer.42", ISODataType.NUMERIC, true, 2, new NumericFieldParser()),
    SF_48_43("43", "AdditionalDataRetailer.43", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_44("44", "AdditionalDataRetailer.44", ISODataType.NUMERIC, true, 2, new HexadecimalFieldParser()),
    SF_48_48("48", "AdditionalDataRetailer.48", ISODataType.NUMERIC, true, 2, new NumericFieldParser()),
    SF_48_49("49", "AdditionalDataRetailer.49", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_50("50", "AdditionalDataRetailer.50", ISODataType.NUMERIC, true, 2, new NumericFieldParser()),
    SF_48_51("51", "AdditionalDataRetailer.51", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_53("53", "AdditionalDataRetailer.53", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_55("55", "AdditionalDataRetailer.55", ISODataType.NUMERIC, true, 2, new NumericFieldParser()),
    SF_48_56("56", "AdditionalDataRetailer.56", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_57("57", "AdditionalDataRetailer.57", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_58("58", "AdditionalDataRetailer.58", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_60("60", "AdditionalDataRetailer.60", ISODataType.NUMERIC, true, 2, new NumericFieldParser()),
    SF_48_61("61", "AdditionalDataRetailer.61", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_62("62", "AdditionalDataRetailer.62", ISODataType.NUMERIC, true, 2, new NumericFieldParser()),
    SF_48_63("63", "AdditionalDataRetailer.63", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_64("64", "AdditionalDataRetailer.64", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_65("65", "AdditionalDataRetailer.65", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_66("66", "AdditionalDataRetailer.66", ISODataType.NUMERIC, true, 2, new NumericFieldParser()),
    SF_48_67("67", "AdditionalDataRetailer.67", ISODataType.NUMERIC, true, 2, new NumericFieldParser()),
    SF_48_68("68", "AdditionalDataRetailer.68", ISODataType.NUMERIC, true, 2, new NumericFieldParser()),
    SF_48_71("71", "AdditionalDataRetailer.71", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_72("72", "AdditionalDataRetailer.72", ISODataType.NUMERIC, true, 2, new HexadecimalFieldParser()),
    SF_48_74("74", "AdditionalDataRetailer.74", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_75("75", "AdditionalDataRetailer.75", ISODataType.NUMERIC, true, 2, new NumericFieldParser()),
    SF_48_77("77", "AdditionalDataRetailer.77", ISODataType.NUMERIC, false, 3, new NumericFieldParser()),
    SF_48_78("78", "AdditionalDataRetailer.78", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_79("79", "AdditionalDataRetailer.79", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_80("80", "AdditionalDataRetailer.80", ISODataType.ALPHA_NUMERIC, false, 2, new AlphaNumericFieldParser()),
    SF_48_92("92", "AdditionalDataRetailer.92", ISODataType.NUMERIC, true, 2, new NumericFieldParser()),
    SF_48_93("93", "AdditionalDataRetailer.93", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_95("95", "AdditionalDataRetailer.95", ISODataType.ALPHA_NUMERIC, false, 6, new AlphaNumericFieldParser()),


    // --- GRUPO 2: Con subsubcampos

    SF_48_33("33", "AdditionalDataRetailer.33", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),



    // ----------- SUB SUB CAMPOS (Longitud Fijas) -----------

    // Añadir cuando sea necesario

    // ----------- SUB SUB CAMPOS (Longitud variable) -----------

    // Sub-subcampos de Campo 48.33 (No está en el mapa, de la version anterior)
    SF_48_33_01("33.01", "AdditionalDataRetailer.33.01", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_33_02("33.02", "AdditionalDataRetailer.33.02", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_33_03("33.03", "AdditionalDataRetailer.33.03", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_33_05("33.05", "AdditionalDataRetailer.33.05", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_33_06("33.06", "AdditionalDataRetailer.33.06", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser()),
    SF_48_33_08("33.08", "AdditionalDataRetailer.33.08", ISODataType.ALPHA_NUMERIC, true, 2, new AlphaNumericFieldParser())


    ; // <---- Fin del enum

    private final String id;
    private final String name;
    private final ISODataType typeData;
    private final boolean isVariable;
    private final int length;
    private FieldParserStrategy parserStrategy;


    Field48(String id, String name, ISODataType typeData, boolean isVariable, int length, FieldParserStrategy parserStrategy) {
        this.id = id;
        this.name = name;
        this.typeData = typeData;
        this.isVariable = isVariable;
        this.length = length;
        this.parserStrategy = parserStrategy;
    }

    @Override
    public String getId() { return id; }
    @Override
    public String getName() { return name; }
    @Override
    public ISODataType getTypeData() { return typeData; }
    @Override
    public boolean isVariable() { return isVariable; }
    @Override
    public int getLength() { return length; }
    @Override
    public FieldParserStrategy getParserStrategy() { return parserStrategy; }

    // Implementación de getIdentifier() para IFieldDefinition
    @Override
    public String getIdentifier() {
        return this.id;
    }


    public void setParserStrategy(FieldParserStrategy parserStrategy) {
        this.parserStrategy = parserStrategy;
    }
}