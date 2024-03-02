/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.util;

import com.novatronic.formatter.gui.exception.GUIException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author Omar
 */
public class FindResource {

    private static final Logger log = Logger.getLogger(FindResource.class);
    private static final String TSTR = "Caught Exception while in Loader.getResource. This may be innocuous.";

    public static Element getXMLElement(String fileName) throws GUIException{
        InputStream is;
        Document doc;
        Element root;
        
        log.debug("Leyendo configuracion del CP:[" + fileName + "]");
        try {
            is =FindResource.get(fileName).openStream();
            doc = (new SAXBuilder()).build(is);
            root = doc.getRootElement();
            log.debug("XML obtenido");
            
            return root;
        } catch (JDOMException ex) {
            throw new GUIException("Formato invalido para el "
                    + "archivo [" + fileName + "]", ex);
        } catch (IOException ex) {
            throw new GUIException("No es posible leer el archivo"
                    + " [" + fileName + "]", ex);
        }
    }

    public static URL get(String resource) {
        ClassLoader classLoader;
        ClassLoader defaultLoader;
        URL url;

        try {

            /*url = ClassLoader.getSystemResource(resource);
            return url;
            */

            classLoader=FindResource.class.getClassLoader();
            if (classLoader != null) {
                log.trace("Trying to find [" + resource + "] using context classloader "
                        + classLoader + ".");
                url = classLoader.getResource(resource);
                if (url != null) {
                    return url;
                }
            }


            // We could not find resource. Let us now try with the classloader that loaded this class.
            classLoader = FindResource.class.getClassLoader();
            if (classLoader != null) {
                log.trace("Trying to find [" + resource + "] using " + classLoader + " class loader.");
                url = classLoader.getResource(resource);
                if (url != null) {
                    return url;
                }
            }

            // We could not find resource. Finally try with the default ClassLoader.
            defaultLoader = new String().getClass().getClassLoader();
            if (defaultLoader != null) {
                log.trace("Trying to find [" + resource + "] using " + defaultLoader + " class loader.");
                url = defaultLoader.getResource(resource);
                if (url != null) {
                    return url;
                }
            }


        }  catch (Exception t) {
            //
            //  can't be InterruptedException or InterruptedIOException
            //    since not declared, must be error or RuntimeError.
            log.error(TSTR, t);
        }

        // Last ditch attempt: get the resource from the class path. It
        // may be the case that clazz was loaded by the Extentsion class
        // loader which the parent of the system class loader. Hence the
        // code below.
        log.trace("Trying to find [" + resource + "] using ClassLoader.getSystemResource().");
        return ClassLoader.getSystemResource(resource);


    }
/*
    private static ClassLoader getTCL() throws IllegalAccessException, InvocationTargetException {
        ClassLoader cl;
        if (System.getSecurityManager() == null) {
            cl = Thread.currentThread().getContextClassLoader();
        } else {
            cl = (ClassLoader) java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedAction() {

                        @Override
                        public Object run() {
                            return Thread.currentThread().getContextClassLoader();
                        }
                    });
        }
        return cl;
    }*/
}
