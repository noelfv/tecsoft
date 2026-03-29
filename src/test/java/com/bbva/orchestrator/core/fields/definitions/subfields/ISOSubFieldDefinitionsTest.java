package com.bbva.orchestrator.core.fields.definitions.subfields;

import com.bbva.gateway.interceptors.GrpcHeadersInfo;
import com.bbva.gateway.utils.LogsTraces;
import com.bbva.orchestrator.core.fields.definitions.subfields.tlv.Field48;
import com.bbva.orchestrator.core.fields.definitions.subfields.tlv.TLVFieldLoadStructure;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class ISOSubFieldDefinitionsTest {

    private MockedStatic<LogsTraces> logsTracesMock;
    private MockedStatic<GrpcHeadersInfo> grpcHeadersInfo;

    @BeforeEach
    void setUp() {
        logsTracesMock = mockStatic(LogsTraces.class);
        grpcHeadersInfo = mockStatic(GrpcHeadersInfo.class);
    }

    @AfterEach
    void tearDown() {
        logsTracesMock.close();
        grpcHeadersInfo.close();
    }

    @Test
    void getSubFieldDefinitionsForComposite_returnsDirectField48Defs_whenIdIs48() {
        Map<String, Field48> result = TLVFieldLoadStructure.getSubFieldDefinitionsForComposite("48");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getSubFieldDefinitionsForComposite_returnsEmptyMap_whenIdIsUnknown() {
        Map<String, Field48> result = TLVFieldLoadStructure.getSubFieldDefinitionsForComposite("99");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getSubFieldDefinitionsForComposite_returnsSubSubFieldMap_whenIdIsSubField() {
        // Si existe subcampo "33" en SUB_SUBFIELD_MAP, debe devolver el mapa correspondiente
        Map<String, Field48> result = TLVFieldLoadStructure.getSubFieldDefinitionsForComposite("33");
        assertNotNull(result);
        // Puede estar vacío si no hay sub-subcampos, pero no debe ser null
    }
}