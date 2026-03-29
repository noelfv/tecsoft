package com.bbva.orchestrator.core.mapper.model;

import java.util.Collections;
import java.util.Map;

/**
 * Wrapper inmutable sobre el Map canónico de campos de un mensaje.
 *
 * <p>Actúa como la única fuente de datos para toda la capa de mapeo ISO-20022,
 * reemplazando el DTO {@code ISO8583} que acoplaba las strategies al protocolo.
 * Al recibir un {@code Map<String,String>} genérico, cualquier transformer
 * (ISO8583, JSON, XML) puede alimentar el mapper sin cambios en las strategies.
 *
 * <p>Uso:
 * <pre>{@code
 *   CanonicalFields fields = CanonicalFields.of(operation.enrichedFields());
 *   delegateMapper.mapper(fields);
 * }</pre>
 *
 * <p>Para subcampos con formato {@code "NN.NN"} o claves especiales
 * ({@code "ADDITIONAL_*"}, {@code "ECI"}), usar el escape hatch genérico:
 * <pre>{@code
 *   fields.getOrDefault("22.01", null);
 *   fields.get("ADDITIONAL_AMOUNT_DOUBLE");
 * }</pre>
 */
public final class CanonicalFields {

    // ── Constantes de keys ─────────────────────────────────────────────────────

