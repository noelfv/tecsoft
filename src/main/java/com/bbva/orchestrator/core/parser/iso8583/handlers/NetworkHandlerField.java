package com.bbva.orchestrator.core.parser.iso8583.handlers;

import com.bbva.orchestrator.core.fields.definitions.IFieldDefinition;
import com.bbva.orchestrator.core.fields.definitions.ISODataType;

/**
 * Interfaz que define el perfil de red para diferentes protocolos de comunicación.
 * Proporciona métodos para manejar la longitud de campos variables y fijos,
 * así como para decodificar valores según el tipo de datos ISO.
 *
 * Implementaciones específicas de esta interfaz deben proporcionar la lógica
 * adecuada para cada protocolo de red (por ejemplo, Visa, Mastercard).
 */
public interface NetworkHandlerField {

    /**
     * Obtiene la longitud del encabezado en caracteres hexadecimales para un campo de longitud variable
     * basado en su definición.
     *
     * @param fieldDefinition  La definición del campo que incluye detalles como el tipo de datos y la longitud máxima.
     * @return La longitud del encabezado del campo en caracteres decimal.
     */
    int getHeaderFieldVar(IFieldDefinition fieldDefinition);


    /** Decodifica la longitud del encabezado en caracteres hexadecimales para un campo de longitud variable
     * basado en su representación hexadecimal y la definición del campo.
     *
     * @param lengthHeader      La longitud del encabezado en caracteres hexadecimales.
     * @param rawDataSegment    La representación hexadecimal del segmento de datos que contiene el encabezado.
     * @param fieldDefinition   La definición del campo que incluye detalles como el tipo de datos y la longitud máxima.
     * @return La longitud del campo en caracteres decimal.
     */
    int decodeHeaderFieldVar(int lengthHeader, String rawDataSegment,IFieldDefinition fieldDefinition);


    /**
     * Calcula la longitud fija en caracteres hexadecimales de un campo basado en su definición.
     *
     * @param fieldDefinition  La definición del campo que incluye detalles como el tipo de datos y la longitud fija.
     * @return La longitud fija del campo en caracteres decimal.
     */
    int decodeLengthField(IFieldDefinition fieldDefinition);

    /** Decodifica un valor de campo desde su representación hexadecimal o texto claro a su formato legible
     * según el tratamiento por cada red.
     *
     * @param rawDataSegment  La representación hexadecimal o texto claro del valor del campo.
     * @param dataType       El tipo de datos ISO que indica cómo debe interpretarse el valor.
     * @return El valor decodificado en su formato legible (por ejemplo, cadena, número).
     */
    String decode(String rawDataSegment, ISODataType dataType);

    /**
     * Codifica la longitud del encabezado en caracteres hexadecimales para un campo de longitud variable
     * basado en el valor procesado y la definición del campo.
     *
     * @param processedDataSegment  El valor del campo que se va a codificar.
     * @param fieldDefinition       La definición del campo que incluye detalles como el tipo de datos y la longitud máxima.
     * @return La representación hexadecimal del encabezado del campo.
     */
    String encodeHeaderFieldVar(String processedDataSegment, IFieldDefinition fieldDefinition);

    /** Codifica un valor de campo desde su formato legible a su representación hexadecimal o texto claro
     * según el tratamiento por cada red.
     *
     * @param processedDataSegment  El valor del campo que se va a codificar.
     * @param dataType              El tipo de datos ISO que indica cómo debe codificarse el valor.
     * @return La representación hexadecimal o texto claro del valor del campo.
     */
    String encode(String processedDataSegment, ISODataType dataType);

}