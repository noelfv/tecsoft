package com.bbva.orchestrator.core.mapper.factory.impl;

import com.bbva.gateway.dto.iso20022.*;
import com.bbva.gateway.interceptors.GrpcHeadersInfo;
import com.bbva.gateway.utils.LogsTraces;
import com.bbva.orchestrator.core.enums.MessageFunction;
import com.bbva.orchestrator.core.exception.MapperFieldsException;
import com.bbva.orchestrator.core.mapper.iso20022.strategy.impl.*;
import com.bbva.orchestrator.core.dto.ISO8583;
import com.bbva.orchestrator.core.mapper.factory.ISO20022DelegateMapper;
import com.bbva.orchestrator.core.builders.MonitoringBuilder;
import com.bbva.orchestrator.core.utils.MapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DefaultDelegateMapper implements ISO20022DelegateMapper {

    private final MapperUtil mapperUtil;
    private final EnvironmentMappingStrategy environmentStrategy;
    private final TransactionMappingStrategy transactionStrategy;
    private final ContextMappingStrategy contextStrategy;
    private final SupplementaryDataMappingStrategy supplementaryDataStrategy;
    private final SecurityTrailerMappingStrategy securityTrailerStrategy;
    private final ProtectedDataMappingStrategy protectedDataStrategy;
    private final TraceDataMappingStrategy traceDataStrategy;
    private final ProcessingResultMappingStrategy processingResultMappingStrategy;
    private final AddendumDataMappingStrategy addendumDataStrategy;
    private final CustomDataLocalMappingStrategy customDataLocalStrategy;
    private final MonitoringBuilder monitoringService;


    /**
     * Mapea un objeto ISO8583 a un objeto ISO20022.
     * Utiliza estrategias de mapeo para convertir cada parte del mensaje.
     *
     * @param input El objeto ISO8583 a mapear.
     * @param subFields Campos adicionales que pueden ser necesarios para el mapeo.
     * @return Un objeto ISO20022 construido con los datos del mensaje ISO8583.
     */
    @Override
    public ISO20022 mapper(ISO8583 input, Map<String, String> subFields) {
        try {
            //TODO Si typeMessage es de respuesta solo seria necesaria crear el objeto processingResult ya que  es un flujo host
            //Colocar todos los mensajes que son devueltos por HOST como condicion
            if(isResponseHost(input.getMessageType())){
                return createISO20022ResponseFromHost(input,subFields);
            }

            LogsTraces.writeInfo("requestMessage %s".formatted(input.getPlainTextPCI()));

            // Mapeo paralelo (opcional, si necesitas alto rendimiento)
            EnvironmentDTO environment = environmentStrategy.mapper(input, subFields);
            TransactionDTO transaction = transactionStrategy.mapper(input, subFields);
            ContextDTO context = contextStrategy.mapper(input, subFields);
            List<SupplementaryDataDTO> supplementaryData = supplementaryDataStrategy.mapper(input, subFields);
            List<TraceDataDTO> traceData = traceDataStrategy.mapper(input, subFields);
            List<ProtectedDataDTO> protectedData = protectedDataStrategy.mapper(input, subFields);
            SecurityTrailerDTO securityTrailer = securityTrailerStrategy.mapper(input, subFields);
            AddendumDataDTO addendumData = addendumDataStrategy.mapper(input, subFields);
            CustomDataLocalDTO customDataLocal = customDataLocalStrategy.mapper(input, subFields);
            MonitoringDTO monitoring=monitoringService.build(input, transaction,environment,context);

            LogsTraces.writeInfo("transactionReference %s".formatted(transaction.getTransactionId().getTransactionReference()));

            // Construir el objeto ISO20022 final usando su builder, solo con objetos no nulos
            ISO20022.ISO20022Builder iso20022Builder = ISO20022.builder()
                    .networkName(input.getNetworkName())
                    .isSimulation(false)//TODO Revisar este valor siempre es false(ecommerce nos indica que sea false)
                    .messageFunction(MessageFunction.convertMessageFunction(input.getMessageType()))
                    .socketPort(GrpcHeadersInfo.getPort())
                    .traceData(traceData)
                    .transaction(transaction)
                    .environment(environment)
                    .addendumData(addendumData)
                    .iccRelatedData(input.getIntegratedCircuitCard())
                    .customDataLocal(customDataLocal)//se deberia eliminar este bloque en el sensitiveData del flowhandler
                    .protocolVersion(mapperUtil.getBinDescription(input.getNetworkName(),input.getBinCode()))
                    .monitoring(monitoring);//Esto se deberia de setear en el mapper de la respuesta

            if (Objects.nonNull(context)) { // Asumiendo que context es un objeto DTO
                iso20022Builder.context(context);
            }
            if (Objects.nonNull(protectedData)) {
                iso20022Builder.protectedData(protectedData);
            }
            if (Objects.nonNull(securityTrailer)) {
                iso20022Builder.securityTrailer(securityTrailer);
            }
            if (Objects.nonNull(supplementaryData)) {
                iso20022Builder.supplementaryData(supplementaryData);
            }

            //  Solo agregar processingResult si es un mensaje de respuesta
            if (input.getMessageType().equals("0120") || input.getMessageType().equals("0420")) {
                iso20022Builder.processingResult(processingResultMappingStrategy.mapper(input, subFields));
               //iso20022Builder.processingResult(mapperUtil.createProcessingResult(input));
            }

            return iso20022Builder.build();

        }catch (MapperFieldsException e) {
            //TODO: Que hacer si hay una excepcion de mapeo local ¿Que deberiamos hacer como flujo funcional?
            LogsTraces.writeWarning(e.getCode() + " " + e.getDescription() + " " + e.getCause());
            return buildFallbackResponse(input); //Esto no deberia devolverse, lo correcto es lanzar una excepcion
        } catch (Exception e) {
            //TODO: Error no contemplado o controlados por temas de null pointer ¿Que deberiamos hacer como flujo funcional?
            LogsTraces.writeWarning("PGWP-00121 - ExceptionError al mapear desde ISO8583: " +  e);
            return buildFallbackResponse(input); //Esto no deberia devolverse, lo correcto es lanzar una excepcion
        }
    }

    /* Este método es un placeholder, ya que el mapeo inverso no está implementado.
     * En una implementación real, deberías convertir los campos de ISO20022 a ISO8583.
     */
    @Override
    public Map<String, String> unMapper(ISO20022 input) {

        Map<String,String> processingResult = processingResultMappingStrategy.unMapper(input.getNetworkName(),input.getProcessingResult());
        Map<String,String> environment = environmentStrategy.unMapper(input.getNetworkName(),input.getEnvironment());
        Map<String,String> transaction = transactionStrategy.unMapper(input.getNetworkName(),input.getTransaction());
        Map<String,String> context = contextStrategy.unMapper(input.getNetworkName(),input.getContext());
        Map<String,String> protectedData = protectedDataStrategy.unMapper(input.getNetworkName(),input.getProtectedData());
        Map<String,String> securityTrailer = securityTrailerStrategy.unMapper(input.getNetworkName(),input.getSecurityTrailer());
        Map<String,String> traceData = traceDataStrategy.unMapper(input.getNetworkName(),input.getTraceData());
        Map<String,String> supplementaryData = supplementaryDataStrategy.unMapper(input.getNetworkName(),input.getSupplementaryData());
        Map<String,String> addendumData = addendumDataStrategy.unMapper(input.getNetworkName(),input.getAddendumData());

        Map<String,String> mapValues=new HashMap<>();

        mapValues.putAll(addendumData);
        mapValues.putAll(environment);
        mapValues.putAll(transaction);
        mapValues.putAll(context);
        mapValues.putAll(protectedData);
        mapValues.putAll(securityTrailer);
        mapValues.putAll(traceData);
        mapValues.putAll(supplementaryData);
        mapValues.putAll(processingResult);

        // momentaneo para limpiar los en blanco
        //mapValues.values().removeIf(String::isEmpty);
        mapValues.put("integratedCircuitCard", input.getIccRelatedData());
        mapValues.put("messageType", MessageFunction.convertTypeMessageResponse(input.getMessageFunction())); // Mientras no nos den la marca para saber que tipo de respuesta es.
        mapValues.put("networkName", input.getNetworkName());

        return mapValues;
    }

    //TODO este metodo solo deberia usarse para los siguientes mensajes "0110", "0130", "0410", "0430", "0210","0800","0810","0312"
    private ISO20022 createISO20022ResponseFromHost(ISO8583 input, Map<String, String> subFields) {
        //TODO: Validar si es necesario llamar al uncrypto en el flujo HOST
        //TODO: Construir los objetos mandatorios del iso200022 segun el catalogo para no tener error de nullPointer
        // Incluir siempre el PAN por que se tiene uso dentro de las reglas de orquestacion del application-local.yml
        EnvironmentDTO environment = environmentStrategy.mapperResponse(input);
        TransactionDTO transaction= transactionStrategy.mapperResponse(input);//Campos mandatorios
        ContextDTO context = contextStrategy.mapperResponse(input);//Campos mandatorios
        AddendumDataDTO addendumData = addendumDataStrategy.mapperResponse(input);
        MonitoringDTO monitoring=monitoringService.build(input, transaction,null,null);
        //TODO: No aplica para el mensaje 0800 y 0302 validar que  ocurriria
        ProcessingResultDTO processingResultDTO = processingResultMappingStrategy.mapper(input,subFields);

        LogsTraces.writeInfo("requestMessage %s".formatted(input.getPlainTextPCI()));
        LogsTraces.writeInfo("transactionReference %s".formatted(transaction.getTransactionId().getTransactionReference()));

        return ISO20022.builder()
                .monitoring(monitoring)
                .context(context)
                .environment(environment)
                .networkName(input.getNetworkName())
                .messageFunction(MessageFunction.convertMessageFunction(input.getMessageType()))
                .transaction(transaction)
                .addendumData(addendumData)//Contiene el mensaje de respuesta del host
                .processingResult(processingResultDTO)
                .protocolVersion(mapperUtil.getBinDescription(input.getNetworkName(),input.getBinCode()))
                .build();
    }


    // Metodo auxiliar para verificar si es un mensaje de respuesta
    private boolean isResponseHost(String messageType) {
        if (messageType == null || messageType.length() < 4) return false;
        return Set.of("0110","0130","0410","0430","0210","0810","0312","0800").contains(messageType); //Validar si hay que incluir el 0130
    }


    //Esto solo aplica para el passtrhoug luego se debe de evaluar si se debe de eliminar
    private ISO20022 buildFallbackResponse(ISO8583 input) {
        return ISO20022.builder()
                .networkName(input.getNetworkName())
                .messageFunction(MessageFunction.convertMessageFunction(input.getMessageType()))
                .socketPort(GrpcHeadersInfo.getPort())
                .transaction(TransactionDTO.builder()
                        .transactionId(TransactionIdDTO.builder()
                                .transactionReference("fallback-%s".formatted(UUID.randomUUID()))
                                .build())
                        .build())
                .environment(EnvironmentDTO.builder()
                        .card(CardDTO.builder()
                                .pan(input.getPrimaryAccountNumber())
                                .build())
                        .build())
                .addendumData(AddendumDataDTO.builder()
                        .additionalData(List.of(
                                AdditionalDataDTO.builder()
                                        .key("ISO8583_HOST")
                                        .value(input.getOriginalMessage())
                                        .build(),
                                AdditionalDataDTO.builder()
                                        .key("UNSP")
                                        .value(input.getMessageType())
                                        .build()
                        ))
                        .build())
                .monitoring(MonitoringDTO.builder().isNextGen(Boolean.FALSE).build())
                .build();
    }
}

