package com.bbva.orchestrator.core.mapper.iso20022.strategy.impl;

import com.bbva.gateway.dto.iso20022.SupplementaryDataDTO;
import com.bbva.orchestrator.core.exception.MapperFieldsException;
import com.bbva.orchestrator.core.mapper.iso20022.strategy.SectionMappingStrategy;
import com.bbva.orchestrator.core.mapper.model.CanonicalFields;
import com.bbva.orchestrator.core.utils.MapperUtil;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SupplementaryDataMappingStrategy implements SectionMappingStrategy<List<SupplementaryDataDTO>> {

    private final MapperUtil mapperUtil;

    public SupplementaryDataMappingStrategy(MapperUtil mapperUtil) {
        this.mapperUtil = mapperUtil;
    }

    @Override
    public List<SupplementaryDataDTO> mapper(CanonicalFields fields) {

        try {
            List<SupplementaryDataDTO> list = new ArrayList<>();

            addIfNotNull(list, "networkManagementInfoCode", fields.getNetworkManagementInformationCode());
            addIfNotNull(list, "keyManagement", fields.getKeyManagement());
            addIfNotNull(list, "settlementData", fields.getSettlementData());
            addIfNotNull(list, "issuerTraceId", fields.getIssuerTraceId());
            addIfNotNull(list, "dateConversion", fields.getDateConversion());
            addIfNotNull(list, "pointServicePIN", fields.getPointServicePIN());
            addIfNotNull(list, "paymentAccountData", fields.getPaymentAccountData());
            addIfNotNull(list, "serviceIndicator", fields.getServiceIndicator());
            addIfNotNull(list, "messageSecurityCode", fields.getMessageSecurityCode());
            addIfNotNull(list, "transactionData", fields.getTransactionData());
            addIfNotNull(list, "additionalDataNationalUse", fields.getAdditionalDataNationalUse());
            addIfNotNull(list, "authorizingAgentIdCode", fields.getAuthorizingAgentIdCode());
            addIfNotNull(list, "amountCardholderBillingFee", fields.getAmountCardholderBillingFee());
            addIfNotNull(list, "primaryAccountNumberCountryCode", fields.getPrimaryAccountNumberCountryCode());
            addIfNotNull(list, "forwardingInstitutionCountryCode", fields.getForwardingInstitutionCountryCode());
            addIfNotNull(list, "networkInternationalId", fields.getNetworkInternationalId());
            addIfNotNull(list, "authorizationIdResponseLength", fields.getAuthorizationIdResponseLength());
            addIfNotNull(list, "amountSettlementFee", fields.getAmountSettlementFee());
            addIfNotNull(list, "amountTransactionProcessingFee", fields.getAmountTransactionProcessingFee());
            addIfNotNull(list, "amountSettlementProcessingFee", fields.getAmountSettlementProcessingFee());
            addIfNotNull(list, "primaryAccountNumberExtended", fields.getPrimaryAccountNumberExtended());
            addIfNotNull(list, "trackThreeData", fields.getTrackThreeData());
            addIfNotNull(list, "expandedAdditionalAmounts", fields.getExpandedAdditionalAmounts());
            addIfNotNull(list, "additionalDataNationalUse2", fields.getAdditionalDataNationalUse2());
            addIfNotNull(list, "messageAuthenticationCode", fields.getMessageAuthenticationCode());
            addIfNotNull(list, "settlementCode", fields.getSettlementCode());
            addIfNotNull(list, "extendedPaymentCode", fields.getExtendedPaymentCode());
            addIfNotNull(list, "receivingInstitutionCountryCode", fields.getReceivingInstitutionCountryCode());
            addIfNotNull(list, "settlementInstitutionCountryCode", fields.getSettlementInstitutionCountryCode());
            addIfNotNull(list, "messageNumber", fields.getMessageNumber());
            addIfNotNull(list, "messageNumberLast", fields.getMessageNumberLast());
            addIfNotNull(list, "dateAction", fields.getDateAction());
            addIfNotNull(list, "creditsNumber", fields.getCreditsNumber());
            addIfNotNull(list, "creditsReversalNumber", fields.getCreditsReversalNumber());
            addIfNotNull(list, "debitsNumber", fields.getDebitsNumber());
            addIfNotNull(list, "debitsReversalNumber", fields.getDebitsReversalNumber());
            addIfNotNull(list, "transferNumber", fields.getTransferNumber());
            addIfNotNull(list, "transferReversalNumber", fields.getTransferReversalNumber());
            addIfNotNull(list, "inquiriesNumber", fields.getInquiriesNumber());
            addIfNotNull(list, "authorizationNumber", fields.getAuthorizationNumber());
            addIfNotNull(list, "creditsProcessingFeeAmount", fields.getCreditsProcessingFeeAmount());
            addIfNotNull(list, "creditsTransactionFeeAmount", fields.getCreditsTransactionFeeAmount());
            addIfNotNull(list, "debitsProcessingFeeAmount", fields.getDebitsProcessingFeeAmount());
            addIfNotNull(list, "debitsTransactionFeeAmount", fields.getDebitsTransactionFeeAmount());
            addIfNotNull(list, "creditsAmount", fields.getCreditsAmount());
            addIfNotNull(list, "creditsReversalAmount", fields.getCreditsReversalAmount());
            addIfNotNull(list, "debitsAmount", fields.getDebitsAmount());
            addIfNotNull(list, "debitsReversalAmount", fields.getDebitsReversalAmount());
            addIfNotNull(list, "fileUpdateCode", fields.getFileUpdateCode());
            addIfNotNull(list, "fileSecurityCode", fields.getFileSecurityCode());
            addIfNotNull(list, "responseIndicator", fields.getResponseIndicator());
            addIfNotNull(list, "replacementAmounts", fields.getReplacementAmounts());
            addIfNotNull(list, "amountNetSettlement", fields.getAmountNetSettlement());
            addIfNotNull(list, "payee", fields.getPayee());
            addIfNotNull(list, "settlementInstitutionIdentificationCode", fields.getSettlementInstitutionIdentificationCode());
            addIfNotNull(list, "receivingInstitutionIdentificationCode", fields.getReceivingInstitutionIdentificationCode());
            addIfNotNull(list, "fileName", fields.getFileName());
            addIfNotNull(list, "accountIdentification1", fields.getAccountIdentification1());
            addIfNotNull(list, "accountIdentification2", fields.getAccountIdentification2());
            addIfNotNull(list, "fleetServiceData", fields.getFleetServiceData());
            addIfNotNull(list, "additionalTransactionReferenceData", fields.getAdditionalTransactionReferenceData());
            addIfNotNull(list, "isoUse", fields.getIsoUse());
            addIfNotNull(list, "reservedNationalUse", fields.getReservedNationalUse());
            addIfNotNull(list, "reservedNationalUse2", fields.getReservedNationalUse2());
            addIfNotNull(list, "privateData", fields.getPrivateData());
            addIfNotNull(list, "additionalRecordData", fields.getAdditionalRecordData());
            addIfNotNull(list, "messageAuthenticationCode2", fields.getMessageAuthenticationCode2());

            return list;

        } catch (RuntimeException e) {
            // Manejo de excepciones, puedes lanzar una RuntimeException o una excepción personalizada
            throw new MapperFieldsException("PGWP-00121", "Error al mapear SupplementaryDataDTO desde ISO8583", e);
        }
    }

    @Override
    public Map<String, String> unMapper(String networkName,List<SupplementaryDataDTO> input) {

        Map<String, String> mapValues = new HashMap<>();

        for (SupplementaryDataDTO dto : input) {
            String key = mapperUtil.getFieldValue(dto, SupplementaryDataDTO::getPlaceAndName, DEFAULT_EMPTY_VALUE);
            String value = mapperUtil.getFieldValue(dto, SupplementaryDataDTO::getEnvelope, DEFAULT_EMPTY_VALUE);

            mapValues.put(key, value);
        }
        return mapValues;
    }

    private void addIfNotNull(List<SupplementaryDataDTO> list, String key, String value) {
        if (value != null && !value.trim().isEmpty()) {
            list.add(SupplementaryDataDTO.builder()
                    .placeAndName(key)
                    .envelope(value)
                    .build());
        }
    }
}