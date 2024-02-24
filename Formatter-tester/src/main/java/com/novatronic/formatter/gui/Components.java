/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui;

import java.awt.Component;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * //TODO encapsular excepciones al extraer componentes
 * @author Omar
 * @version 1.0
 * @since 19 Feb 2012
 */
public class Components {
    private Map<String, Map> groups;
    private Map<String, Component> components;
    
    private static Components componentsManager;
    
    static{
        componentsManager = new Components();
    }
    
    public static Components getInstance(){
        return componentsManager;
    }
    
    private Components(){
        groups = new HashMap<String, Map>();
        components = new HashMap<String, Component>();
    }
    
    /**
     * Agrega un componente al mapa de componentes. Usa el nombre del componente
     * como la clave para insertarlo en el Mapa.
     * @param component El componente por agregar
     */
    public void add(Component component){
        components.put(component.getName(), component);
    }
    
    /**
     * Agrega un componente al mapa de componentes. Ademas, asigna como nombre
     * del componente, el nombre pasado como parametro, para luego una vez mas
     * usarlo como clave al insertarlo en el Mapa.
     * @param component El componente por agregar
     * @param name El nombre del componente
     */
    public void add(Component component, String name){
        component.setName(name);
        add(component);
    }
    
    /**
     * Agrega un componente a un grupo de componentes y cuyo nombre esta dado
     * por groupName. Ademas, agrega dicho componente al mapa de componentes 
     * usando su nombre como clave al insertalo. En caso de no existir el grupo
     * previamente, se crea un nuevo grupo antes de insertarlo.
     * @param component El componente por agregar
     * @param groupName El nombre del grupo donde se agregara
     */
    public void addToGroup(Component component, String groupName){
        Map<String, Component> group = groups.get(groupName);
        if (group == null){
            group = new HashMap<String, Component>();
            groups.put(groupName, group);
        }
        group.put(component.getName(), component);
        add(component);
    }
    
    /**
     * Agrega un componente a un grupo de componentes y cuyo nombre esta dado
     * por groupName. Ademas, asigna a este componente el nombre dado por name
     * y agrega dicho componente al mapa de componentes dicho nombre como clave
     * al insertalo. En caso de no existir el grupo previamente, se crea un 
     * nuevo grupo antes de insertarlo.
     * @param component El componente por agregar
     * @param name El nombre del componente por asignar
     * @param groupName El nombre del grupo donde se agregara
     */
    public void addToGroup(Component component, String name, String groupName){
        component.setName(name);
        addToGroup(component, groupName);
    }
    
    /**
     * Devuelve un componente extraido de acuerdo al nombre pasado por parametro.
     * Dicho nombre se usa como clave para extraerlo del mapa
     * @param name El nombre del componente a extraer.
     * @return El componente o null en caso no encontrarse dicho componente.
     */
    public Component get(String name){
        return components.get(name);
    }
    
    /**
     * Devuelve un componente extraido de acuerdo al nombre pasado por parametro
     * casteandolo de acuerdo al tipo type pasado como parametro. Dicho nombre
     * se usa como clave para extraerlo del mapa
     * @param <T> El tipo que se usara para castearlo
     * @param name El nombre del componente a extraer.
     * @param type La clase de acuerdo a la cual se realizara el casteo.
     * @return El componente o null en caso no encontrarse dicho componente.
     */
    public <T>T get(String name, Class <T> type){
        return (T)components.get(name);
    }
    
    /**
     * Devuelve un componente extraido de acuerdo al nombre y el nombre de grupo
     * pasados como parametros, usando dichos valores para extraerlos de los
     * mapas respectivos.
     * @param name El nombre del componente a extraer.
     * @param groupName El nombre del grupo desde donde se extraera
     * @return El componente o null en caso no encontrarse dicho componente en 
     * dicho grupo
     */
    public Component get(String name, String groupName){
        Map<String, Component> group = groups.get(groupName);
        if (group == null){
            return null;
        }
        return group.get(name);
    }
    
    /**
     * Devuelve un componente extraido de acuerdo al nombre y el nombre de grupo
     * pasados como parametros, usando dichos valores para extraerlos de los
     * mapas respectivos. Adicionalmente castea dicho componente al tipo pasado
     * como parametro.
     * @param <T> El tipo al cual sera castedo.
     * @param name El nombre del componente a extraer.
     * @param type El tipo al cual sera casteado el componente
     * @param groupName El nombre del grupo desde donde se extraera
     * @return El componente o null en caso no encontrarse dicho componente en 
     * dicho grupo
     */
    public <T> T get(String name, Class <T> type, String groupName){
        return (T)get(name,groupName);
    }
    
    @Override
    public String toString(){
        String value = "Componentes:\n";
        Component component;
        
        Iterator<Component> it = components.values().iterator();
        while(it.hasNext()){
            component = it.next();
            value += "[" + component.getName() + "," + component.getClass().getSimpleName() + "]\n";
        }
        return value;
    }
}
