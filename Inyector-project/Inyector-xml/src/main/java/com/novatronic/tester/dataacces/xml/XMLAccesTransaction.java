/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.dataacces.xml;

import com.novatronic.tester.dataacces.AccesTransaction;
import com.novatronic.tester.dataacces.exception.XMLException;
import java.io.InputStream;


/**
 *
 * @author ofernandez
 */
public class XMLAccesTransaction implements AccesTransaction{
    
    private boolean initTransaction = false;
    private InputStream inputStream = null;
    
    
   
    public XMLAccesTransaction(){}

    public XMLAccesTransaction(String resource){
        inputStream = getResourceAsStream(resource);
    }
    /**
     *
     * @param resource
     * @return
     */
    private static InputStream getResourceAsStream(String resource) {
        return getResourceAsStream(XMLAccesTransaction.class, resource);
    }

    /**
     *
     * @param klass
     * @param resource
     * @return
     */
    private  static InputStream getResourceAsStream(Class klass, String resource) {
        if (klass == null) {
            throw new NullPointerException("Objeto klass cannot be null");
        }
        if (resource == null) {
            throw new NullPointerException("Object resource cannot be null");
        }
        String stripped = resource.startsWith("/") ? resource.substring(1) : resource;
        InputStream stream = null;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader != null) {
            System.out.println("Buscando " + resource + " con Classloader del Hilo actual");
            stream = classLoader.getResourceAsStream(stripped);
        }
        if (stream == null) {
            System.out.println("Buscando " + resource + " con objeto class");
            stream = klass.getResourceAsStream(resource);
        }
        if (stream == null) {
            System.out.println("Buscando " + resource + " con el Classloader de la clase");
            stream = klass.getClassLoader().getResourceAsStream(stripped);
        }
        if(stream == null){
            System.out.println("Buscando " + resource + " con el Classloader system resource de la clase");
            stream = ClassLoader.getSystemResourceAsStream(stripped);
        }
        if (stream == null) {
            System.out.println(resource + " no encontrado");
        }
        return stream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    

    public InputStream getInputStream() {
        return inputStream;
    }

    
    @Override
    public void beginTransaction() {        
    }


    @Override
    public void endTransaction() {
        try{
            if(inputStream != null){
                inputStream.close();                
            }
        } catch (Exception  ex){
            throw new XMLException("Error closing XMLAccesTransaction ", ex);
        }
    }

    @Override
    public void rollBack() {        
    }

    @Override
    public void close() {
        try{
            if(inputStream != null){                
                inputStream.close();                
            }
        } catch (Exception  ex){
            throw new XMLException("Error closing XMLAccesTransaction ", ex);
        }
    }


}