    public static final String KEY_HEADER = "header";
    public static final String KEY_MESSAGE_TYPE = "messageType";
    public static final String KEY_ORIGINAL_MESSAGE = "originalMessage";
    public static final String KEY_PRIMARY_ACCOUNT_NUMBER = "primaryAccountNumber";
    public static final String KEY_PROCESSING_CODE = "processingCode";
    public static final String KEY_TRANSACTION_AMOUNT = "transactionAmount";
    public static final String KEY_SETTLEMENT_AMOUNT = "settlementAmount";
    public static final String KEY_CARD_HOLDER_BILLING_AMOUNT = "cardHolderBillingAmount";
    public static final String KEY_TRANSMISSION_DATE_TIME = "transmissionDateTime";
    public static final String KEY_AMOUNT_CARDHOLDER_BILLING_FEE = "amountCardholderBillingFee";
    public static final String KEY_CONVERSION_RATE_SETTLEMENT = "conversionRateSettlement";
    public static final String KEY_CONVERSION_RATE = "conversionRate";
    public static final String KEY_SYSTEM_TRACE_AUDIT_NUMBER = "systemTraceAuditNumber";
    public static final String KEY_LOCAL_TRANSACTION_TIME = "localTransactionTime";
    public static final String KEY_LOCAL_TRANSACTION_DATE = "localTransactionDate";
    public static final String KEY_DATE_EXPIRATION = "dateExpiration";
    public static final String KEY_SETTLEMENT_DATE = "settlementDate";
    public static final String KEY_DATE_CONVERSION = "dateConversion";
    public static final String KEY_CAPTURE_DATE = "captureDate";
    public static final String KEY_MERCHANT_TYPE = "merchantType";
    public static final String KEY_ACQUIRER_COUNTRY_CODE = "acquirerCountryCode";
    public static final String KEY_PRIMARY_ACCOUNT_NUMBER_COUNTRY_CODE = "primaryAccountNumberCountryCode";
    public static final String KEY_FORWARDING_INSTITUTION_COUNTRY_CODE = "forwardingInstitutionCountryCode";
    public static final String KEY_POINT_SERVICE_ENTRY_MODE = "pointServiceEntryMode";
    public static final String KEY_CARD_SEQUENCE_NUMBER = "cardSequenceNumber";
    public static final String KEY_NETWORK_INTERNATIONAL_ID = "networkInternationalId";
    public static final String KEY_POINT_SERVICE_CONDITION_CODE = "pointServiceConditionCode";
    public static final String KEY_POINT_SERVICE_PIN = "pointServicePIN";
    public static final String KEY_AUTHORIZATION_ID_RESPONSE_LENGTH = "authorizationIdResponseLength";
    public static final String KEY_AMOUNT_TRANSACTION_FEE = "amountTransactionFee";
    public static final String KEY_AMOUNT_SETTLEMENT_FEE = "amountSettlementFee";
    public static final String KEY_AMOUNT_TRANSACTION_PROCESSING_FEE = "amountTransactionProcessingFee";
    public static final String KEY_AMOUNT_SETTLEMENT_PROCESSING_FEE = "amountSettlementProcessingFee";
    public static final String KEY_ACQUIRING_INSTITUTION_IDENTIFICATION_CODE = "acquiringInstitutionIdentificationCode";
    public static final String KEY_FORWARDING_INSTITUTION_IDENTIFICATION_CODE = "forwardingInstitutionIdentificationCode";
    public static final String KEY_PRIMARY_ACCOUNT_NUMBER_EXTENDED = "primaryAccountNumberExtended";
    public static final String KEY_TRACK_TWO_DATA = "trackTwoData";
    public static final String KEY_TRACK_THREE_DATA = "trackThreeData";
    public static final String KEY_RETRIEVAL_REFERENCE_NUMBER = "retrievalReferenceNumber";
    public static final String KEY_AUTHORIZATION_IDENTIFICATION_RESPONSE = "authorizationIdentificationResponse";
    public static final String KEY_RESPONSE_CODE = "responseCode";
    public static final String KEY_SERVICE_RESTRICTION_CODE = "serviceRestrictionCode";
    public static final String KEY_CARD_ACCEPTOR_TERMINAL_IDENTIFICATION = "cardAcceptorTerminalIdentification";
    public static final String KEY_CARD_ACCEPTOR_IDENTIFICATION_CODE = "cardAcceptorIdentificationCode";
    public static final String KEY_CARD_ACCEPTOR_NAME_LOCATION = "cardAcceptorNameLocation";
    public static final String KEY_ADDITIONAL_RESPONSE_DATA = "additionalResponseData";
    public static final String KEY_TRACK_ONE_DATA = "trackOneData";
    public static final String KEY_EXPANDED_ADDITIONAL_AMOUNTS = "expandedAdditionalAmounts";
    public static final String KEY_ADDITIONAL_DATA_NATIONAL_USE = "additionalDataNationalUse";
    public static final String KEY_ADDITIONAL_DATA_RETAILER = "additionalDataRetailer";
    public static final String KEY_TRANSACTION_CURRENCY_CODE = "transactionCurrencyCode";
    public static final String KEY_SETTLEMENT_CURRENCY_CODE = "settlementCurrencyCode";
    public static final String KEY_CARDHOLDER_BILLING_CURRENCY_CODE = "cardholderBillingCurrencyCode";
    public static final String KEY_PIN_DATA = "pinData";
    public static final String KEY_SECURITY_CONTROL_INFORMATION = "securityControlInformation";
    public static final String KEY_ADDITIONAL_AMOUNTS = "additionalAmounts";
    public static final String KEY_INTEGRATED_CIRCUIT_CARD = "integratedCircuitCard";
    public static final String KEY_PAYMENT_ACCOUNT_DATA = "paymentAccountData";
    public static final String KEY_REDEMPTION_POINTS = "redemptionPoints";
    public static final String KEY_CAMPAIGN_DATA = "campaignData";
    public static final String KEY_POS_TERMINAL_DATA = "posTerminalData";
    public static final String KEY_POS_CARD_ISSUER = "posCardIssuer";
    public static final String KEY_POSTAL_CODE = "postalCode";
    public static final String KEY_NETWORK_DATA = "networkData";
    public static final String KEY_MESSAGE_AUTHENTICATION_CODE = "messageAuthenticationCode";
    public static final String KEY_SETTLEMENT_CODE = "settlementCode";
    public static final String KEY_EXTENDED_PAYMENT_CODE = "extendedPaymentCode";
    public static final String KEY_RECEIVING_INSTITUTION_COUNTRY_CODE = "receivingInstitutionCountryCode";
    public static final String KEY_SETTLEMENT_INSTITUTION_COUNTRY_CODE = "settlementInstitutionCountryCode";
    public static final String KEY_NETWORK_MANAGEMENT_INFORMATION_CODE = "networkManagementInformationCode";
    public static final String KEY_MESSAGE_NUMBER = "messageNumber";
    public static final String KEY_MESSAGE_NUMBER_LAST = "messageNumberLast";
    public static final String KEY_DATE_ACTION = "dateAction";
    public static final String KEY_CREDITS_NUMBER = "creditsNumber";
    public static final String KEY_CREDITS_REVERSAL_NUMBER = "creditsReversalNumber";
    public static final String KEY_DEBITS_NUMBER = "debitsNumber";
    public static final String KEY_DEBITS_REVERSAL_NUMBER = "debitsReversalNumber";
    public static final String KEY_TRANSFER_NUMBER = "transferNumber";
    public static final String KEY_TRANSFER_REVERSAL_NUMBER = "transferReversalNumber";
    public static final String KEY_INQUIRIES_NUMBER = "inquiriesNumber";
    public static final String KEY_AUTHORIZATION_NUMBER = "authorizationNumber";
    public static final String KEY_CREDITS_PROCESSING_FEE_AMOUNT = "creditsProcessingFeeAmount";
    public static final String KEY_CREDITS_TRANSACTION_FEE_AMOUNT = "creditsTransactionFeeAmount";
    public static final String KEY_DEBITS_PROCESSING_FEE_AMOUNT = "debitsProcessingFeeAmount";
    public static final String KEY_DEBITS_TRANSACTION_FEE_AMOUNT = "debitsTransactionFeeAmount";
    public static final String KEY_CREDITS_AMOUNT = "creditsAmount";
    public static final String KEY_CREDITS_REVERSAL_AMOUNT = "creditsReversalAmount";
    public static final String KEY_DEBITS_AMOUNT = "debitsAmount";
    public static final String KEY_DEBITS_REVERSAL_AMOUNT = "debitsReversalAmount";
    public static final String KEY_ORIGINAL_DATA_ELEMENTS = "originalDataElements";
    public static final String KEY_FILE_UPDATE_CODE = "fileUpdateCode";
    public static final String KEY_FILE_SECURITY_CODE = "fileSecurityCode";
    public static final String KEY_RESPONSE_INDICATOR = "responseIndicator";
    public static final String KEY_SERVICE_INDICATOR = "serviceIndicator";
    public static final String KEY_REPLACEMENT_AMOUNTS = "replacementAmounts";
    public static final String KEY_MESSAGE_SECURITY_CODE = "messageSecurityCode";
    public static final String KEY_AMOUNT_NET_SETTLEMENT = "amountNetSettlement";
    public static final String KEY_PAYEE = "payee";
    public static final String KEY_SETTLEMENT_INSTITUTION_IDENTIFICATION_CODE = "settlementInstitutionIdentificationCode";
    public static final String KEY_RECEIVING_INSTITUTION_IDENTIFICATION_CODE = "receivingInstitutionIdentificationCode";
    public static final String KEY_FILE_NAME = "fileName";
    public static final String KEY_ACCOUNT_IDENTIFICATION_1 = "accountIdentification1";
    public static final String KEY_ACCOUNT_IDENTIFICATION_2 = "accountIdentification2";
    public static final String KEY_TRANSACTION_DATA = "transactionData";
    public static final String KEY_DOUBLE_LENGTH_DES_KEY = "doubleLengthDesKey";
    public static final String KEY_FLEET_SERVICE_DATA = "fleetServiceData";
    public static final String KEY_ADDITIONAL_TRANSACTION_REFERENCE_DATA = "additionalTransactionReferenceData";
    public static final String KEY_ISO_USE = "isoUse";
    public static final String KEY_ENCRYPTION_DATA = "encryptionData";
    public static final String KEY_ADDITIONAL_DATA_NATIONAL_USE_2 = "additionalDataNationalUse2";
    public static final String KEY_RESERVED_NATIONAL_USE = "reservedNationalUse";
    public static final String KEY_RESERVED_NATIONAL_USE_2 = "reservedNationalUse2";
    public static final String KEY_KEY_MANAGEMENT = "keyManagement";
    public static final String KEY_AUTHORIZING_AGENT_ID_CODE = "authorizingAgentIdCode";
    public static final String KEY_ADDITIONAL_RECORD_DATA = "additionalRecordData";
    public static final String KEY_CRYPTOGRAPHIC_SERVICE_MESSAGE = "cryptographicServiceMessage";
    public static final String KEY_INFO_TEXT = "infoText";
    public static final String KEY_SETTLEMENT_DATA = "settlementData";
    public static final String KEY_ISSUER_TRACE_ID = "issuerTraceId";
    public static final String KEY_PRIVATE_DATA = "privateData";
    public static final String KEY_MESSAGE_AUTHENTICATION_CODE_2 = "messageAuthenticationCode2";
    public static final String KEY_NETWORK_NAME = "networkName";
    public static final String KEY_PLAIN_TEXT_PCI = "plainTextPCI";
    public static final String KEY_REJECT_FLAG = "rejectFlag";
    public static final String KEY_TRANSACTION_TYPE = "transactionType";
    public static final String KEY_BIN_CODE = "binCode";

