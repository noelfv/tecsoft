/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.field.util.filler;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Omar
 */
public enum Align {
    LEFT, RIGHT;
    
    private static final Map<String, Align> ALIGNS;
    static{
        ALIGNS = new HashMap<String, Align>();
        ALIGNS.put("LEFT", LEFT);
        ALIGNS.put("RIGHT", RIGHT);
    }
    
    public static Align searchAlign(String format) {
        if(format == null){
            return null;
        } else if (ALIGNS.containsKey(format.toUpperCase())){
            return ALIGNS.get(format.toUpperCase());
        } else {
            return null;
        }
    }
}
