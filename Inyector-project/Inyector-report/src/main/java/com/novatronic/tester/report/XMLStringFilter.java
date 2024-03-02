/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.report;

import com.novatronic.tester.engine.Engine;
import java.util.regex.Pattern;

/**
 *
 * @author ofernandez
 */
public abstract class XMLStringFilter {
    private static final Pattern patPrint = Pattern.compile(Engine.getPropertie("PatternFilter"));
    private static final String CARACNOVIS_NAME = Engine.getPropertie("CaracNoVisible");

    public static String filter(String text){
        if (text == null){
            throw new IllegalArgumentException("Se recibió argumento nulo, "
                    + "text:" + text);
        }
        String newString = patPrint.matcher(text).replaceAll(CARACNOVIS_NAME);
        return newString;
    }
}
