/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.engine;

/**
 *
 * @author ofernandez
 */
public interface TestListener {
    public void setInit(int row);

    public void setFailled(Long value, int row);

    public void setSuccesful(Long value, int row);

    public void setState(TestState value, int row);
}
