package com.bbva.orchestrator.core.parser.factory;

import java.util.Map;

/**
 * Interfaz para parsear mensajes ISO 8583.
 * Proporciona métodos para convertir entre tramas ISO 8583 y mapas de campos.
 */
public interface ISO8583DelegateParser {

    /**
     * Se encarga de convertir un String ISO8583 a un mapa conformado
     * por sus campos
     *
     * @param originalMessage Trama ISO8583 original
     * @return Mapa conteniendo todos los campos de la trama
     */
    Map<String, String> parser(String originalMessage);

    /**
     * Convierte un mapa de campos a un String ISO8583
     *
     * @param mappedFields Mapa con todos los valores del ISO8583 parseados
     * @return Una trama ISO8583 en formato Hexadecimal con los bitmaps recreado
     */
    String unParser(Map<String, String> mappedFields);


    /**
     * Parsea los subcampos de campos variables específicos (como el Campo 48)
     * a partir de un mapa de campos principales ya parseados.
     *
     * @param iso8583 Objeto iso8583 con todos los campos principales parseados,
     *                  donde el Campo 48 (si está presente) contiene su valor hexadecimal.
     * @return Un mapa que contiene todos los subcampos y sub-subcampos parseados,
     *         con claves como "48.01", "48.11.01", etc.
     *         Si el Campo 48 no está presente, devuelve un mapa vacío.
     */
    //Validar este punto en que casos requiere un mapa de subcampos para los 0800 no es necesario
  //  Map<String, String> parserSubFields(ISO8583 iso8583);


    /**
     * Convierte un mapa de campos a un String ISO8583 en texto claro
     *
     * @param mappedFields Mapa con todos los valores del ISO8583 parseados
     * @return Una trama ISO8583 en formato en claro con los bitmaps recreado
     */
    String unParserPlainText(Map<String, String> mappedFields);




}
