package com.bbva.orchestrator.core.fields.definitions.subfields.fixed;

import com.bbva.orchestrator.core.parser.iso8583.ParsedSubFieldResult;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class Field54DefinitionTest {
    @Test
    void getId_returns54() {
        Field54Definition def = new Field54Definition();
        assertEquals("54", def.getId());
    }

    @Test
    void getSubFields_returnsExpectedSubFields() {
        Field54Definition def = new Field54Definition();
        List<ParsedSubFieldResult> subFields = def.getSubFields();
        assertNotNull(subFields);
        assertEquals(5, subFields.size());
        assertEquals("54.01", subFields.get(0).id());
        assertEquals("additional_account_type", subFields.get(0).name());
        assertEquals(2, subFields.get(0).length());
        assertEquals("54.05", subFields.get(4).id());
        assertEquals("54.05", subFields.get(4).name());
        assertEquals(12, subFields.get(4).length());
    }
}