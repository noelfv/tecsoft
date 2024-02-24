/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.field.util;

import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Omar
 */
public class Validator {
    
    private Pattern actualPattern;
    private FieldFormat format;
    private int length;
    
    private static final int LENGTH_NOVALIDATE = -1;
    private static final Map<FieldFormat, Pattern> patterns;
    
    static {
        patterns = new EnumMap<FieldFormat, Pattern>(FieldFormat.class);
        patterns.put(FieldFormat.ALPHA, Pattern.compile("[^\\p{Alpha} ]"));
        patterns.put(FieldFormat.NUMBER, Pattern.compile("[^\\p{Digit}]"));
        patterns.put(FieldFormat.SIGNAL, Pattern.compile("[^\\p{Punct}]"));
        patterns.put(FieldFormat.ALPHA_NUMBER, Pattern.compile("[^\\p{Alnum} ]"));
        patterns.put(FieldFormat.ALPHA_SIGNAL, Pattern.compile("[^\\p{Alpha} \\p{Punct}]"));
        patterns.put(FieldFormat.NUMBER_SIGNAL, Pattern.compile("[^\\p{Digit}\\p{Punct}]"));
        patterns.put(FieldFormat.ALPHA_NUMBER_SIGNAL, Pattern.compile("[^\\p{Graph} ]"));
    }
    
    /**
     * Asigna un formato y tamaño a usar en la validación.
     * @param newFormat El formato a usar en la validación. Este puede ser=
     * {a,n,s,an,as,ns,ans}
     * @param newLength El tamaño a usar en la validación de tamaño. Un valor de
     * -1 indica que no se deberá validar el tamaño.
     */
    public void setFormat(FieldFormat newFormat, int newLength){
        format = newFormat;
        length = newLength;
        
        if (newFormat == null){
            format = FieldFormat.ALL;
        }else{
            actualPattern = patterns.get(format);
        }
    }
    
    /**
     * Realiza la validación sergun el formato y tamaño indicado.
     * @param value el valor por validar.
     * @return True si es valido, falso en caso contrario.
     */
    public boolean isValid(String value){
        if (patternValidate(value) && lenghtValidate(value)) {
            return true;
        }
        return false;
    }
    
    private boolean patternValidate(String value){
        if (actualPattern != null) {
            Matcher matcher = actualPattern.matcher(value);
            if (matcher.find()) {
                return false;
            }
            return true;
        }
        return true;
    }
    
    private boolean lenghtValidate(String value){
        if(length == LENGTH_NOVALIDATE){
            return true;
        }
        if (value.getBytes().length > length) {
            return false;
        }
        return true;
    }

    public Pattern getActualPattern() {
        return actualPattern;
    }

    public FieldFormat getFormat() {
        return format;
    }
}
