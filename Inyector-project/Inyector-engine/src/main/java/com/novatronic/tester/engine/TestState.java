/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.engine;

/**
 *
 * @author ofernandez
 */
public enum TestState {
    /**
     * Estado PAUSED es cuando el motor detiene sus tareas pero podria
     reiniciarlas. Solo es posible llegar a este estado a partir del estado
     STARTED*/
    PAUSED("PAUSED"),

    /**
     * Estado STOPPED es cuando el motor se detuvo de sus actividades aun si
     * este no concluyo sus tareas. Es posible llegar a este estado desde los
     * estados PAUSED o STARTED.
     */
    STOPPED("STOPPED"),

    /**
     * El estado NOT_STARTED es cuando el motor no ha sido iniciado ningua vez.
     * El otor asigna este estado en su creacion. No es posible llegar a este
     * estado nuevamente.
     */
    NOT_STARTED("NOT_STARTED"),

    /**
     * El estado STARTED es cuando el motor inicio sus tareas y esta en ejecucion.
     * Se puede llgar a este estado desde el estado NOT_STARTED, STOPPED o FINISHED
     */
    RUNNING("RUNNING"),
    
    /**
     * El estado FINISHED se logra cuando el motor logro terminar sus tareas de
     * manera normal. Es posible llegar a este estado a partir del estado STARTED.
     * El motor es quien trasalada este estado.
     */
    FINISHED("FINISHED"),

    /**
     * El estado STEP_FINISHED se alcanza cuando al ejecutar el motor paso a paso
     * este termina un paso por cada hilo TestWorker, y estara a la espera de que
     * se le notifique la ejecucion del siguiente paso.
     */
    STEP_FINISHED("STEP_FINISHED");

    TestState(String state){
        this.state = state;
    }
    private final String state;

    public String getState() {
        return state;
    }

    @Override
    public String toString(){
        return state;
    }
}
