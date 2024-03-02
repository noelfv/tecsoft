/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.dataacces;

/**
 *
 * @author ofernandez
 */
public interface AccesTransaction {
    
    public void beginTransaction();
    public void endTransaction();
    public void rollBack();
    public void close();
}
