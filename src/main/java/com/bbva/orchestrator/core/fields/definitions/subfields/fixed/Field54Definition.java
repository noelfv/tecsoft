package com.bbva.orchestrator.core.fields.definitions.subfields.fixed;

import com.bbva.orchestrator.core.parser.iso8583.ParsedSubFieldResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Qualifier("field54Definition")
public class Field54Definition implements CompositeFieldDefinition {

    @Override
    public String getId() {
        return "54";
    }

    @Override
    public List<ParsedSubFieldResult> getSubFields() {
        return List.of(
                new ParsedSubFieldResult("54.01", "additional_account_type", 2),
                new ParsedSubFieldResult("54.02", "additional_amount_type", 2),
                new ParsedSubFieldResult("54.03", "additional_currency_code", 3),
                new ParsedSubFieldResult("54.04", "additional_indicator", 1),
                new ParsedSubFieldResult("54.05", "54.05", 12)
        );
    }
}