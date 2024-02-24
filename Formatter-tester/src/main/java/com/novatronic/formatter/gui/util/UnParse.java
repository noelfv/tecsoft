/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.util;

import com.novatronic.formatter.util.VariableByteBuffer;
import java.util.regex.Pattern;

/**
 *
 * @author ofernandez
 */
public class UnParse {

    /**
     * Este metodo nos permite leer un arrgle el cual pueda tener contener caracteres no
     * imprimibles los cuales expresados en formato hexadecimal
     *
     * @param array
     * @return
     */
    public static String unparse(byte[] array) {
        StringBuilder buffer;
        VariableByteBuffer varBuffer;
        boolean hexaStarted;

        varBuffer = new VariableByteBuffer();
        hexaStarted = false;
        buffer = new StringBuilder(array.length);
        for (int i = 0; i < array.length; i++) {
            if ((31 < array[i]) && (array[i] < 127)) {
                if(hexaStarted){
                    hexaStarted = false;
                    buffer.append("$HEX{").append(varBuffer.toHexaString()).append("}");
                }
                buffer.append((char)array[i]);
            } else {
                if (!hexaStarted) {
                    varBuffer = new VariableByteBuffer();
                    hexaStarted = true;
                }
                varBuffer.addByte(array[i]);
            }
        }
        
        if(hexaStarted){
            buffer.append("$HEX{").append(varBuffer.toHexaString()).append("}");
        }

        return buffer.toString();
    }
}
