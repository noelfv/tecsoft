package com.bbva.orchestrator.core.fields.definitions.subfields.fixed;

import com.bbva.orchestrator.core.parser.iso8583.ParsedSubFieldResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Qualifier("field61Definition")
public class Field61Definition implements CompositeFieldDefinition{
    @Override
    public String getId() {
        return "61";
    }

    @Override
    public List<ParsedSubFieldResult> getSubFields() {
        return List.of(
                new ParsedSubFieldResult("61.01", "61.01", 1),
                new ParsedSubFieldResult("61.02", "61.02", 1),
                new ParsedSubFieldResult("61.03", "61.03", 1),
                new ParsedSubFieldResult("61.04", "61.04", 1),
                new ParsedSubFieldResult("61.05", "61.05", 1),
                new ParsedSubFieldResult("61.06", "61.06", 1),
                new ParsedSubFieldResult("61.07", "61.07", 1),
                new ParsedSubFieldResult("61.08", "61.08", 1),
                new ParsedSubFieldResult("61.09", "61.09", 1),
                new ParsedSubFieldResult("61.10", "61.10", 1),
                new ParsedSubFieldResult("61.11", "61.11", 1),
                new ParsedSubFieldResult("61.12", "61.12", 2),
                new ParsedSubFieldResult("61.13", "61.13", 3),
                new ParsedSubFieldResult("61.14", "61.14", 0)
        );
    }
}
