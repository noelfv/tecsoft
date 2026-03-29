package com.bbva.orchestrator.core;

import com.bbva.gateway.dto.iso20022.AddendumDataDTO;
import com.bbva.gateway.dto.iso20022.AdditionalDataDTO;
import com.bbva.gateway.dto.iso20022.ISO20022;
import com.bbva.gateway.interceptors.GrpcHeadersInfo;
import com.bbva.gateway.utils.LogsTraces;
import com.bbva.orchestrator.core.builders.ISO20022Builder;
import com.bbva.orchestrator.core.logic.factory.FieldLogicFactory;
import com.bbva.orchestrator.core.logic.factory.NetworkDelegateFieldLogic;
import com.bbva.orchestrator.core.mapper.factory.ISO20022DelegateMapper;
import com.bbva.orchestrator.core.mapper.factory.MapperFactory;
import com.bbva.orchestrator.core.operation.OperationHandler;
import com.bbva.orchestrator.core.operation.factory.OperationHandlerFactory;
import com.bbva.orchestrator.core.parser.factory.ISO8583DelegateParser;
import com.bbva.orchestrator.core.parser.factory.ParserFactory;
import com.bbva.orchestrator.core.transformer.DelegateTransformer;
import com.bbva.orchestrator.core.transformer.factory.TransformerFactory;
import com.bbva.orchlib.parser.IParser;
import com.bbva.orchlib.utils.RulesLocalUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class OrchestratorFlowProcess implements IParser {

    private final TransformerFactory transformerFactory;
    private final OperationHandlerFactory operationHandlerFactory;
    private final ISO20022Builder iso20022Builder;
    private final MapperFactory mapperFactory;
    private final ParserFactory parserFactory;
    private final FieldLogicFactory fieldLogicFactory;

    @Override
    public ISO20022 convert8583to20022(String originalMessage) {
        DelegateTransformer transformer = transformerFactory.getDelegateTransformer(GrpcHeadersInfo.getNetwork());
        Map<String, String> fieldsValues = transformer.toMap(originalMessage);
        OperationHandler operation = operationHandlerFactory.handle(fieldsValues);
        return iso20022Builder.build(operation);
    }

    @Override
    public String convert20022to8583(ISO20022 iso20022) {
        Boolean isNextGen = iso20022.getMonitoring().getIsNextGen();
        if(isNextGen){
            return flowPaymentAuthorization(iso20022);
        }
        //TODO traza opcional, para production se debe eliminar
        LogsTraces.writeInfo("Flujo passthrough red: " + iso20022.getNetworkName() + ", Regla destino : " + RulesLocalUtils.getLastOrchestration());
        return flowPassThrough(iso20022, "ISO8583_HOST");
    }

    private String flowPaymentAuthorization(ISO20022 iso20022) {
        ISO20022DelegateMapper delegateMapper = mapperFactory.getDelegateMapper();
        Map<String, String> fieldsValues = delegateMapper.unMapper(iso20022);
        NetworkDelegateFieldLogic delegateFieldLogic = fieldLogicFactory.getDelegateFieldLogic(iso20022.getNetworkName());
        Map<String, String> fieldsValuesResponse = delegateFieldLogic.applyLogicFields(fieldsValues);
        ISO8583DelegateParser delegateParser = parserFactory.getDelegateParser();
        return delegateParser.unParser(fieldsValuesResponse);
    }

    private String flowPassThrough(ISO20022 iso20022, String keyTramaOrigin) {
        return findValueByKey(iso20022.getAddendumData(), keyTramaOrigin);
    }

    private String findValueByKey(AddendumDataDTO input, String key) {
        if (input == null || input.getAdditionalData() == null) {
            return null;
        }
        return input.getAdditionalData().stream()
                .filter(data -> key.equals(data.getKey()))
                .map(AdditionalDataDTO::getValue)
                .findFirst()
                .orElse(null);
    }
}