    // ── Estado ─────────────────────────────────────────────────────────────────

    private final Map<String, String> fields;

    private CanonicalFields(Map<String, String> fields) {
        this.fields = fields;
    }

    /**
     * Crea un {@code CanonicalFields} a partir del Map canónico producido por
     * cualquier {@code DelegateTransformer}.
     *
     * @param fields Map canónico con campos base + subcampos ("03.01", "48.42", etc.)
     * @return instancia inmutable de CanonicalFields
     */
    public static CanonicalFields of(Map<String, String> fields) {
        return new CanonicalFields(fields);
    }

    // ── Accessors tipados ──────────────────────────────────────────────────────

    public String getHeader()                                        { return fields.get(KEY_HEADER); }
    public String getMessageType()                                   { return fields.get(KEY_MESSAGE_TYPE); }
    public String getOriginalMessage()                               { return fields.get(KEY_ORIGINAL_MESSAGE); }
    public String getPrimaryAccountNumber()                          { return fields.get(KEY_PRIMARY_ACCOUNT_NUMBER); }
    public String getProcessingCode()                                { return fields.get(KEY_PROCESSING_CODE); }
    public String getTransactionAmount()                             { return fields.get(KEY_TRANSACTION_AMOUNT); }
    public String getSettlementAmount()                              { return fields.get(KEY_SETTLEMENT_AMOUNT); }
    public String getCardHolderBillingAmount()                       { return fields.get(KEY_CARD_HOLDER_BILLING_AMOUNT); }
    public String getTransmissionDateTime()                          { return fields.get(KEY_TRANSMISSION_DATE_TIME); }
    public String getAmountCardholderBillingFee()                    { return fields.get(KEY_AMOUNT_CARDHOLDER_BILLING_FEE); }
    public String getConversionRateSettlement()                      { return fields.get(KEY_CONVERSION_RATE_SETTLEMENT); }
    public String getConversionRate()                                { return fields.get(KEY_CONVERSION_RATE); }
    public String getSystemTraceAuditNumber()                        { return fields.get(KEY_SYSTEM_TRACE_AUDIT_NUMBER); }
    public String getLocalTransactionTime()                          { return fields.get(KEY_LOCAL_TRANSACTION_TIME); }
    public String getLocalTransactionDate()                          { return fields.get(KEY_LOCAL_TRANSACTION_DATE); }
    public String getDateExpiration()                                { return fields.get(KEY_DATE_EXPIRATION); }
    public String getSettlementDate()                                { return fields.get(KEY_SETTLEMENT_DATE); }
    public String getDateConversion()                                { return fields.get(KEY_DATE_CONVERSION); }
    public String getCaptureDate()                                   { return fields.get(KEY_CAPTURE_DATE); }
    public String getMerchantType()                                  { return fields.get(KEY_MERCHANT_TYPE); }
    public String getAcquirerCountryCode()                           { return fields.get(KEY_ACQUIRER_COUNTRY_CODE); }
    public String getPrimaryAccountNumberCountryCode()               { return fields.get(KEY_PRIMARY_ACCOUNT_NUMBER_COUNTRY_CODE); }
    public String getForwardingInstitutionCountryCode()              { return fields.get(KEY_FORWARDING_INSTITUTION_COUNTRY_CODE); }
    public String getPointServiceEntryMode()                         { return fields.get(KEY_POINT_SERVICE_ENTRY_MODE); }
    public String getCardSequenceNumber()                            { return fields.get(KEY_CARD_SEQUENCE_NUMBER); }
    public String getNetworkInternationalId()                        { return fields.get(KEY_NETWORK_INTERNATIONAL_ID); }
    public String getPointServiceConditionCode()                     { return fields.get(KEY_POINT_SERVICE_CONDITION_CODE); }
    public String getPointServicePIN()                               { return fields.get(KEY_POINT_SERVICE_PIN); }
    public String getAuthorizationIdResponseLength()                 { return fields.get(KEY_AUTHORIZATION_ID_RESPONSE_LENGTH); }
    public String getAmountTransactionFee()                          { return fields.get(KEY_AMOUNT_TRANSACTION_FEE); }
    public String getAmountSettlementFee()                           { return fields.get(KEY_AMOUNT_SETTLEMENT_FEE); }
    public String getAmountTransactionProcessingFee()                { return fields.get(KEY_AMOUNT_TRANSACTION_PROCESSING_FEE); }
    public String getAmountSettlementProcessingFee()                 { return fields.get(KEY_AMOUNT_SETTLEMENT_PROCESSING_FEE); }
    public String getAcquiringInstitutionIdentificationCode()        { return fields.get(KEY_ACQUIRING_INSTITUTION_IDENTIFICATION_CODE); }
    public String getForwardingInstitutionIdentificationCode()       { return fields.get(KEY_FORWARDING_INSTITUTION_IDENTIFICATION_CODE); }
    public String getPrimaryAccountNumberExtended()                  { return fields.get(KEY_PRIMARY_ACCOUNT_NUMBER_EXTENDED); }
    public String getTrackTwoData()                                  { return fields.get(KEY_TRACK_TWO_DATA); }
    public String getTrackThreeData()                                { return fields.get(KEY_TRACK_THREE_DATA); }
    public String getRetrievalReferenceNumber()                      { return fields.get(KEY_RETRIEVAL_REFERENCE_NUMBER); }
    public String getAuthorizationIdentificationResponse()           { return fields.get(KEY_AUTHORIZATION_IDENTIFICATION_RESPONSE); }
    public String getResponseCode()                                  { return fields.get(KEY_RESPONSE_CODE); }
    public String getServiceRestrictionCode()                        { return fields.get(KEY_SERVICE_RESTRICTION_CODE); }
    public String getCardAcceptorTerminalIdentification()            { return fields.get(KEY_CARD_ACCEPTOR_TERMINAL_IDENTIFICATION); }
    public String getCardAcceptorIdentificationCode()                { return fields.get(KEY_CARD_ACCEPTOR_IDENTIFICATION_CODE); }
    public String getCardAcceptorNameLocation()                      { return fields.get(KEY_CARD_ACCEPTOR_NAME_LOCATION); }
    public String getAdditionalResponseData()                        { return fields.get(KEY_ADDITIONAL_RESPONSE_DATA); }
    public String getTrackOneData()                                  { return fields.get(KEY_TRACK_ONE_DATA); }
    public String getExpandedAdditionalAmounts()                     { return fields.get(KEY_EXPANDED_ADDITIONAL_AMOUNTS); }
    public String getAdditionalDataNationalUse()                     { return fields.get(KEY_ADDITIONAL_DATA_NATIONAL_USE); }
    public String getAdditionalDataRetailer()                        { return fields.get(KEY_ADDITIONAL_DATA_RETAILER); }
    public String getTransactionCurrencyCode()                       { return fields.get(KEY_TRANSACTION_CURRENCY_CODE); }
    public String getSettlementCurrencyCode()                        { return fields.get(KEY_SETTLEMENT_CURRENCY_CODE); }
    public String getCardholderBillingCurrencyCode()                 { return fields.get(KEY_CARDHOLDER_BILLING_CURRENCY_CODE); }
    public String getPinData()                                       { return fields.get(KEY_PIN_DATA); }
    public String getSecurityControlInformation()                    { return fields.get(KEY_SECURITY_CONTROL_INFORMATION); }
    public String getAdditionalAmounts()                             { return fields.get(KEY_ADDITIONAL_AMOUNTS); }
    public String getIntegratedCircuitCard()                         { return fields.get(KEY_INTEGRATED_CIRCUIT_CARD); }
    public String getPaymentAccountData()                            { return fields.get(KEY_PAYMENT_ACCOUNT_DATA); }
    public String getRedemptionPoints()                              { return fields.get(KEY_REDEMPTION_POINTS); }
    public String getCampaignData()                                  { return fields.get(KEY_CAMPAIGN_DATA); }
    public String getPosTerminalData()                               { return fields.get(KEY_POS_TERMINAL_DATA); }
    public String getPosCardIssuer()                                 { return fields.get(KEY_POS_CARD_ISSUER); }
    public String getPostalCode()                                    { return fields.get(KEY_POSTAL_CODE); }
    public String getNetworkData()                                   { return fields.get(KEY_NETWORK_DATA); }
    public String getMessageAuthenticationCode()                     { return fields.get(KEY_MESSAGE_AUTHENTICATION_CODE); }
    public String getSettlementCode()                                { return fields.get(KEY_SETTLEMENT_CODE); }
    public String getExtendedPaymentCode()                           { return fields.get(KEY_EXTENDED_PAYMENT_CODE); }
    public String getReceivingInstitutionCountryCode()               { return fields.get(KEY_RECEIVING_INSTITUTION_COUNTRY_CODE); }
    public String getSettlementInstitutionCountryCode()              { return fields.get(KEY_SETTLEMENT_INSTITUTION_COUNTRY_CODE); }
    public String getNetworkManagementInformationCode()              { return fields.get(KEY_NETWORK_MANAGEMENT_INFORMATION_CODE); }
    public String getMessageNumber()                                 { return fields.get(KEY_MESSAGE_NUMBER); }
    public String getMessageNumberLast()                             { return fields.get(KEY_MESSAGE_NUMBER_LAST); }
    public String getDateAction()                                    { return fields.get(KEY_DATE_ACTION); }
    public String getCreditsNumber()                                 { return fields.get(KEY_CREDITS_NUMBER); }
    public String getCreditsReversalNumber()                         { return fields.get(KEY_CREDITS_REVERSAL_NUMBER); }
    public String getDebitsNumber()                                  { return fields.get(KEY_DEBITS_NUMBER); }
    public String getDebitsReversalNumber()                          { return fields.get(KEY_DEBITS_REVERSAL_NUMBER); }
    public String getTransferNumber()                                { return fields.get(KEY_TRANSFER_NUMBER); }
    public String getTransferReversalNumber()                        { return fields.get(KEY_TRANSFER_REVERSAL_NUMBER); }
    public String getInquiriesNumber()                               { return fields.get(KEY_INQUIRIES_NUMBER); }
    public String getAuthorizationNumber()                           { return fields.get(KEY_AUTHORIZATION_NUMBER); }
    public String getCreditsProcessingFeeAmount()                    { return fields.get(KEY_CREDITS_PROCESSING_FEE_AMOUNT); }
    public String getCreditsTransactionFeeAmount()                   { return fields.get(KEY_CREDITS_TRANSACTION_FEE_AMOUNT); }
    public String getDebitsProcessingFeeAmount()                     { return fields.get(KEY_DEBITS_PROCESSING_FEE_AMOUNT); }
    public String getDebitsTransactionFeeAmount()                    { return fields.get(KEY_DEBITS_TRANSACTION_FEE_AMOUNT); }
    public String getCreditsAmount()                                 { return fields.get(KEY_CREDITS_AMOUNT); }
    public String getCreditsReversalAmount()                         { return fields.get(KEY_CREDITS_REVERSAL_AMOUNT); }
    public String getDebitsAmount()                                  { return fields.get(KEY_DEBITS_AMOUNT); }
    public String getDebitsReversalAmount()                          { return fields.get(KEY_DEBITS_REVERSAL_AMOUNT); }
    public String getOriginalDataElements()                          { return fields.get(KEY_ORIGINAL_DATA_ELEMENTS); }
    public String getFileUpdateCode()                                { return fields.get(KEY_FILE_UPDATE_CODE); }
    public String getFileSecurityCode()                              { return fields.get(KEY_FILE_SECURITY_CODE); }
    public String getResponseIndicator()                             { return fields.get(KEY_RESPONSE_INDICATOR); }
    public String getServiceIndicator()                              { return fields.get(KEY_SERVICE_INDICATOR); }
    public String getReplacementAmounts()                            { return fields.get(KEY_REPLACEMENT_AMOUNTS); }
    public String getMessageSecurityCode()                           { return fields.get(KEY_MESSAGE_SECURITY_CODE); }
    public String getAmountNetSettlement()                           { return fields.get(KEY_AMOUNT_NET_SETTLEMENT); }
    public String getPayee()                                         { return fields.get(KEY_PAYEE); }
    public String getSettlementInstitutionIdentificationCode()       { return fields.get(KEY_SETTLEMENT_INSTITUTION_IDENTIFICATION_CODE); }
    public String getReceivingInstitutionIdentificationCode()        { return fields.get(KEY_RECEIVING_INSTITUTION_IDENTIFICATION_CODE); }
    public String getFileName()                                      { return fields.get(KEY_FILE_NAME); }
    public String getAccountIdentification1()                        { return fields.get(KEY_ACCOUNT_IDENTIFICATION_1); }
    public String getAccountIdentification2()                        { return fields.get(KEY_ACCOUNT_IDENTIFICATION_2); }
    public String getTransactionData()                               { return fields.get(KEY_TRANSACTION_DATA); }
    public String getDoubleLengthDesKey()                            { return fields.get(KEY_DOUBLE_LENGTH_DES_KEY); }
    public String getFleetServiceData()                              { return fields.get(KEY_FLEET_SERVICE_DATA); }
    public String getAdditionalTransactionReferenceData()            { return fields.get(KEY_ADDITIONAL_TRANSACTION_REFERENCE_DATA); }
    public String getIsoUse()                                        { return fields.get(KEY_ISO_USE); }
    public String getEncryptionData()                                { return fields.get(KEY_ENCRYPTION_DATA); }
    public String getAdditionalDataNationalUse2()                    { return fields.get(KEY_ADDITIONAL_DATA_NATIONAL_USE_2); }
    public String getReservedNationalUse()                           { return fields.get(KEY_RESERVED_NATIONAL_USE); }
    public String getReservedNationalUse2()                          { return fields.get(KEY_RESERVED_NATIONAL_USE_2); }
    public String getKeyManagement()                                 { return fields.get(KEY_KEY_MANAGEMENT); }
    public String getAuthorizingAgentIdCode()                        { return fields.get(KEY_AUTHORIZING_AGENT_ID_CODE); }
    public String getAdditionalRecordData()                          { return fields.get(KEY_ADDITIONAL_RECORD_DATA); }
    public String getCryptographicServiceMessage()                   { return fields.get(KEY_CRYPTOGRAPHIC_SERVICE_MESSAGE); }
    public String getInfoText()                                      { return fields.get(KEY_INFO_TEXT); }
    public String getSettlementData()                                { return fields.get(KEY_SETTLEMENT_DATA); }
    public String getIssuerTraceId()                                 { return fields.get(KEY_ISSUER_TRACE_ID); }
    public String getPrivateData()                                   { return fields.get(KEY_PRIVATE_DATA); }
    public String getMessageAuthenticationCode2()                    { return fields.get(KEY_MESSAGE_AUTHENTICATION_CODE_2); }
    public String getNetworkName()                                   { return fields.get(KEY_NETWORK_NAME); }
    public String getPlainTextPCI()                                  { return fields.get(KEY_PLAIN_TEXT_PCI); }
    public String getRejectFlag()                                    { return fields.get(KEY_REJECT_FLAG); }
    public String getTransactionType()                               { return fields.get(KEY_TRANSACTION_TYPE); }
    public String getBinCode()                                       { return fields.get(KEY_BIN_CODE); }

    // ── Escape hatch para subcampos y claves especiales ────────────────────────

    /**
     * Acceso genérico para subcampos (ej: {@code "22.01"}, {@code "48.42"},
     * {@code "ADDITIONAL_AMOUNT_DOUBLE"}, {@code "ECI"}).
     */
    public String get(String key) {
        return fields.get(key);
    }

    public String getOrDefault(String key, String defaultValue) {
        return fields.getOrDefault(key, defaultValue);
    }

    public boolean containsKey(String key) {
        return fields.containsKey(key);
    }

    /** Vista de solo lectura del Map completo. */
    public Map<String, String> asMap() {
        return Collections.unmodifiableMap(fields);
    }
}
