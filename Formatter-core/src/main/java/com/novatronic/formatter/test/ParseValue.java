/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.test;

import com.novatronic.formatter.util.VariableByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public class ParseValue {
    private static final Logger log = Logger.getLogger(ParseValue.class);
    
    private static final char ZERO = '0';
    private static final char CHAR_F = 'F';
    private static final String REGEX = "\\$HEX\\{([0-9A-F]*)\\}";
    private static Pattern hexaPattern = Pattern.compile(REGEX);
    
    /**
     * Este metodo nos permite leer una trama String y convertirla en un arreglo de bytes
     * con porciones en hexadecimal bajo el formato:<br/>
     * <b>HOLA prueba$HEX{AAFF206F}</b><br/>
     * Como resultado la cadena AAFF206F se interpretara en formato hexadecimal
     * @param value La cadena a ser interpretada
     * @return Un Ibjeto VariableByteBuffer con los bytes procesados
     */
    public static VariableByteBuffer parse(String value){
        int idx = 0;
        int delta;
        int offset = 0;
        byte[] data;
        VariableByteBuffer valueParse;
        Matcher hexaMatcher;
        String group;
        
        valueParse = new VariableByteBuffer(value.length()*2);
        valueParse.add(value);
        hexaMatcher = hexaPattern.matcher(value);
        
        while(hexaMatcher.find(idx)){
            group = hexaMatcher.group(1);
            log.debug("Hexa por procesar=" + group);
            data = hexaToBytes(group);
            log.debug("start=" + hexaMatcher.start() +", end=" + hexaMatcher.end() + ",data=" + data);
            
            delta = hexaMatcher.end() - hexaMatcher.start() - data.length;
            valueParse.replace(hexaMatcher.start() - offset, data);
            valueParse.remove(hexaMatcher.start() + data.length - offset, delta);
            offset += delta;
            log.debug("offset=" + offset + ",delta=" + delta + ",valueParse=" + valueParse);
            idx = hexaMatcher.end();
        }
        
        return valueParse;
    }
    
    /**
     * Este metodo nos permite leer
     * @param array
     * @return 
     */
    public String unparse(byte[] array){
        String result;
        
        return null;
    }
    
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
}
