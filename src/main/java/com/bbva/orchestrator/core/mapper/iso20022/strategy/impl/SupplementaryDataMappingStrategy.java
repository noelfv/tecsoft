package com.bbva.orchestrator.core.mapper.iso20022.strategy.impl;

import com.bbva.gateway.dto.iso20022.SupplementaryDataDTO;
import com.bbva.orchestrator.core.dto.ISO8583;
import com.bbva.orchestrator.core.exception.MapperFieldsException;
import com.bbva.orchestrator.core.mapper.iso20022.strategy.SectionMappingStrategy;
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
    public List<SupplementaryDataDTO> mapper(ISO8583 input, Map<String, String> subFields) {

        try {
            List<SupplementaryDataDTO> list = new ArrayList<>();

            // Campos directos del ISO8583
            addIfNotNull(list, "networkManagementInfoCode", input.getNetworkManagementInformationCode());
            addIfNotNull(list, "keyManagement", input.getKeyManagement());
            addIfNotNull(list, "settlementData", input.getSettlementData());
            addIfNotNull(list, "issuerTraceId", input.getIssuerTraceId());
            addIfNotNull(list, "dateConversion", input.getDateConversion());
            addIfNotNull(list, "pointServicePIN", input.getPointServicePIN());
            addIfNotNull(list, "paymentAccountData", input.getPaymentAccountData());
            addIfNotNull(list, "serviceIndicator", input.getServiceIndicator());
            addIfNotNull(list, "messageSecurityCode", input.getMessageSecurityCode());
            addIfNotNull(list, "transactionData", input.getTransactionData());
            addIfNotNull(list, "additionalDataNationalUse", input.getAdditionalDataNationalUse());
            addIfNotNull(list, "authorizingAgentIdCode", input.getAuthorizingAgentIdCode());
            addIfNotNull(list, "amountCardholderBillingFee", input.getAmountCardholderBillingFee());
            addIfNotNull(list, "primaryAccountNumberCountryCode", input.getPrimaryAccountNumberCountryCode());
            addIfNotNull(list, "forwardingInstitutionCountryCode", input.getForwardingInstitutionCountryCode());
            addIfNotNull(list, "networkInternationalId", input.getNetworkInternationalId());
            addIfNotNull(list, "authorizationIdResponseLength", input.getAuthorizationIdResponseLength());
            addIfNotNull(list, "amountSettlementFee", input.getAmountSettlementFee());
            addIfNotNull(list, "amountTransactionProcessingFee", input.getAmountTransactionProcessingFee());
            addIfNotNull(list, "amountSettlementProcessingFee", input.getAmountSettlementProcessingFee());
            addIfNotNull(list, "primaryAccountNumberExtended", input.getPrimaryAccountNumberExtended());
            addIfNotNull(list, "trackThreeData", input.getTrackThreeData());
            addIfNotNull(list, "expandedAdditionalAmounts", input.getExpandedAdditionalAmounts());
            addIfNotNull(list, "additionalDataNationalUse2", input.getAdditionalDataNationalUse2());
            addIfNotNull(list, "messageAuthenticationCode", input.getMessageAuthenticationCode());
            addIfNotNull(list, "settlementCode", input.getSettlementCode());
            addIfNotNull(list, "extendedPaymentCode", input.getExtendedPaymentCode());
            addIfNotNull(list, "receivingInstitutionCountryCode", input.getReceivingInstitutionCountryCode());
            addIfNotNull(list, "settlementInstitutionCountryCode", input.getSettlementInstitutionCountryCode());
            addIfNotNull(list, "messageNumber", input.getMessageNumber());
            addIfNotNull(list, "messageNumberLast", input.getMessageNumberLast());
            addIfNotNull(list, "dateAction", input.getDateAction());
            addIfNotNull(list, "creditsNumber", input.getCreditsNumber());
            addIfNotNull(list, "creditsReversalNumber", input.getCreditsReversalNumber());
            addIfNotNull(list, "debitsNumber", input.getDebitsNumber());
            addIfNotNull(list, "debitsReversalNumber", input.getDebitsReversalNumber());
            addIfNotNull(list, "transferNumber", input.getTransferNumber());
            addIfNotNull(list, "transferReversalNumber", input.getTransferReversalNumber());
            addIfNotNull(list, "inquiriesNumber", input.getInquiriesNumber());
            addIfNotNull(list, "authorizationNumber", input.getAuthorizationNumber());
            addIfNotNull(list, "creditsProcessingFeeAmount", input.getCreditsProcessingFeeAmount());
            addIfNotNull(list, "creditsTransactionFeeAmount", input.getCreditsTransactionFeeAmount());
            addIfNotNull(list, "debitsProcessingFeeAmount", input.getDebitsProcessingFeeAmount());
            addIfNotNull(list, "debitsTransactionFeeAmount", input.getDebitsTransactionFeeAmount());
            addIfNotNull(list, "creditsAmount", input.getCreditsAmount());
            addIfNotNull(list, "creditsReversalAmount", input.getCreditsReversalAmount());
            addIfNotNull(list, "debitsAmount", input.getDebitsAmount());
            addIfNotNull(list, "debitsReversalAmount", input.getDebitsReversalAmount());
            addIfNotNull(list, "fileUpdateCode", input.getFileUpdateCode());
            addIfNotNull(list, "fileSecurityCode", input.getFileSecurityCode());
            addIfNotNull(list, "responseIndicator", input.getResponseIndicator());
            addIfNotNull(list, "replacementAmounts", input.getReplacementAmounts());
            addIfNotNull(list, "amountNetSettlement", input.getAmountNetSettlement());
            addIfNotNull(list, "payee", input.getPayee());
            addIfNotNull(list, "settlementInstitutionIdentificationCode", input.getSettlementInstitutionIdentificationCode());
            addIfNotNull(list, "receivingInstitutionIdentificationCode", input.getReceivingInstitutionIdentificationCode());
            addIfNotNull(list, "fileName", input.getFileName());
            addIfNotNull(list, "accountIdentification1", input.getAccountIdentification1());
            addIfNotNull(list, "accountIdentification2", input.getAccountIdentification2());
            addIfNotNull(list, "fleetServiceData", input.getFleetServiceData());
            addIfNotNull(list, "additionalTransactionReferenceData", input.getAdditionalTransactionReferenceData());
            addIfNotNull(list, "isoUse", input.getIsoUse());
            addIfNotNull(list, "reservedNationalUse", input.getReservedNationalUse());
            addIfNotNull(list, "reservedNationalUse2", input.getReservedNationalUse2());
            addIfNotNull(list, "privateData", input.getPrivateData());
            addIfNotNull(list, "additionalRecordData", input.getAdditionalRecordData());
            addIfNotNull(list, "messageAuthenticationCode2", input.getMessageAuthenticationCode2());

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