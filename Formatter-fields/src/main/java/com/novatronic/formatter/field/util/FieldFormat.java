/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.field.util;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Omar
 */
public enum FieldFormat {

    ALPHA, NUMBER, SIGNAL, ALPHA_NUMBER, ALPHA_SIGNAL, NUMBER_SIGNAL, 
    ALPHA_NUMBER_SIGNAL, ALL;
    
    private static final Map<String, FieldFormat> FORMATS;
    static{
        FORMATS = new HashMap<String, FieldFormat>();
        FORMATS.put("A", ALPHA);
        FORMATS.put("N", NUMBER);
        FORMATS.put("S", SIGNAL);
        FORMATS.put("AN", ALPHA_NUMBER);
        FORMATS.put("AS", ALPHA_SIGNAL);
        FORMATS.put("NS", NUMBER_SIGNAL);
        FORMATS.put("ANS", ALPHA_NUMBER_SIGNAL);
    }

    public static FieldFormat searchFormat(String format) {
        if(format == null){
            return null;
        } else if (FORMATS.containsKey(format.toUpperCase())){
            return FORMATS.get(format.toUpperCase());
        } else {
            return null;
        }
    }
}
