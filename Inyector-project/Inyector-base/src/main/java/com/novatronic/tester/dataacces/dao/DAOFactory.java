/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.dataacces.dao;

import com.novatronic.tester.dataacces.AccesTransaction;
import com.novatronic.tester.exception.TesterException;

/**
 *
 * @author ofernandez
 */
public abstract class DAOFactory {
    
    public static DAOFactory instance(Class factory) {
        try {
            return (DAOFactory)factory.newInstance();
        } catch (Exception ex) {
            throw new TesterException("No es posible crea el DAOFactory: "
                    + factory, ex);
        }
    }

    public abstract AcquirerConnectionDao getAcquirerConnectionDao(AccesTransaction accesTransaction);

    public abstract TransactionDao getTransactionDao(AccesTransaction accesTransaction);

    public abstract AccesTransaction getAccesTransaction();
}
