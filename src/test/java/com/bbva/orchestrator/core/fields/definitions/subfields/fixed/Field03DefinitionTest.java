package com.bbva.orchestrator.core.fields.definitions.subfields.fixed;

import com.bbva.orchestrator.core.parser.iso8583.ParsedSubFieldResult;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class Field03DefinitionTest {

    @Test
    void testGetId() {
        Field03Definition field03 = new Field03Definition();
        assertEquals("03", field03.getId());
    }

    @Test
    void testGetSubFields() {
        Field03Definition field03 = new Field03Definition();
        List<ParsedSubFieldResult> subFields = field03.getSubFields();
        assertEquals(3, subFields.size());
        assertEquals(new ParsedSubFieldResult("03.01", "transactionType", 2), subFields.get(0));
        assertEquals(new ParsedSubFieldResult("03.02", "accountFrom", 2), subFields.get(1));
        assertEquals(new ParsedSubFieldResult("03.03", "accountTo", 2), subFields.get(2));
    }
}
