package com.bbva.orchestrator.core.fields.definitions.subfields.fixed;

import com.bbva.orchestrator.core.parser.iso8583.ParsedSubFieldResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Qualifier("field63Definition")
public class Field63Definition implements CompositeFieldDefinition{
    @Override
    public String getId() {
        return "63";
    }

    @Override
    public List<ParsedSubFieldResult> getSubFields() {
        return List.of(
                new ParsedSubFieldResult("63.01", "63.01", 4)
        );
    }
}
