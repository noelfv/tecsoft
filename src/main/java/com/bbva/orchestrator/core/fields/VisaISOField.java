package com.bbva.orchestrator.core.fields;

import com.bbva.orchestrator.core.fields.definitions.ISODataType;
import com.bbva.orchestrator.core.fields.definitions.ISOField;
import com.bbva.orchestrator.core.parser.iso8583.strategy.FieldParserStrategy;
import com.bbva.orchestrator.core.parser.iso8583.strategy.fields.*;
import java.util.HashMap;
import java.util.Map;

public enum VisaISOField implements ISOField {

    // Campos de longitud fija
    HEADER(0, "header", ISODataType.NUMERIC, false, 2, new NumericFieldParser()),
    MESSAGE_TYPE(0, "messageType", ISODataType.NUMERIC, false, 4, new NumericFieldParser()),
    BITMAP_PRIMARY(0, "bitmap_primary", ISODataType.BINARY_STRING, false, 8, new BinaryStringFieldParser()), // 8 bytes = 16 chars HEX
    BITMAP_SECONDARY(1, "bitmap_secondary", ISODataType.BINARY_STRING, false, 8, new BinaryStringFieldParser()),
    PRIMARY_ACCOUNT_NUMBER(2, "primaryAccountNumber", ISODataType.NUMERIC_ODD_VARIABLE, true, 2, new LlvarLengthPrefixParserVisa(new LlvarLengthPrefixParser(new AlphaNumericDecoratorFieldParser()))),
    PROCESSING_CODE(3, "processingCode", ISODataType.NUMERIC, false, 6, new NumericFieldParser()),
    TRANSACTION_AMOUNT(4, "transactionAmount", ISODataType.NUMERIC_DECIMAL, false, 12, new NumericDecimalFieldParser()),
    SETTLEMENT_AMOUNT(5, "settlementAmount", ISODataType.NUMERIC_DECIMAL, false, 12, new NumericDecimalFieldParser()),
    CARD_HOLDER_BILLING_AMOUNT(6, "cardHolderBillingAmount", ISODataType.NUMERIC_DECIMAL, false, 12, new NumericDecimalFieldParser()),
    TRANSMISSION_DATE_TIME(7, "transmissionDateTime", ISODataType.NUMERIC, false, 10, new NumericFieldParser()),
    AMOUNT_CARD_HOLDER_BILLING_FEE(8, "amountCardholderBillingFee", ISODataType.NUMERIC_DECIMAL, false, 8, new NumericFieldParser()),
    CONVERSION_RATE_SETTLEMENT(9, "conversionRateSettlement", ISODataType.NUMERIC, false, 8, new NumericFieldParser()),
    CONVERSION_RATE(10, "conversionRate", ISODataType.NUMERIC, false, 8, new NumericFieldParser()),
    SYSTEM_TRACE_AUDIT_NUMBER(11, "systemTraceAuditNumber", ISODataType.NUMERIC, false, 6, new NumericFieldParser()),
    LOCAL_TRANSACTION_TIME(12, "localTransactionTime", ISODataType.NUMERIC, false, 6, new NumericFieldParser()),
    LOCAL_TRANSACTION_DATE(13, "localTransactionDate", ISODataType.NUMERIC, false, 4, new NumericFieldParser()),
    DATE_EXPIRATION(14, "dateExpiration", ISODataType.NUMERIC, false, 4, new NumericFieldParser()),
    SETTLEMENT_DATE(15, "settlementDate", ISODataType.NUMERIC, false, 4, new NumericFieldParser()),
    DATE_CONVERSION(16, "dateConversion", ISODataType.NUMERIC, false, 4, new NumericFieldParser()),
    CAPTURE_DATE(17, "captureDate", ISODataType.NUMERIC, false, 4, new NumericFieldParser()),
    MERCHANT_TYPE(18, "merchantType", ISODataType.NUMERIC, false, 4, new NumericFieldParser()),
    ACQUIRER_COUNTRY_CODE(19, "acquirerCountryCode", ISODataType.NUMERIC_ODD, false, 3, new NumericFieldParser()),
    PAN_COUNTRY_CODE(20, "primaryAccountNumberCountryCode", ISODataType.NUMERIC_ODD, false, 3, new NumericFieldParser()),
    FORWARDING_INSTITUTION_COUNTRY_CODE(21, "forwardingInstitutionCountryCode", ISODataType.NUMERIC_ODD, false, 3, new NumericFieldParser()),
    POINT_SERVICE_ENTRY_MODE(22, "pointServiceEntryMode", ISODataType.NUMERIC, false, 4, new NumericFieldParser()),
    CARD_SEQUENCE_NUMBER(23, "cardSequenceNumber", ISODataType.NUMERIC_ODD, false, 3, new NumericFieldParser()),
    NETWORK_INTERNATIONAL_ID(24, "networkInternationalId", ISODataType.NUMERIC_ODD, false, 3, new NumericFieldParser()),
    POINT_SERVICE_CONDITION_CODE(25, "pointServiceConditionCode", ISODataType.NUMERIC, false, 2, new NumericFieldParser()),
    POINT_SERVICE_PERSONAL_ID_NUMBER(26, "pointServicePIN", ISODataType.NUMERIC, false, 2, new NumericFieldParser()),
    AMOUNT_TRANSACTION_FEE(28, "amountTransactionFee", ISODataType.ALPHA_NUMERIC, false, 9, new AlphaNumericFieldParser()),
    AMOUNT_SETTLEMENT_FEE(29, "amountSettlementFee", ISODataType.ALPHA_NUMERIC, false, 9, new AlphaNumericFieldParser()),
    AMOUNT_TRANSACTION_PROCESSING_FEE(30, "amountTransactionProcessingFee", ISODataType.ALPHA_NUMERIC, false, 9, new AlphaNumericFieldParser()),
    AMOUNT_SETTLEMENT_PROCESSING_FEE(31, "amountSettlementProcessingFee", ISODataType.ALPHA_NUMERIC, false, 9, new AlphaNumericFieldParser()),
    ACQUIRING_INSTITUTION_IDENTIFICATION_CODE(32, "acquiringInstitutionIdentificationCode", ISODataType.NUMERIC_ODD_VARIABLE, true, 2, new LlvarLengthPrefixParserVisa(new LlvarLengthPrefixParser(new AlphaNumericDecoratorFieldParser()))),
    FORWARDING_INSTITUTION_IDENTIFICATION_CODE(33, "forwardingInstitutionIdentificationCode", ISODataType.NUMERIC_ODD_VARIABLE, true, 2, new LlvarLengthPrefixParserVisa(new LlvarLengthPrefixParser(new AlphaNumericDecoratorFieldParser()))),
    PAN_EXTENDED(34, "primaryAccountNumberExtended", ISODataType.HEXADECIMAL, true, 4, new LlvarLengthPrefixParser(new AlphaNumericFieldParser())),
    TRACK_TWO_DATA(35, "trackTwoData", ISODataType.NUMERIC_ODD_VARIABLE, true, 2, new LlvarLengthPrefixParserVisa(new LlvarLengthPrefixParser(new AlphaNumericFieldParser()))),
    TRACK_THREE_DATA(36, "trackThreeData", ISODataType.NUMERIC_ODD_VARIABLE, true, 2, new LlvarLengthPrefixParserVisa(new LlvarLengthPrefixParser(new AlphaNumericFieldParser()))),
    RETRIEVAL_REFERENCE_NUMBER(37, "retrievalReferenceNumber", ISODataType.ALPHA_NUMERIC, false, 12,new AlphaNumericFieldParser()),
    AUTHORIZATION_IDENTIFICATION_RESPONSE(38, "authorizationIdentificationResponse", ISODataType.ALPHA_NUMERIC, false, 6,new AlphaNumericFieldParser()),
    RESPONSE_CODE(39, "responseCode", ISODataType.ALPHA_NUMERIC, false, 2,new AlphaNumericFieldParser()),
    SERVICE_RESTRICTION_CODE(40, "serviceRestrictionCode", ISODataType.ALPHA_NUMERIC, false, 3,new AlphaNumericFieldParser()),
    CARD_ACCEPTOR_TERMINAL_IDENTIFICATION(41, "cardAcceptorTerminalIdentification", ISODataType.ALPHA_NUMERIC, false, 8,new AlphaNumericFieldParser()),
    CARD_ACCEPTOR_IDENTIFICATION_CODE(42, "cardAcceptorIdentificationCode", ISODataType.ALPHA_NUMERIC, false, 15,new AlphaNumericFieldParser()),
    CARD_ACCEPTOR_NAME_LOCATION(43, "cardAcceptorNameLocation", ISODataType.ALPHA_NUMERIC, false, 40,new AlphaNumericFieldParser()),
    ADDITIONAL_RESPONSE_DATA(44, "additionalResponseData", ISODataType.ALPHA_NUMERIC, true, 2,new LlvarLengthPrefixParser(new AlphaNumericDecoratorFieldParser())),
    TRACK_ONE_DATA(45, "trackOneData", ISODataType.ALPHA_NUMERIC, true, 2,new LlvarLengthPrefixParser(new AlphaNumericDecoratorFieldParser())),
    EXPANDED_ADDITIONAL_AMOUNTS(46, "expandedAdditionalAmounts", ISODataType.ALPHA_NUMERIC, true, 2,new LlvarLengthPrefixParser(new AlphaNumericDecoratorFieldParser())),
    ADDITIONAL_DATA_NATIONAL_USE_2(47, "additionalDataNationalUse2", ISODataType.ALPHA_NUMERIC, true, 2,new LlvarLengthPrefixParser(new AlphaNumericDecoratorFieldParser())),
    ADDITIONAL_DATA_48(48, "additionalDataRetailer", ISODataType.HEXADECIMAL, true, 2,new LlvarLengthPrefixParser(new AlphaNumericDecoratorFieldParser())),
    TRANSACTION_CURRENCY_CODE(49, "transactionCurrencyCode", ISODataType.NUMERIC_ODD, false, 3,new NumericFieldParser()),
    SETTLEMENT_CURRENCY_CODE(50, "settlementCurrencyCode", ISODataType.NUMERIC_ODD, false, 3,new NumericFieldParser()),
    CARD_HOLDER_BILLING_CURRENCY_CODE(51, "cardholderBillingCurrencyCode", ISODataType.NUMERIC_ODD, false, 3,new NumericFieldParser()),
    PIN_DATA(52, "pinData", ISODataType.HEXADECIMAL, false, 8,new HexadecimalFieldParser()),//revisar este campo ya que deberia ser su longitud de 16
    SECURITY_CONTROL_INFORMATION(53, "securityControlInformation", ISODataType.NUMERIC, false, 16,new NumericFieldParser()),
    ADDITIONAL_AMOUNTS(54, "additionalAmounts", ISODataType.ALPHA_NUMERIC, true, 2,new LlvarLengthPrefixParser(new AlphaNumericDecoratorFieldParser())),
    INTEGRATED_CIRCUIT_CARD(55,"integratedCircuitCard",ISODataType.HEXADECIMAL,true,2,new LlvarLengthPrefixParser(new HexadecimalFieldParser())),
    PAYMENT_ACCOUNT_DATA(56,"paymentAccountData",ISODataType.ALPHA_NUMERIC,true,2,new LlvarLengthPrefixParser(new AlphaNumericDecoratorFieldParser())),
    REDEMPTION_POINTS(58, "redemptionPoints", ISODataType.ALPHA_NUMERIC, true, 2,new LlvarLengthPrefixParser(new AlphaNumericDecoratorFieldParser())),
    CAMPAIGN_DATA(59, "campaignData", ISODataType.ALPHA_NUMERIC, true, 2,new LlvarLengthPrefixParser(new AlphaNumericDecoratorFieldParser())),
    POS_TERMINAL_DATA(60, "posTerminalData", ISODataType.HEXADECIMAL, true, 2,new LlvarLengthPrefixParser(new AlphaNumericDecoratorFieldParser())),
    POS_CARD_ISSUER(61, "posCardIssuer", ISODataType.HEXADECIMAL, true, 2,new LlvarLengthPrefixParser(new AlphaNumericDecoratorFieldParser())),
    POSTAL_CODE(62, "postalCode", ISODataType.HEXADECIMAL, true, 2,new LlvarLengthPrefixParser(new AlphaNumericDecoratorFieldParser())),
    NETWORK_DATA(63, "networkData", ISODataType.HEXADECIMAL, true, 2,new LlvarLengthPrefixParser(new AlphaNumericDecoratorFieldParser())),
    MESSAGE_AUTHENTICATION_CODE(64, "messageAuthenticationCode", ISODataType.HEXADECIMAL, false, 8,new HexadecimalFieldParser()),
    SETTLEMENT_CODE(66, "settlementCode", ISODataType.NUMERIC, false, 2, new NumericFieldParser()),
    EXTENDED_PAYMENT_CODE(67, "extendedPaymentCode", ISODataType.NUMERIC, false, 4, new NumericFieldParser()),
    RECEIVING_INSTITUTION_COUNTRY_CODE(68, "receivingInstitutionCountryCode", ISODataType.NUMERIC_ODD, false, 3, new NumericFieldParser()),
    SETTLEMENT_INSTITUTION_COUNTRY_CODE(69, "settlementInstitutionCountryCode", ISODataType.NUMERIC_ODD, false, 3, new NumericFieldParser()),
    NETWORK_MANAGEMENT_INFORMATION_CODE(70, "networkManagementInformationCode", ISODataType.NUMERIC_ODD, false, 3, new NumericFieldParser()),
    MESSAGE_NUMBER(71, "messageNumber", ISODataType.NUMERIC, false, 4, new NumericFieldParser()),
    MESSAGE_NUMBER_LAST(72, "messageNumberLast", ISODataType.NUMERIC, false, 4, new NumericFieldParser()),
    DATE_ACTION(73, "dateAction", ISODataType.NUMERIC, false, 6, new NumericFieldParser()),
    CREDITS_NUMBER(74, "creditsNumber", ISODataType.NUMERIC, false, 10, new NumericFieldParser()),
    CREDITS_REVERSAL_NUMBER(75, "creditsReversalNumber", ISODataType.NUMERIC, false, 10, new NumericFieldParser()),
    DEBITS_NUMBER(76, "debitsNumber", ISODataType.NUMERIC, false, 10, new NumericFieldParser()),
    DEBITS_REVERSAL_NUMBER(77, "debitsReversalNumber", ISODataType.NUMERIC, false, 10, new NumericFieldParser()),
    TRANSFER_NUMBER(78, "transferNumber", ISODataType.NUMERIC, false, 10, new NumericFieldParser()),
    TRANSFER_REVERSAL_NUMBER(79, "transferReversalNumber", ISODataType.NUMERIC, false, 10, new NumericFieldParser()),
    INQUIRIES_NUMBER(80, "inquiriesNumber", ISODataType.NUMERIC, false, 10, new NumericFieldParser()),
    AUTHORIZATIONS_NUMBER(81, "authorizationsNumber", ISODataType.NUMERIC, false, 10, new NumericFieldParser()),
    CREDITS_PROCESSING_FEE_AMOUNT(82, "creditsProcessingFeeAmount", ISODataType.NUMERIC, false, 12, new NumericFieldParser()),
    CREDITS_TRANSACTION_FEE_AMOUNT(83, "creditsTransactionFeeAmount", ISODataType.NUMERIC, false, 12, new NumericFieldParser()),
    DEBITS_PROCESSING_FEE_AMOUNT(84, "debitsProcessingFeeAmount", ISODataType.NUMERIC, false, 12, new NumericFieldParser()),
    DEBITS_TRANSACTION_FEE_AMOUNT(85, "debitsTransactionFeeAmount", ISODataType.NUMERIC, false, 12, new NumericFieldParser()),
    CREDITS_AMOUNT(86, "creditsAmount", ISODataType.NUMERIC, false, 16, new NumericFieldParser()),
    CREDITS_REVERSAL_AMOUNT(87, "creditsReversalAmount", ISODataType.NUMERIC, false, 16, new NumericFieldParser()),
    DEBITS_AMOUNT(88, "debitsAmount", ISODataType.NUMERIC, false, 16, new NumericFieldParser()),
    DEBITS_REVERSAL_AMOUNT(89, "debitsReversalAmount", ISODataType.NUMERIC, false, 16, new NumericFieldParser()),
    ORIGINAL_DATA_ELEMENTS(90, "originalDataElements", ISODataType.NUMERIC, false, 42, new NumericFieldParser()),
    FILE_UPDATE_CODE(91, "fileUpdateCode", ISODataType.ALPHA_NUMERIC, false, 1, new AlphaNumericFieldParser()),
    FILE_SECURITY_CODE(92, "fileSecurityCode", ISODataType.ALPHA_NUMERIC, false, 2, new AlphaNumericFieldParser()),
    SERVICE_INDICATOR(94, "serviceIndicator", ISODataType.ALPHA_NUMERIC, false, 7, new AlphaNumericFieldParser()),
    REPLACEMENT_AMOUNTS(95, "replacementAmounts", ISODataType.ALPHA_NUMERIC, false, 42, new AlphaNumericFieldParser()),
    RESERVED_BIT_STRING_96(96, "reservedBitString96", ISODataType.HEXADECIMAL, false, 8, new HexadecimalFieldParser()),
    AMOUNT_NET_SETTLEMENT(97, "amountNetSettlement", ISODataType.ALPHA_NUMERIC, false, 17, new AlphaNumericFieldParser()),
    PAYEE(98, "payee", ISODataType.ALPHA_NUMERIC, false, 25, new AlphaNumericFieldParser()),
    SETTLEMENT_INSTITUTION_ID_CODE(99, "settlementInstitutionIDCode", ISODataType.NUMERIC_ODD_VARIABLE, true, 2, new LlvarLengthPrefixParserVisa(new LlvarLengthPrefixParser(new AlphaNumericDecoratorFieldParser()))),
    RECEIVING_INSTITUTION_ID_CODE(100, "receivingInstitutionIDCode", ISODataType.NUMERIC_ODD_VARIABLE, true, 2, new LlvarLengthPrefixParserVisa(new LlvarLengthPrefixParser(new AlphaNumericDecoratorFieldParser()))),
    FILE_NAME(101, "fileName", ISODataType.ALPHA_NUMERIC, true, 2, new LlvarLengthPrefixParser(new AlphaNumericDecoratorFieldParser())),
    ACCOUNT_IDENTIFICATION_1(102, "accountIdentification1", ISODataType.ALPHA_NUMERIC, true, 2, new LlvarLengthPrefixParser(new AlphaNumericDecoratorFieldParser())),
    ACCOUNT_IDENTIFICATION_2(103, "accountIdentification2", ISODataType.ALPHA_NUMERIC, true, 2, new LlvarLengthPrefixParser(new AlphaNumericDecoratorFieldParser())),
    TRANSACTION_DATA(104, "transactionData",ISODataType.HEXADECIMAL,true,2,new LlvarLengthPrefixParserVisa(new LlvarLengthPrefixParser(new AlphaNumericDecoratorFieldParser()))),
    ADDITIONAL_TRANSACTION_REFERENCE_DATA(108, "additionalTransactionReferenceData", ISODataType.ALPHA_NUMERIC, true, 2,new LlvarLengthPrefixParser(new AlphaNumericDecoratorFieldParser())),
    CARD_ISSUER_REFERENCE_DATA(116, "cardIssuerReferenceData", ISODataType.ALPHA_NUMERIC, true, 2, new LlvarLengthPrefixParser(new AlphaNumericDecoratorFieldParser())),
    KEY_MANAGEMENT(120, "keyManagement", ISODataType.HEXADECIMAL, true, 2,new LlvarLengthPrefixParser(new AlphaNumericDecoratorFieldParser())),
    ISSUING_INSTITUTION_IDENTIFICATION_CODE(121, "issuingInstitutionIdentificationCode", ISODataType.ALPHA_NUMERIC, true, 2, new LlvarLengthPrefixParser(new AlphaNumericFieldParser())),
    ADDITIONAL_DATA_122(122,"additionalRecordData",ISODataType.ALPHA_NUMERIC,true,2,new LlvarLengthPrefixParser(new AlphaNumericFieldParser())),
    CRYPTOGRAPHIC_SERVICE_MESSAGE(123, "cryptographicServiceMessage", ISODataType.HEXADECIMAL, true,2,new LlvarLengthPrefixParser(new AlphaNumericDecoratorFieldParser())),
    INFO_TEXT(124, "infoText", ISODataType.ALPHA_NUMERIC, true, 2,new LlvarLengthPrefixParser(new AlphaNumericDecoratorFieldParser())),
    SETTLEMENT_DATA(125, "settlementData", ISODataType.HEXADECIMAL, true, 2,new LlvarLengthPrefixParser(new AlphaNumericDecoratorFieldParser())),
    ISSUER_TRACE_ID(126, "issuerTraceId", ISODataType.HEXADECIMAL, true, 2,new LlvarLengthPrefixParser(new AlphaNumericDecoratorFieldParser())),
    FILE_RECORDS_ACTION_AND_DATA(127,"fileRecordsActionAndData",ISODataType.ALPHA_NUMERIC,true,2,new LlvarLengthPrefixParser(new AlphaNumericDecoratorFieldParser()));

    private final int id;
    private final String name;
    private final ISODataType typeData;
    private final boolean isVariable;
    private final int length; // Longitud del campo o longitud del prefijo (LL/LLL)
    private final FieldParserStrategy parserStrategy; // La estrategia de parsing/building para este campo

    VisaISOField(int id, String name, ISODataType typeData, boolean isVariable, int length, FieldParserStrategy parserStrategy) {
        this.id = id;
        this.name = name;
        this.typeData = typeData;
        this.isVariable = isVariable;
        this.length = length;
        this.parserStrategy = parserStrategy;
    }

    @Override
    public int getId() { return id; }
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
        return String.valueOf(this.id);
    }

    // Mapa estático para una búsqueda eficiente por ID
    private static final Map<Integer, VisaISOField> BY_ID = new HashMap<>();

    static {
        for (VisaISOField field : values()) {
            BY_ID.put(field.id, field);
        }
    }
    public static VisaISOField getById(int id) {
        return BY_ID.get(id);
    }

}