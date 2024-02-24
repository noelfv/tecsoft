/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.actions;

import com.novatronic.formatter.gui.Components;
import com.novatronic.formatter.gui.states.StateMachine;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public abstract class ActionsBuilder {
    private static final Logger log = Logger.getLogger(ActionsBuilder.class);
    
    private StateMachine stateMachine;
    private Map<String, Action> actions;

    public ActionsBuilder(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
        actions = new HashMap<String, Action>();
    }
    
    public abstract void setUpActions();
    
    protected <C extends Component, A extends Action> A buildAction(
            String ComponentID, 
            Class<C> componentClaz, 
            Class<A> actionclaz) throws InstantiationException, IllegalAccessException{
        
        C component;
        A action;
        
        component = Components.getInstance().get(ComponentID, componentClaz);
        action = actionclaz.newInstance();
        action.setStateMachine(stateMachine);
        action.subscribe(component);
        action.setName(ComponentID);
        actions.put(ComponentID, action);
        
        return action;
    }
}
