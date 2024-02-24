/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.util;

import java.io.File;
import java.net.URL;
import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public class ResourceHelper {

    public static final Logger log = org.apache.log4j.Logger.getLogger(ResourceHelper.class);

    /**
     *
     * @param path
     * @return
     */
    public static URL findResource(String path) {
        URL url = null;
        File file;

        log.info("Buscando recurso a traves de el Context Classloader");
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null) {
            url = contextClassLoader.getResource(path);
        }
        if (url != null) {
            return url;
        }
        log.info("Buscando recurso via a traves del Classloader de Clases");
        url = ResourceHelper.class.getClassLoader().getResource(path);
        if (url != null) {
            return url;
        }
        log.info("Buscando recurso a traves del System Classloader");
        url = ClassLoader.getSystemClassLoader().getResource(path);
        if (url != null) {
            return url;
        }
        log.info("Buscando el recurso en el Directorio de Ejecucion");
        file = new File(path);
        if (file.exists()) {
            try {
                url = file.toURI().toURL();
                return url;
            } catch (Exception ex) {
                log.error("No fue posible encontrar el recurso via:" + path, ex);
                return null;
            }
        }else{
            return null;
        }
    }
}
