package com.bbva.orchestrator.core.fields.definitions.subfields.tlv;

import com.bbva.gateway.interceptors.GrpcHeadersInfo;
import com.bbva.orchestrator.core.fields.definitions.IFieldDefinition;
import com.bbva.orchestrator.core.parser.iso8583.handlers.impl.VisaHandlerField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class TLVFieldLoadStructureTest {

    private MockedStatic<GrpcHeadersInfo> mockedHeaders;

    @BeforeEach
    void setUp() {
        mockedHeaders = mockStatic(GrpcHeadersInfo.class);
    }

    @AfterEach
    void tearDown() {
        mockedHeaders.close();
    }
    @Test
    void testGetDirectSubFieldDefinitionsForField48_returnsMap() {
        Map<String, Field48> result = TLVFieldLoadStructure.getDirectSubFieldDefinitionsForField48();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        // Verifica que contiene algunos subcampos esperados
        assertTrue(result.containsKey("01"));
    }
}

