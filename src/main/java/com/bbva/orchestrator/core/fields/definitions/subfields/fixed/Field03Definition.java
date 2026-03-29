package com.bbva.orchestrator.core.fields.definitions.subfields.fixed;

import com.bbva.orchestrator.core.parser.iso8583.ParsedSubFieldResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@Qualifier("field03Definition")
public class Field03Definition implements CompositeFieldDefinition {

    @Override
    public String getId() {
        return "03";
    }

    @Override
    public List<ParsedSubFieldResult> getSubFields() {
        return List.of(
                new ParsedSubFieldResult("03.01", "transactionType", 2),
                new ParsedSubFieldResult("03.02", "accountFrom", 2),
                new ParsedSubFieldResult("03.03", "accountTo", 2)
        );
    }
}