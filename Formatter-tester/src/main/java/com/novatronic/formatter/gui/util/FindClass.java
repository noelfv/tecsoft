/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.util;

import com.novatronic.formatter.gui.exception.GUIException;

/**
 *
 * @author Omar
 */
public class FindClass {
    public static Class getClass(String name) throws GUIException{
        try {
            Class claz = Class.forName(name);
            return claz;
        } catch (ClassNotFoundException ex) {
            throw new GUIException("No se puede ubicar la clase " + name, ex);
        }
    }
}
