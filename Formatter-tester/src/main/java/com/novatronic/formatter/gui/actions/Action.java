/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.actions;

import com.novatronic.formatter.gui.states.StateMachine;
import java.awt.Component;

/**
 *
 * @author Omar
 */
public abstract class Action<C extends Component> {
    private StateMachine stateMachine;
    private String name;

    public static final String ACTION_ERROR = "ACTION_ERROR";
    
    public Action(){
        
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStateMachine(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }
    
    /**
     * Hace que la accion pueda hacer una transición hacia el siguiente estado.
     * En este caso, se usa el nombre de la accion para decidir a que estado
     * se debe continuar. Es decir, este nombre es el nombre de la transicion a
     * realizar.
     */
    public void nextState(){
        stateMachine.nextState(name);
    }
    
    /**
     * Hace que la accion pueda hacer una transición hacia el siguiente estado,
     * pero de acuerdo al nombre de la tranasicion pasado como parametro.
     * Este metodo se uliza en caso se deba forzar a una transicion pero que no
     * utiliza el nombre de la accion directamente. Por ejemplo se produjo un
     * error y se debe forzar dicha transcion
     * @param transitionName El nombre de la transicion a utilizar
     */
    public void nextState(String transitionName){
        stateMachine.nextState(transitionName);
    }
    
    /**
     * Realiza una transcion hacia un estado de error en caso de haberse producido
     * al realizar la accion o en caso lo amerite. Tener en cuenta que el
     * transitar a un siguiente estado dependera de la configuacion de esta
     * utlima.
     */
    public void nextErrorState(){
        stateMachine.nextState(Action.ACTION_ERROR);
    }
    
    /**
     * Subscribe esta accion al componente pasado por parametro
     * @param component El componente al cual se agregara la accion
     */
    public abstract void subscribe(C component);
}
