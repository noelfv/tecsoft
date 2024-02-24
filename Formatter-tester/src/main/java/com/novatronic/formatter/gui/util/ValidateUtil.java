/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Omar
 */
public class ValidateUtil {
    private static final String NUMBER_REG_EXPR = "[0-9]+";
    private static final Pattern NUMBER_PATTERN = Pattern.compile(NUMBER_REG_EXPR);
    
    public static boolean isNumber(String text){
        Matcher matcher;
        matcher = NUMBER_PATTERN.matcher(text);
        
        return matcher.matches();
    }
}
