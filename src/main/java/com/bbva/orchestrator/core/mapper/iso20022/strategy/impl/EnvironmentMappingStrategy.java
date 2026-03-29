package com.bbva.orchestrator.core.mapper.iso20022.strategy.impl;

import com.bbva.gateway.dto.iso20022.*;
import com.bbva.orchestrator.core.enums.CardholderVerificationCapability;
import com.bbva.orchestrator.core.exception.MapperFieldsException;
import com.bbva.orchestrator.core.mapper.iso20022.strategy.SectionMappingResponseStrategy;
import com.bbva.orchestrator.core.mapper.iso20022.strategy.SectionMappingStrategy;
import com.bbva.orchestrator.core.mapper.model.CanonicalFields;
import com.bbva.orchestrator.core.utils.MapperUtil;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class EnvironmentMappingStrategy implements SectionMappingStrategy<EnvironmentDTO>, SectionMappingResponseStrategy<EnvironmentDTO> {

    private final MapperUtil mapperUtil;
    
    public EnvironmentMappingStrategy(MapperUtil mapperUtil) {
        this.mapperUtil = mapperUtil;
    }

    @Override
    public EnvironmentDTO mapper(CanonicalFields fields) {

        try {

            // ======== FIELD 35 (TRACK 2 DATA) ========
            Track2DTO track2 = Track2DTO
                    .builder()
                    .textValue(fields.getTrackTwoData())
                    .build();

           // ======== FIELD 14 (EXPIRATION DATE) ========
            String dateExpiration = mapperUtil.convertFormatExpiryDate(fields.getDateExpiration());

            CardDTO card = CardDTO.builder()
                    // ======== FIELD 23 (CARD SEQUENCE NUMBER) ========
                    .cardSequenceNumber(fields.getCardSequenceNumber())
                    // ======== FIELD 14 (EXPIRATION DATE) ========
                    .expiryDate(dateExpiration)
                    // ======== FIELD 2 (PAN) ========
                    .pan(fields.getPrimaryAccountNumber())
                    // ======== FIELD 40 (SERVICE RESTRICTION CODE) ========
                    .serviceCode(fields.getServiceRestrictionCode())
                    // ======== FIELD 45 (TRACK 1 DATA) ========
                    .track1(fields.getTrackOneData())
                    .track2(track2)
                    .build();

            CardholderVerificationCapabilityDTO cvCapability = CardholderVerificationCapabilityDTO.builder()
                    .capability(CardholderVerificationCapability.convertCardholderVerificationCapability(fields.getOrDefault("22.02",null)))
                    .build();

            CardReadingCapabilityDTO cardReadingCapability = CardReadingCapabilityDTO.builder()
                    .capability(CardholderVerificationCapability.mapCardReadingCapability_Capability(
                                    fields.getOrDefault("61.11", null))
                                    .getOrDefault("capability",null))
                    .build();

            CapabilitiesDTO capabilities = CapabilitiesDTO.builder()
                    .cardholderVerificationCapabilities(List.of(cvCapability))
                    .cardCaptureCapable(CardholderVerificationCapability.mapCapabilities_CardCaptureCapable(
                            fields.getOrDefault("61.06", null)))
                    .cardReadingCapabilities(List.of(cardReadingCapability))
                    .build();

            TerminalIdDTO terminalId = TerminalIdDTO.builder()
                    // ======== FIELD 41 (CARD ACCEPTOR TERMINAL IDENTIFICATION) ========
                    .id(fields.getCardAcceptorTerminalIdentification())
                    // ======== FIELD 60 (POS TERMINAL DATA) ========
                    .assigner(fields.getPosTerminalData())
                    // ======== FIELD 61_13 (POS COUNTRY CODE) ========
                    .country(fields.getOrDefault("61.13", null))
                    .build();

            // ... resto del mapeo
            String type = mapperUtil.channelTPVIndicator(fields);

            Map<String,String> posTerminalLocation = CardholderVerificationCapability.mapPosTerminalLocation(
                    fields.getOrDefault("61.03", null),
                    fields.getOrDefault("61.02", null), type);

            TerminalDTO terminal = TerminalDTO.builder()
                    .capabilities(capabilities)
                    .terminalId(terminalId)
                    .key(type)
                    .otherType(posTerminalLocation.getOrDefault("OtherType",null))
                    .offPremisesIndicator(mapperUtil.safeBooleanValueOf(
                            posTerminalLocation.getOrDefault("offPremisesIndicator",null)
                    ))
                    .geographicLocation(posTerminalLocation.getOrDefault("GeographicLocation",null))
                    .build();

            // ======== FIELD 62 (MAPPED AS POSTAL CODE) ========
            AdditionalIdDTO postalCodeData = AdditionalIdDTO.builder()
                    .key("postalCode")
                    .value(fields.getPostalCode())
                    .build();

            AcquirerDTO acquirer = AcquirerDTO.builder()
                    // ======== FIELD 32 (ACQUIRING INSTITUTION IDENTIFICATION CODE) ========
                    .id(fields.getAcquiringInstitutionIdentificationCode())
                    .additionalId(postalCodeData)
                    // ======== FIELD 19 (ACQUIRING INSTITUTION COUNTRY CODE) ========
                    .country(fields.getAcquirerCountryCode())
                    .build();


            // ======== FIELD 48 (ADDITIONAL DATA RETAILER) ========
            AdditionalIdDTO additionalDataRetailer = AdditionalIdDTO.builder()
                    .key("additionalDataRetailer")
                    .value(fields.getAdditionalDataRetailer())
                    .build();

            SenderDTO sender = SenderDTO.builder()
                    // ======== FIELD 33 (FORWARDING INSTITUTION IDENTIFICATION CODE) ========
                    .id(fields.getForwardingInstitutionIdentificationCode())
                    .additionalId(additionalDataRetailer)
                    .build();

            AddressDTO address = AddressDTO.builder()
                    .postalCode(fields.getOrDefault("61.14", null))
                    .build();

            LocalDataDTO localData = LocalDataDTO.builder()
                    .address(address)
                    .build();

            AcceptorDTO acceptor = AcceptorDTO.builder()
                    // ======== FIELD 42 (CARD ACCEPTOR IDENTIFICATION CODE) ========
                    .id(fields.getCardAcceptorIdentificationCode())
                    // ======== FIELD 43 (CARD ACCEPTOR NAME AND LOCATION) ========
                    .nameAndLocation(fields.getCardAcceptorNameLocation())
                    // ======== FIELD 61_14 (POS Postal Code (or Sub-Merchant Information, if applicable)) ========
                    .localData(localData)
                    .build();

            IssuerDTO issuer = IssuerDTO.builder()
                    // ======== FIELD 61 (POS CARD ISSUER / OTHER AMOUNTS) ========
                    .assigner(fields.getPosCardIssuer())
                    .build();

            if(!Set.of("0110","0130","0410","0430","0210","0810","0312","0800").contains(fields.getMessageType())){
                return EnvironmentDTO.builder()
                        .card(card)
                        .terminal(terminal)
                        .acquirer(acquirer)
                        .sender(sender)
                        .acceptor(acceptor)
                        .issuer(issuer)
                        .build();
            }else{
                return EnvironmentDTO.builder()
                        .card(card)
                        .acquirer(acquirer)
                        .sender(sender)
                        .issuer(issuer)
                        .build();
            }

        } catch (RuntimeException e) {
            // Manejo de excepciones, puedes lanzar una RuntimeException o una excepción personalizada
            throw new MapperFieldsException("PGWP-00121", "Error al mapear EnvironmentDTO desde ISO8583", e);
        }
    }

    @Override
    public Map<String, String> unMapper(String networkName,EnvironmentDTO env) {
        Map<String, String> resultMap = new HashMap<>();

        CardDTO card = env.getCard();
        TerminalDTO terminal = env.getTerminal();
        AcquirerDTO acquirer = env.getAcquirer();
        SenderDTO sender = env.getSender();
        AcceptorDTO acceptor = env.getAcceptor();
        IssuerDTO issuer = env.getIssuer();

        // Card Information
        // ======== FIELD 14 (EXPIRATION DATE) ========
        String dateExpiration = mapperUtil.reConvertFormatExpiryDate(card.getExpiryDate());

        resultMap.put("primaryAccountNumber", mapperUtil.getFieldValue(card, CardDTO::getPan, DEFAULT_EMPTY_VALUE));
        resultMap.put("dateExpiration", dateExpiration); //El formato de fecha debe ser MMYY pero regresa como YYYY-MM
        resultMap.put("cardSequenceNumber", mapperUtil.getFieldValue(card, CardDTO::getCardSequenceNumber, DEFAULT_EMPTY_VALUE));
        resultMap.put("trackOneData", mapperUtil.getFieldValue(card, CardDTO::getTrack1, DEFAULT_EMPTY_VALUE));
        resultMap.put("trackTwoData", mapperUtil.getFieldValue(card.getTrack2(), Track2DTO::getTextValue, DEFAULT_EMPTY_VALUE));

        // Terminal Information
        resultMap.put("cardAcceptorTerminalIdentification", mapperUtil.getFieldValue(terminal.getTerminalId(), TerminalIdDTO::getId, DEFAULT_EMPTY_VALUE));
        resultMap.put("posTerminalData", mapperUtil.getFieldValue(terminal.getTerminalId(), TerminalIdDTO::getAssigner, DEFAULT_EMPTY_VALUE));

        // Acquirer Information
        resultMap.put("acquiringInstitutionIdentificationCode", mapperUtil.getFieldValue(acquirer, AcquirerDTO::getId, DEFAULT_EMPTY_VALUE));
        resultMap.put("acquirerCountryCode", mapperUtil.getFieldValue(acquirer, AcquirerDTO::getCountry, DEFAULT_EMPTY_VALUE));
        resultMap.put("postalCode", mapperUtil.getAdditionalDataValue(acquirer.getAdditionalId(), "postalCode", DEFAULT_EMPTY_VALUE));

        // Sender Information
        // ======== FIELD 33 (FORWARDING INSTITUTION IDENTIFICATION CODE) ========
        resultMap.put("forwardingInstitutionIdentificationCode", mapperUtil.getFieldValue(sender, SenderDTO::getId, DEFAULT_EMPTY_VALUE));
        // ======== FIELD 48 (ADDITIONAL DATA RETAILER) ========
        resultMap.put("additionalDataRetailer", mapperUtil.getAdditionalDataValue(sender.getAdditionalId(), "additionalDataRetailer", DEFAULT_EMPTY_VALUE));

        // Acceptor Information
        resultMap.put("cardAcceptorIdentificationCode", mapperUtil.getFieldValue(acceptor, AcceptorDTO::getId, DEFAULT_EMPTY_VALUE));
        resultMap.put("cardAcceptorNameLocation", mapperUtil.getFieldValue(acceptor, AcceptorDTO::getNameAndLocation, DEFAULT_EMPTY_VALUE));

        // Issuer Information
        resultMap.put("posCardIssuer", mapperUtil.getFieldValue(issuer, IssuerDTO::getAssigner, DEFAULT_EMPTY_VALUE));

        return resultMap;
    }

    @Override
    public EnvironmentDTO mapperResponse(CanonicalFields fields) {
        CardDTO card = CardDTO.builder()
                .pan(fields.getPrimaryAccountNumber())
                .build();
        return EnvironmentDTO.builder()
                .card(card)
                .build();
    }
}