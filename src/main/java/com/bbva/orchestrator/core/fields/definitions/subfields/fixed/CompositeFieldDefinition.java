package com.bbva.orchestrator.core.fields.definitions.subfields.fixed;

import com.bbva.orchestrator.core.parser.iso8583.ParsedSubFieldResult;

import java.util.List;

public interface CompositeFieldDefinition {
    String getId();
    List<ParsedSubFieldResult> getSubFields();
}