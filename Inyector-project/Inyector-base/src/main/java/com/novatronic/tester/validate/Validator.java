/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.validate;

import com.novatronic.tester.domain.Transaction;
import com.novatronic.tester.engine.TestResult;

/**
 *
 * @author ofernandez
 */
public interface Validator {
    /**
     * 
     * @param transaction Objeto que contiene los datos de la prueba ejecutada.
     * @param tramaOut Trama recibida la cual se compara con la primera trama
     * @return true en caso las dos sean iguales, false en caso de encontrar
     * alguna diferencia o si la tramaOut es nula.
     * @throws IllegalStateException 
     */
    public boolean validate(Transaction transaction, String tramaOut);

    /**
     *
     * @return
     * @throws IllegalStateException
     */
    public String getResult();

    /**
     *
     * @param testResult
     */
    public void setTestResult(TestResult testResult);
}
