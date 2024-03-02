/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.tester.util;

import com.novatronic.tester.exception.TesterException;

/**
 *
 * @author ofernandez
 */
public class ClassHelper {

    public static <T> T getInstance(Class<T> claz) {
        try {
            return (T) claz.newInstance();
        } catch (InstantiationException ex) {
            throw new TesterException("No se pudo intancias la clase: "
                    + claz, ex);
        } catch (IllegalAccessException ex) {
            throw new TesterException("No se pudo acceder a la clase: "
                    + claz, ex);
        }
    }
    
    public static <I> I getInstance(Class clazInstance, Class<I> clasInt) {
        try {
            return (I) clazInstance.newInstance();
        } catch (InstantiationException ex) {
            throw new TesterException("No se pudo intancias la clase: "
                    + clazInstance, ex);
        } catch (IllegalAccessException ex) {
            throw new TesterException("No se pudo acceder a la clase: "
                    + clazInstance, ex);
        }
    }
}
