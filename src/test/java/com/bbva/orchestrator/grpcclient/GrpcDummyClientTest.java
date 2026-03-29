package com.bbva.orchestrator.grpcclient;

import com.bbva.gateway.dto.iso20022.ISO20022;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ContextConfiguration
class GrpcDummyClientTest {

    private final GrpcDummyClient grpcDummyClient = new GrpcDummyClient();

    @Test
    void testCallGetDummyResponse() {
        ISO20022 iso20022Request = ISO20022.builder().build();
        ISO20022 iso2002Response = grpcDummyClient.callDummyService("localhost", 9098, iso20022Request);
        assertEquals(iso20022Request, iso2002Response);
    }
}