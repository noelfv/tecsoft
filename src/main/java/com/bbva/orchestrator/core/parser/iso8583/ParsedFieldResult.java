package com.bbva.orchestrator.core.parser.iso8583;

/**
 * @param value Devuelve el valor del campo parseado, que puede ser un String, Bitmap o un Hexadecimal para los campos variables que tengan subcampos.
 * @param consumedLengthInChars Longitud de los caracteres HEX consumidos de la trama
 */

public record ParsedFieldResult(String value, int consumedLengthInChars) {

}