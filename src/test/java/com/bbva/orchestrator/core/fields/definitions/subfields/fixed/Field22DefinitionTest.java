package com.bbva.orchestrator.core.fields.definitions.subfields.fixed;

import com.bbva.orchestrator.core.parser.iso8583.ParsedSubFieldResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Field22DefinitionTest {
    @Test
    void testGetId() {
        Field22Definition field22 = new Field22Definition();
        assertEquals("22", field22.getId());
    }

    @Test
    void testGetSubFields() {
        Field22Definition field22 = new Field22Definition();
        List<ParsedSubFieldResult> subFields = field22.getSubFields();
        assertEquals(2, subFields.size());
        assertEquals(new ParsedSubFieldResult("22.01", "22.01", 2), subFields.get(0));
        assertEquals(new ParsedSubFieldResult("22.02", "22.02", 1), subFields.get(1));
    }
}
