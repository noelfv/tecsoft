/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Omar
 */
public class Datas {
    public static final String ACTUAL_TEST = "ACTUAL_TEST";
    public static final String TEST_GROUP = "TEST_GROUP";
    public static final String TEST_SUITE = "TEST_SUITE";
    public static final String MEMORY_DATA_USED = "MEMORY_DATA_USED";
    public static final String MEMORY_DATA_TOTAL = "MEMORY_DATA_TOTAL";
    public static final String CPU_USAGE = "CPU_USAGE";
    
    public static final String PERF_TEST_TIME = "PERF_TEST_TIME";
    public static final String PERF_EXEC_PER_THREAD = "PERF_EXEC_PER_THREAD";
    public static final String PERF_THREADS_NUMBER = "PERF_EXEC_PER_SECOND";
    public static final String PERF_THREAD_DELAY = "PERF_THREAD_DELAY";
    public static final String PERF_GRAPH_REFRESH = "PERF_GRAPH_REFRESH";
    public static final String PERF_EXEC_DIRECTION = "PERF_EXEC_DIRECTION";
    public static final String PERF_IS_PLAY = "PERF_IS_PLAY";
    public static final String PERF_EXIST_DATA = "PERF_EXIST_DATA";
    public static final String PERF_TIMER_GRAPH = "PERF_TIMER_GRAPH";
    public static final String PERF_MEM_INIT_POS = "PERF_MEM_INIT_POS";
    public static final String PERF_CPU_INIT_POS = "PERF_CPU_INIT_POS";
    public static final String PERF_MEM_END_POS = "PERF_MEM_END_POS";
    public static final String PERF_CPU_END_POS = "PERF_CPU_END_POS";
    public static final String PERF_RESULTS = "PERF_RESULTS";
    public static final String PERF_SERVICE = "PERF_SERVICE";
    public static final String PERF_TIME_INIT = "PERF_TIME_INIT";
    public static final String PERF_TIME_END = "PERF_TIME_END";
    
    
    private static Datas dataManager;
    
    static{
        dataManager = new Datas();
    }
    
    public static Datas getInstance(){
        return dataManager;
    }
    
    private Map<String, Object> data;
    
    private Datas(){
        data = new HashMap<String, Object>();
    }
    
    /**
     * Agrega un nuevo objeto al repositorio de datos. si el objeto ya existe
     * previamente, este sera reemplazado por el nuevo
     * @param key La clave del objeto por agregar
     * @param object El objeto por agregar
     */
    public void add(String key, Object object){
        data.put(key, object);
    }
    
    /**
     * Devuelve el objeto guardado segun su clave.
     * @param key La clave del objeto a obtener
     * @return El objeto guardado o null en caso no exista
     */
    public Object get(String key){
        return data.get(key);
    }
    
    /**
     * Remueve un objeto guardado segun la clave recibida
     * @param key La clave del objeto a remover
     * @return El ojeto guardado anteriormente o null en caso no exista alguno
     * anterior
     */
    public Object remove(String key){
        return data.remove(key);
    }
    
    /**
     * Obtiene un bojeto del repositorio realiznado un casteo del mismo a partir
     * de la clase pasada como parametro. Si el objeto noe xites se devuelve un
     * null
     * @param <D> El tipo a retornar el cual depende de la clase pasada como 
     * parametro para el casteo respectivo
     * @param key La clave a usar para buscar el objeto
     * @param clazz La clase a partir de la cual se realizará el casteo
     * @return Un objeto del tipo pasado por parametro o null en caso de no 
     * existir
     */
    public <D>D get(String key, Class <D> clazz){
        Object obj = data.get(key);
        if(obj == null){
            return null;
        }else{
            return (D)obj;
        }
    }
}
