package com.bbva.orchestrator.core.parser.iso8583.strategy.fields;

import com.bbva.orchestrator.core.exception.ParserFieldsException;
import com.bbva.orchestrator.core.parser.iso8583.strategy.FieldParserStrategy;
import com.bbva.orchestrator.core.fields.definitions.IFieldDefinition;
import com.bbva.orchestrator.core.parser.iso8583.ParsedFieldResult;
import com.bbva.orchestrator.core.parser.iso8583.handlers.NetworkHandlerField;

/**
 * Implementación de la estrategia de parsing para campos hexadecimales.
 * Esta clase maneja la conversión de datos hexadecimales a partir de segmentos hexadecimales
 * y viceversa.
 */
public class HexadecimalFieldParser implements FieldParserStrategy {

    /**
     * Método para parsear un segmento de datos crudos en un campo hexadecimal.
     * @param rawDataSegment Segmento de datos crudos a parsear.
     * @param fieldDefinition Definición del campo que se está parseando.
     * @return Resultado del campo parseado, incluyendo el valor y la longitud del segmento.
     * @throws ParserFieldsException Si el segmento de datos es demasiado corto.
     */
    @Override
    public ParsedFieldResult parse(String rawDataSegment, IFieldDefinition fieldDefinition, NetworkHandlerField networkHandlerField) {
        try {
            int expectedHexLengthCalculated = networkHandlerField.decodeLengthField(fieldDefinition);
            String extractedHex = rawDataSegment.substring(0, expectedHexLengthCalculated);
            return new ParsedFieldResult(extractedHex, expectedHexLengthCalculated);
        } catch (RuntimeException e) {
            throw new ParserFieldsException("PGWP-00105","Error procesando campo " + fieldDefinition.getIdentifier() + " ¨[" + rawDataSegment + "]",e);
        }
    }


    /**
     * Método para construir un campo hexadecimal a partir de su valor.
     * @param processedDataSegment Valor del campo a construir.
     * @param fieldDefinition Definición del campo que se está construyendo.
     * @return El valor del campo en formato hexadecimal.
     */
    @Override
    public String build(String processedDataSegment, IFieldDefinition fieldDefinition, NetworkHandlerField networkHandlerField) {
        return networkHandlerField.encode(processedDataSegment,fieldDefinition.getTypeData());
    }

}