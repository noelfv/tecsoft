/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.field.util;

import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public class Converter {
    public static final Logger log = Logger.getLogger(Converter.class);
    
    private static final char ZERO = '0';
    private static final char CHAR_F = 'F';

    /**
     * Convierte una cadena dada en formato hexadecimal a una cadena de bytes.
     * Los caracteres validos son={0,1,2,3,4,5,6,7,8,9,A,B,C,D,E,F}. Si la
     * cadena posee una cantidad impar de caracteres, se alineara a la izquierda
     * y se rellenera con un '0' a la derecha.
     * @param data Cadena a convertir en un arreglo de bytes
     * @return El arreglo de bytes convertido
     * @throws IllegalArgumentException Si el parametro recibido contiene al
     * menos un caracter que no pertenece al conjunto valido.
     */
    public static byte[] hexaToBytes(String data) throws IllegalArgumentException {
        int hiNibble;
        int loNibble;
        char[] chars;
        byte[] result;

        data = (data.length() % 2 == 1) ? ZERO + data : data;
        result = new byte[data.length() / 2];

        chars = data.toCharArray();
        for (int i = 0; i < chars.length; i += 2) {
            hiNibble = getDigit(chars[i]) << 4;
            loNibble = getDigit(chars[i + 1]);
            result[i / 2] = (byte) (hiNibble + loNibble);
        }

        return result;
    }

    private static int getDigit(char c) {
        int res = c - ZERO;
        if (c > CHAR_F) {
            throw new IllegalArgumentException("Caracter hexadecimal Ilegal:" + c);
        }
        return (res > 10) ? (res - 7) : res;
    }
    
    /**
     * Convierte una cadena de bytes en una cadena de caracteres en representacion
     * hexadecimal
     * @param bytes La cadena a ser convertida
     * @return Una cadena que representa los bytes recibidios
     */
    public static String toHexaString(byte[] bytes){
        String result = "";
        for (int i = 0; i < bytes.length; i++) {
            result += toHexaString(bytes[i]);
        }
        return result;
    }
    
    public static String toHexaString(byte bite){
        return Integer.toString((bite & 0xff) + 0x100, 16)
                            .substring(1).toUpperCase();
    }
}
