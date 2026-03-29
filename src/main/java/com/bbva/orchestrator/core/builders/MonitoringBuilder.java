package com.bbva.orchestrator.core.builders;

import com.bbva.gateway.dto.iso20022.*;
import com.bbva.gateway.interceptors.GrpcHeadersInfo;
import com.bbva.gateway.utils.LogsTraces;
import com.bbva.orchestrator.configuration.ApplicationDataCache;
import com.bbva.orchestrator.core.dto.ISO8583;
import com.bbva.orchestrator.core.enums.ChannelOperator;
import com.bbva.orchestrator.core.enums.FilterOperator;
import com.bbva.orchestrator.core.enums.TransactionType;
import com.bbva.orchestrator.core.utils.FieldUtil;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.Optional;

@Component
public class MonitoringBuilder {

    private static final Set<String> MTI_OUTPUT = Set.of("0110", "0130", "0410", "0430");
    private static final Set<String> LIST_CODE_APPROVED = Set.of("00", "10", "11","87","08","85","N0");
    private static final String BIN_ADQUIRENTE_P2P = "420829";
    private static final String APPROVED = "Approved"; //Se recibió una respuesta positiva.
    private static final String DENIED = "Denied";//Se recibió una respuesta negativa.
    private final ApplicationDataCache applicationDataCache;


    public MonitoringBuilder(ApplicationDataCache applicationDataCache) {
        this.applicationDataCache = applicationDataCache;
    }

    public MonitoringDTO build(ISO8583 input, TransactionDTO transaction,EnvironmentDTO environment,ContextDTO context) {

        MonitoringDTO monitoring = MonitoringDTO.builder().build();
        String networkName = input.getNetworkName();
        String envAcceptorNameAndLocation = input.getCardAcceptorNameLocation();
        String envAcquirerId = input.getAcquiringInstitutionIdentificationCode();
        String envAcceptorId = input.getCardAcceptorIdentificationCode();
        String merchantCategory = input.getMerchantType();
        try {
            String currentTime = String.valueOf(Instant.now().toEpochMilli());
            if(FieldUtil.requiredProcess(input.getMessageType())){

                String binCode = extractBinCode(input.getPrimaryAccountNumber());
                String merchantName = extractMerchantName(envAcceptorNameAndLocation);
                monitoring.setStartDateMs(currentTime);
                monitoring.setBinCode(binCode);
                monitoring.setBinDescription(applicationDataCache.getBinDescription(networkName, binCode));
                monitoring.setMerchantNameAceptor(merchantName.trim());
                monitoring.setMerchantCategoryDescription(applicationDataCache.getCustomValue("merchant_type", merchantCategory));
                monitoring.setTransactionTypeDescription(TransactionType.getTransactionType(transaction.getTransactionType()));
                monitoring.setCountryDate(getCurrentDatePeru());
                monitoring.setChannelFilter(channelFilterDescription(context.getPointOfServiceContext().getEcommerceIndicator(), environment.getTerminal().getKey()));
                monitoring.setOperationFilter(FilterOperator.getFilterOperator(transaction.getTransactionType()));
                if (BIN_ADQUIRENTE_P2P.equals(envAcquirerId)) {
                    monitoring.setP2pType(envAcceptorNameAndLocation.substring(0, 4));
                    monitoring.setOriginBankCode(envAcceptorId.trim());
                    monitoring.setOriginBankDescription(applicationDataCache.getCustomValue("bank_p2p", monitoring.getOriginBankCode()));
                    monitoring.setMerchantNameAceptor("Visa direct");//TODO revisar y validar que dato podemos colocar en este campo, ya que el nombre del comercio se ennvia los datos del cliente
                    monitoring.setTransactionTypeDescription("BILLETERA");//TODO este dato se deberia calcular a partir del transaction type
                    monitoring.setChannelFilter("P2PP");//TODO este dato se deberia calcular a partir del ecommerce indicator y el terminal key .channelFilterDescription
                }
            } else {
                if (MTI_OUTPUT.contains(input.getMessageType())) {
                    monitoring.setEndDateMs(currentTime);
                    monitoring.setIsNextGen(false);

                    if(LIST_CODE_APPROVED.contains(input.getResponseCode())){
                        monitoring.setTransactionStatus(APPROVED);
                    }else{
                        monitoring.setTransactionStatus(DENIED);
                    }
                } else {
                    LogsTraces.writeInfo("No requiere generar bloque monitoreo messageType= "  + input.getMessageType());
                    //TODO Validarlo en el tiempo, ya que se seteamos este valor para controlar el nullpointer para mensajes que no se estan monitoreando
                    monitoring.setIsNextGen(false);
                    return monitoring;
                }
            }
            return monitoring;
        } catch (RuntimeException e) {
            LogsTraces.writeError("Error creating monitoring: " + e.getMessage());
            return monitoring;
        }
    }


    private  String extractBinCode(String primaryAccountNumber) {
        String resultBinCode = "000000";
        try {
            resultBinCode = primaryAccountNumber.substring(0, 6);
            return resultBinCode;
        } catch (Exception e) {
            LogsTraces.writeWarning("Error getting the binCode from the field primaryAccountNumber  " +e.getMessage());
            return resultBinCode;
        }
    }

    private static String extractMerchantName(String nameLocation) {
        String resultNameLocation = "";
        try {
            resultNameLocation = nameLocation.substring(0, 22);
        } catch (Exception e) {
            resultNameLocation = "MerchantName-NotFound";
        }
        return resultNameLocation;
    }

    public static String channelFilterDescription(Boolean eCommerceIndicator, String terminalKey) {
        if (Boolean.TRUE.equals(eCommerceIndicator)) {
            return "ECOMMER";
        }
        String channel = ChannelOperator.getChannelOperator(terminalKey);
        return Optional.ofNullable(channel).orElse("OTHER");
    }

    public static String getCurrentDatePeru() {

        ZonedDateTime horaActualPeru = ZonedDateTime.now(ZoneId.of("America/Lima"));
        DateTimeFormatter formateador = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

        return horaActualPeru.format(formateador)+"-05:00";
    }
}
