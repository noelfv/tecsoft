package com.bbva.orchestrator.core.fields.definitions.subfields.fixed;

import com.bbva.orchestrator.core.parser.iso8583.ParsedSubFieldResult;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class Field60DefinitionTest {
    @Test
    void getId_returns60() {
        Field60Definition def = new Field60Definition();
        assertEquals("60", def.getId());
    }

    @Test
    void getSubFields_returnsExpectedSubFields() {
        Field60Definition def = new Field60Definition();
        List<ParsedSubFieldResult> subFields = def.getSubFields();
        assertEquals(10, subFields.size());
        assertEquals(new ParsedSubFieldResult("60.01", "Terminal Type", 1), subFields.get(0));
        assertEquals(new ParsedSubFieldResult("60.02", "Terminal Entry Cap", 1), subFields.get(1));
        assertEquals(new ParsedSubFieldResult("60.03", "Chip Condition", 1), subFields.get(2));
        assertEquals(new ParsedSubFieldResult("60.04", "Special Condition", 1), subFields.get(3));
        assertEquals(new ParsedSubFieldResult("60.05", "Merchant Group", 2), subFields.get(4));
        assertEquals(new ParsedSubFieldResult("60.06", "Chip Trans Indicator", 1), subFields.get(5));
        assertEquals(new ParsedSubFieldResult("60.07", "Auth Reliability", 1), subFields.get(6));
        assertEquals(new ParsedSubFieldResult("60.08", "E-commerce Indicator", 2), subFields.get(7));
        assertEquals(new ParsedSubFieldResult("60.09", "Cardholder ID Method", 1), subFields.get(8));
        assertEquals(new ParsedSubFieldResult("60.10", "Partial Auth Indicator", 1), subFields.get(9));
    }
}