package com.bbva.orchestrator.grpcclient;

import com.bbva.gateway.dto.iso20022.ISO20022;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ContextConfiguration
class GrpcControlDialogoClientTest {

    private final GrpcControlDialogoClient grpcControlDialogoClient = new GrpcControlDialogoClient();

    @Test
    void testCallControlDialogoPostProcessService() {
        ISO20022 iso20022Request = ISO20022.builder().build();
        ISO20022 iso2002Response = grpcControlDialogoClient.callControlDialogoService("localhost", 9098, iso20022Request);
        assertEquals(iso20022Request, iso2002Response);
    }

}