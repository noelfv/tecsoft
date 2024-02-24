/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.field.util;

/**
 *
 * @author Omar
 */
public class StringUtil {
    
    public static String repeatString(String sequence, int number) {
        String result = "";
        for (int i = 0; i < number; i++) {
            result += sequence;
        }
        return result;
    }
}
