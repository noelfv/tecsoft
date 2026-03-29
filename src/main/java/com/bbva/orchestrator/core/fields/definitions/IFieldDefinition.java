package com.bbva.orchestrator.core.fields.definitions;

import com.bbva.orchestrator.core.parser.iso8583.strategy.FieldParserStrategy;

public interface IFieldDefinition {
    String getName(); // Nombre descriptivo
    ISODataType getTypeData(); // Tipo de dato (NUMERIC, ALPHA_NUMERIC, etc.)
    boolean isVariable(); // Si es de longitud variable (LLVAR/LLLVAR, TLV)
    int getLength(); // Longitud fija o longitud del prefijo (LL/LLL)
    FieldParserStrategy getParserStrategy(); // Estrategia de parsing para este campo/subcampo
    String getIdentifier();
}
