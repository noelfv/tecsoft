/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.states;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Omar
 */
public abstract class State {
    private Map<String, State> transitions;
    private String name;
    
    public State(){
        transitions = new HashMap<String, State>();
    }
    
    public void addTransition(String actionName, State state){
        transitions.put(actionName, state);
    }
    
    public State next(String actionName){
        return transitions.get(actionName);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String toString(){
        String result = "State:" + name + ":\n";
        
        for (Map.Entry<String, State> entry : transitions.entrySet()) {
            result += "[Action:" + entry.getKey() + "->";
            result += "State:" + entry.getValue().getName() + "]\n";
        }
        
        return result;
    }
    
    public abstract void execute();
    
}
