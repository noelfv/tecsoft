package com.bbva.orchestrator.core.fields.definitions.subfields.fixed;

import com.bbva.orchestrator.core.parser.iso8583.ParsedSubFieldResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@Qualifier("field60Definition")
public class Field60Definition implements CompositeFieldDefinition {

    @Override
    public String getId() {
        return "60";
    }

    @Override
    public List<ParsedSubFieldResult> getSubFields() {
        return List.of(
                new ParsedSubFieldResult("60.01", "Terminal Type", 1),
                new ParsedSubFieldResult("60.02", "Terminal Entry Cap", 1),
                new ParsedSubFieldResult("60.03", "Chip Condition", 1),
                new ParsedSubFieldResult("60.04", "Special Condition", 1),
                new ParsedSubFieldResult("60.05", "Merchant Group", 2),
                new ParsedSubFieldResult("60.06", "Chip Trans Indicator", 1),
                new ParsedSubFieldResult("60.07", "Auth Reliability", 1),
                new ParsedSubFieldResult("60.08", "E-commerce Indicator", 2),
                new ParsedSubFieldResult("60.09", "Cardholder ID Method", 1),
                new ParsedSubFieldResult("60.10", "Partial Auth Indicator", 1)
        );
    }
}