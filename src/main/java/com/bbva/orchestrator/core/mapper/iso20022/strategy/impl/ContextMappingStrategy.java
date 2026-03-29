package com.bbva.orchestrator.core.mapper.iso20022.strategy.impl;

import com.bbva.gateway.dto.iso20022.*;
import com.bbva.orchestrator.core.dto.ISO8583;
import com.bbva.orchestrator.core.enums.CardDataEntryMode;
import com.bbva.orchestrator.core.enums.CardholderVerificationCapability;
import com.bbva.orchestrator.core.exception.MapperFieldsException;
import com.bbva.orchestrator.core.mapper.iso20022.strategy.SectionMappingResponseStrategy;
import com.bbva.orchestrator.core.mapper.iso20022.strategy.SectionMappingStrategy;
import com.bbva.orchestrator.core.utils.FieldUtil;
import com.bbva.orchestrator.core.utils.MapperUtil;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ContextMappingStrategy implements SectionMappingStrategy<ContextDTO> , SectionMappingResponseStrategy<ContextDTO> {

    private final MapperUtil mapperUtil;

    public ContextMappingStrategy(MapperUtil mapperUtil) {
        this.mapperUtil = mapperUtil;
    }

    @Override
    public ContextDTO mapper(ISO8583 input, Map<String, String> subFields) {

        try{
            // === TRANSACTION CONTEXT ===

            Boolean isECommerceIndicator= mapperUtil.channelECommerceIndicator(input.getNetworkName(), subFields, input.getPointServiceConditionCode());
            String channelTPVIndicator = mapperUtil.channelTPVIndicator(input, subFields);
            String dateValue = input.getAmountTransactionProcessingFee();

            ReconciliationDTO reconciliation = ReconciliationDTO.builder()
                    .date(mapperUtil.validValue(dateValue))
                    .build();

            SettlementServiceDatesDTO settlementServiceDates = SettlementServiceDatesDTO.builder()
                    .settlementDate(input.getSettlementDate())
                    .build();

            SettlementServiceDTO settlementService = SettlementServiceDTO.builder()
                    .settlementServiceDates(settlementServiceDates)
                    .build();

            String cardDataEntryModeValue = CardDataEntryMode.convertCardDataEntryMode(
                    subFields.getOrDefault("22.01",null));

            String entryMode = mapperUtil.entryModeValue(input, subFields, cardDataEntryModeValue);

            String operationType = mapperUtil.operationTypeValue(
                    FieldUtil.isNullOrEmptySubstring(input.getProcessingCode(), 0, 2)
            );

            String channel = mapperUtil.channelValue(
                    isECommerceIndicator,
                    channelTPVIndicator,
                    input.getPosTerminalData()
            );

            String owner = mapperUtil.ownerValue(channel);

            List<AdditionalDataDTO> transactionContextAdditionalData = List.of(
                    AdditionalDataDTO.builder()
                            .key("ENTRY_MODE")
                            .value(entryMode)
                            .build(),
                    AdditionalDataDTO.builder()
                            .key("OPERATION_TYPE")
                            .value(operationType)
                            .build(),
                    AdditionalDataDTO.builder()
                            .key("CHANNEL")
                            .value(channel)
                            .build(),
                    AdditionalDataDTO.builder()
                            .key("OWNER")
                            .value(owner)
                            .build()
            );

            String captureDateValue = input.getCaptureDate();

            TransactionContextDTO transactionContext = TransactionContextDTO.builder()
                    .merchantCategoryCode(input.getMerchantType())
                    .merchantCategorySpecificData("NATIONAL")
                    .reconciliation(reconciliation)
                    .settlementService(settlementService)
                    .captureDate(mapperUtil.validValue(captureDateValue))
                    .additionalData(transactionContextAdditionalData)
                    .build();

            // === POINT OF SERVICE CONTEXT ===

            Map<String, String> posCardHolderPresence = CardholderVerificationCapability.mapPointOfServiceContext_CardDataEntryMode(
                    subFields.getOrDefault("61.04",null)
            );

            //TODO Validar este campo ya que se usa de manera provisional para guardar el valor original del campo 22
            // === FIELD CAMPO 22 ===
            AdditionalDataDTO pointServiceEntryMode = AdditionalDataDTO.builder()
                    .key("UNKNOWN")
                    .value(input.getPointServiceEntryMode())
                    .build();

            PointOfServiceContextDTO pointOfServiceContext = PointOfServiceContextDTO.builder()
                    .cardDataEntryMode(cardDataEntryModeValue)
                    .ecommerceIndicator(CardholderVerificationCapability.mapPointOfServiceContext_EcommerceIndicator(
                            subFields.getOrDefault("61.04",null),isECommerceIndicator))
                    .attendedIndicator(CardholderVerificationCapability.mapPointOfServiceContext_AttendedIndicator(
                            subFields.getOrDefault("61.01",null)))
                    .unattendedLevelCategory(CardholderVerificationCapability.mapPointOfServiceContext_UnattendedLevelCategory(
                            subFields.getOrDefault("61.01",null), subFields.getOrDefault("61.10",null)))
                    .cardholderPresent(mapperUtil.safeBooleanValueOf(
                            posCardHolderPresence.getOrDefault("cardholderPresent",null)))
                    .motoCode(posCardHolderPresence.getOrDefault("MOTOCode",null))
                    .cardPresent(CardholderVerificationCapability.mapPointOfServiceContext_CardPresent(
                            subFields.getOrDefault("61.05",null)))
                    .additionalData(List.of(pointServiceEntryMode))
                    .build();

            // === VERIFICATION ===
            PINDataDTO pinData = PINDataDTO.builder()
                    .encryptedPINBlock(input.getPinData())
                    .build();

            ValueDTO value = ValueDTO.builder()
                    .pinData(pinData)
                    .build();

            VerificationInformationDTO verificationInfo = VerificationInformationDTO.builder()
                    .key("CVC")
                    .value(value)
                    .build();

            ValueDTO valueCVC2 = ValueDTO.builder()
                    .textValue(subFields.getOrDefault("48.92",null))
                    .build();

            //CAMPO 48 SUBCAMPO 92
            VerificationInformationDTO verificationInfoCVC2 = VerificationInformationDTO.builder()
                    .key("CVV2")
                    .value(valueCVC2)
                    .build();


            ResultDetailsDTO resultDetails = ResultDetailsDTO.builder()
                    //.key("CVC")
                    .key("PENDING")
                    //.value(subFields.getOrDefault("48.87",null))
                    .value("")
                    .build();

            VerificationResultDTO verificationResult = VerificationResultDTO.builder()
                    .key("card_validation_code_result")
                    .resultDetails(List.of(resultDetails))
                    .build();

            VerificationDTO verification = VerificationDTO.builder()
                    .verificationInformation(List.of(verificationInfo,verificationInfoCVC2))
                    .verificationResult(List.of(verificationResult))
                    .build();

            List<VerificationDTO> verificationList = List.of(verification);

            // === SALE CONTEXT ===
            AdditionalDataDTO campaignData = AdditionalDataDTO.builder()
                    .key("campaignData")
                    .value(input.getCampaignData())
                    .build();

            SaleContextDTO saleContext = SaleContextDTO.builder()
                    .additionalData(List.of(campaignData))
                    .build();

            return ContextDTO.builder()
                    .transactionContext(transactionContext)
                    .pointOfServiceContext(pointOfServiceContext)
                    .verification(verificationList)
                    .saleContext(saleContext)
                    .build();

        } catch (RuntimeException e) {
            // Manejo de excepciones, puedes lanzar una RuntimeException o una excepción personalizada
            throw new MapperFieldsException("PGWP-00121", "Error al mapear ContextDTO desde ISO8583", e);
        }
    }

    @Override
    public Map<String, String> unMapper(String networkName,ContextDTO input) {
        Map<String, String> mapValues = new HashMap<>();

        TransactionContextDTO transactionContext = input.getTransactionContext();
        PointOfServiceContextDTO posContext = input.getPointOfServiceContext();
        SaleContextDTO saleContext = input.getSaleContext();

        // --- TransactionContext ---
        mapValues.put("amountTransactionFee", mapperUtil.safeSubstring(mapperUtil.getFieldValue(transactionContext.getReconciliation(), ReconciliationDTO::getDate, DEFAULT_EMPTY_VALUE),0,9) );
        mapValues.put("settlementDate", mapperUtil.getFieldValue(transactionContext.getSettlementService().getSettlementServiceDates(), SettlementServiceDatesDTO::getSettlementDate, DEFAULT_EMPTY_VALUE));
        mapValues.put("captureDate", mapperUtil.getFieldValue(transactionContext, TransactionContextDTO::getCaptureDate, DEFAULT_EMPTY_VALUE));
        mapValues.put("merchantType", mapperUtil.getFieldValue(transactionContext, TransactionContextDTO::getMerchantCategoryCode, DEFAULT_EMPTY_VALUE));

        // --- Point Service Context ---
        //TODO Recuperar el valor original del campo 22
        // === FIELD CAMPO 22 ===
        String pointServiceEntryMode = posContext.getAdditionalData().stream()
                .filter(dto -> "UNKNOWN".equals(dto.getKey()))
                .map(AdditionalDataDTO::getValue)
                .findFirst()
                .orElse(DEFAULT_EMPTY_VALUE);
        mapValues.put("pointServiceEntryMode", pointServiceEntryMode);

        // --- Sale Context ---
        String campaignData = saleContext.getAdditionalData().stream()
                .filter(dto -> "campaignData".equals(dto.getKey()))
                .map(AdditionalDataDTO::getValue)
                .findFirst()
                .orElse(DEFAULT_EMPTY_VALUE);
        mapValues.put("campaignData", campaignData);

        // --- Verification ---
        String pinData = input.getVerification().stream().findFirst()
                .map(VerificationDTO::getVerificationInformation).flatMap(list -> list.stream().findFirst())
                .map(VerificationInformationDTO::getValue)
                .map(ValueDTO::getPinData)
                .map(PINDataDTO::getEncryptedPINBlock)
                .orElse(DEFAULT_EMPTY_VALUE);
        mapValues.put("pinData", pinData);

        return mapValues;
    }

    @Override
    public ContextDTO mapperResponse(ISO8583 input) {
        TransactionContextDTO transactionContextVuelta = TransactionContextDTO.builder()
                .transactionInitiator("0000")
                .merchantCategoryCode(input.getMerchantType())
                .build();

        return ContextDTO.builder()
                .transactionContext(transactionContextVuelta)
                .build();
    }
}