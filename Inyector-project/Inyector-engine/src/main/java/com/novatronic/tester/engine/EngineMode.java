/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.engine;

/**
 *
 * @author ofernandez
 */
public enum EngineMode {
    /**
     *
     */
    ALL_TEST("ALL_TEST"),

    /**
     * 
     */
    STEP_BY_STEP("STEP_BY_STEP");

    EngineMode(String mode){
        this.mode = mode;
    }
    private String mode;

    public String getMode() {
        return mode;
    }

    @Override
    public String toString(){
        return mode;
    }
}
