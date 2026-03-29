package com.bbva.orchestrator.grpcclient;

import com.bbva.gateway.dto.iso20022.ISO20022;
import com.bbva.orchlib.grpcclient.IGrpcDummyClient;
import org.springframework.stereotype.Component;

/**
 * Clase GRPC cliente de Dummy
 */
@Component
public class GrpcDummyClient implements IGrpcDummyClient {

    @Override
    public ISO20022 callDummyService(String serverGrpcClient, int portGrpcClient, ISO20022 iso20022) {
        return iso20022;
    }
}

