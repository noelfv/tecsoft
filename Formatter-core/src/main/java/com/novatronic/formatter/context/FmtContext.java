/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.context;

import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author ofernandez
 */
public class FmtContext {
    private Map<String, Object> context;
    
    public FmtContext(){
        context = new TreeMap<String, Object>();
    }
    /**
     * Obtiene un dato del este contexto
     * @param key EL identificador por el cual se buscara el dato
     * @return EL objeto consultado o null en caso de no existir.
     */
    public Object getData(String key){
        return context.get(key);
    }
    
    /**
     * Coloca un nuevo dato en este contexto.
     * @param key La clave con la cual se colocara el dato en este contexto
     * @param data El objeto por almacenar
     * @return El objeto que existia previamente o null en caso de no existir
     * alguno previo.
     */
    public Object putData(String key, Object data){
        return context.put(key, data);
    }
    
    /**
     * Consulta si una clave existe en este contexto
     * @param key La clave a consultar
     * @return true si la clave existe, false en cualquier otro caso.
     */
    public boolean contains(String key){
        return context.containsKey(key);
    }
}
