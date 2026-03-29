package com.bbva.orchestrator.core.fields.definitions.subfields.fixed;

import com.bbva.orchestrator.core.parser.iso8583.ParsedSubFieldResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Qualifier("field22Definition")
public class Field22Definition implements CompositeFieldDefinition{
    @Override
    public String getId() {
        return "22";
    }

    @Override
    public List<ParsedSubFieldResult> getSubFields() {
        return List.of(
                new ParsedSubFieldResult("22.01", "22.01", 2),
                new ParsedSubFieldResult("22.02", "22.02", 1)
        );
    }
}
