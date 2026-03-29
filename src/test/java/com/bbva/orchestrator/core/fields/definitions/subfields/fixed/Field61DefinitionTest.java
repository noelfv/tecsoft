package com.bbva.orchestrator.core.fields.definitions.subfields.fixed;

import com.bbva.orchestrator.core.parser.iso8583.ParsedSubFieldResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Field61DefinitionTest {
    @Test
    void testGetId() {
        Field61Definition field61 = new Field61Definition();
        assertEquals("61", field61.getId());
    }

    @Test
    void testGetSubFields() {
        Field61Definition field61 = new Field61Definition();
        List<ParsedSubFieldResult> subFields = field61.getSubFields();
        assertEquals(14, subFields.size());
        assertEquals(new ParsedSubFieldResult("61.01", "61.01", 1), subFields.get(0));
        assertEquals(new ParsedSubFieldResult("61.02", "61.02", 1), subFields.get(1));
        assertEquals(new ParsedSubFieldResult("61.03", "61.03", 1), subFields.get(2));
        assertEquals(new ParsedSubFieldResult("61.04", "61.04", 1), subFields.get(3));
        assertEquals(new ParsedSubFieldResult("61.05", "61.05", 1), subFields.get(4));
        assertEquals(new ParsedSubFieldResult("61.06", "61.06", 1), subFields.get(5));
        assertEquals(new ParsedSubFieldResult("61.07", "61.07", 1), subFields.get(6));
        assertEquals(new ParsedSubFieldResult("61.08", "61.08", 1), subFields.get(7));
        assertEquals(new ParsedSubFieldResult("61.09", "61.09", 1), subFields.get(8));
        assertEquals(new ParsedSubFieldResult("61.10", "61.10", 1), subFields.get(9));
        assertEquals(new ParsedSubFieldResult("61.11", "61.11", 1), subFields.get(10));
        assertEquals(new ParsedSubFieldResult("61.12", "61.12", 2), subFields.get(11));
        assertEquals(new ParsedSubFieldResult("61.13", "61.13", 3), subFields.get(12));
        assertEquals(new ParsedSubFieldResult("61.14", "61.14", 0), subFields.get(13));
    }
}
