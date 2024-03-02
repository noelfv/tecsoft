/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.util;

import java.io.InputStream;
import org.apache.log4j.Logger;

/**
 *
 * @author ofernandez
 */
public class ResourceHelper {
    private static Logger log = Logger.getLogger(ResourceHelper.class);

    public static InputStream find(String resource) {

        String stripped = resource.startsWith("/") ? resource.substring(1) : resource;
        InputStream stream = null;

        /*Buscando recurso por Hilo*/
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader != null) {
            log.debug("Buscando [" + resource + "] con Classloader del Hilo actual");
            stream = classLoader.getResourceAsStream(stripped);
        }

        /*Buscando recurso por Objeto de Clase*/
        if (stream == null) {
            log.debug("Buscando [" + resource + "] con objeto class");
            stream = ResourceHelper.class.getResourceAsStream(resource);
        }

        /*Buscando recurso por Classloader de Clase*/
        if (stream == null) {
            log.debug("Buscando [" + resource + "] con el Classloader de la clase");
            stream = ResourceHelper.class.getClassLoader().getResourceAsStream(stripped);
        }

        /*El recurso no pudo ser encontrado*/
        if (stream == null) {
            log.debug(resource + " no encontrado");
        }
        return stream;
    }
}
