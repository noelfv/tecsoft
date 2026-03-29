package com.bbva.orchestrator.core.parser.iso8583.strategy;

import com.bbva.orchestrator.core.exception.ParserFieldsException;
import com.bbva.orchestrator.core.fields.definitions.IFieldDefinition;
import com.bbva.orchestrator.core.parser.iso8583.ParsedFieldResult;
import com.bbva.orchestrator.core.parser.iso8583.handlers.NetworkHandlerField;

public interface FieldParserStrategy {
    /**
     * Parsea una porción de la trama ISO (en formato hexadecimal EBCDIC) y devuelve el valor decodificado
     * junto con la cantidad de caracteres hexadecimales consumidos.
     *
     * @param rawDataSegment La porción de la trama codificada a parsear.
     * @param fieldDefinition La definición del campo/subcampo (implementa IFieldDefinition).
     * @return Un objeto ParsedFieldResult que contiene el valor decodificado y la longitud consumida.
     * @throws ParserFieldsException Si la trama es demasiado corta o el formato es inválido.
     */
    ParsedFieldResult parse(String rawDataSegment, IFieldDefinition fieldDefinition, NetworkHandlerField networkProfile);

    /**
     * Construye la porción de la trama ISO (siempre en HEX EBCDIC para la salida) a partir de un valor decodificado.
     *
     * @param processedDataSegment El valor del campo ya decodificado (ej. "1234.56", "ABC").
     * @param fieldDefinition La definición del campo/subcampo.
     * @return La representación hexadecimal EBCDIC del campo lista para ser insertada en la trama.
     */
    String build(String processedDataSegment, IFieldDefinition fieldDefinition, NetworkHandlerField networkProfile);


}
