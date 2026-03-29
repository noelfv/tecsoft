package com.bbva.orchestrator.core.fields.definitions.subfields.fixed;

import com.bbva.orchestrator.core.parser.iso8583.ParsedSubFieldResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Field63DefinitionTest {

    @Test
    void testGetId() {
        Field63Definition field63 = new Field63Definition();
        assertEquals("63", field63.getId());
    }

    @Test
    void testGetSubFields() {
        Field63Definition field63 = new Field63Definition();
        List<ParsedSubFieldResult> subFields = field63.getSubFields();
        assertEquals(1, subFields.size());
        assertEquals(new ParsedSubFieldResult("63.01", "63.01", 4), subFields.get(0));
    }
}
