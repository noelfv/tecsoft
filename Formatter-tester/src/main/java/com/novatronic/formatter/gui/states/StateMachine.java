/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.states;

import com.novatronic.formatter.gui.util.FindClass;
import com.novatronic.formatter.gui.exception.GUIException;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public class StateMachine {

    private static final Logger log = Logger.getLogger(StateMachine.class);
    private Map<String, State> states;
    private State currentState;
    private static final String INIT_STATE_NAME = "INIT";

    public StateMachine() {
        states = new HashMap<String, State>();
    }

    public void setUpStates(String fileName) {
        log.debug("Creando Maquina de Estados...");
        XMLReader xmlReader = new XMLReader();
        
        xmlReader.readFile(fileName);
        log.debug("Creando estados...");
        xmlReader.createStates(this);

        log.debug("Creando transiciones...");
        xmlReader.createTransitions(this);

        log.debug("Maquina de estados creada");
    }
    
    public State getState(String stateId){
        return states.get(stateId);
    }

    public void createState(String stateName, String className){
        createState(stateName, FindClass.getClass(className));
    }

    private <C extends State> void createState(String stateName, Class<C> clazz) {
        State state;
        try {
            state = clazz.newInstance();
            state.setName(stateName);
            states.put(stateName, state);

        } catch (InstantiationException ex) {
            log.error("No se pudo instanciar la clase:" + clazz, ex);
        } catch (IllegalAccessException ex) {
            log.error("No se pudo instanciar la clase:" + clazz, ex);
        }

    }

    public void addTransition(State state, String actionName, String stateName) {
        state.addTransition(actionName, states.get(stateName));
    }

    public void nextState(String actionName) {
        log.debug("Current State:" + currentState.getName() + ", ActionName:" + actionName);
        State state = currentState.next(actionName);
        if (state == null) {
            log.debug("No hay transicion para esta accion. Se mantiene el estado");
        } else {
            state.execute();
            currentState = state;
            log.debug("New State:" + currentState.getName());
        }
    }

    public void init() {
        currentState = states.get(INIT_STATE_NAME);
        currentState.execute();
        log.debug("Current state:" + currentState.getName());
    }

    @Override
    public String toString() {
        String result = "Current State:" + currentState.getName() + "\n";

        for (Map.Entry<String, State> entry : states.entrySet()) {
            result += entry.getValue().toString();
        }

        return result;
    }
}
