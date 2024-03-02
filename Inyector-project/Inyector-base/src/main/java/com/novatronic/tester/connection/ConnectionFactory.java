/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.connection;

import com.novatronic.tester.domain.AcquirerConnection;
import com.novatronic.tester.exception.TesterConnectionException;

/**
 *
 * @author ofernandez
 */
public abstract class ConnectionFactory  {
    
    public static ConnectionFactory getConnectionFactory(Class connection){
        try {
            return (ConnectionFactory)connection.newInstance();
        } catch (Exception ex) {
            throw new TesterConnectionException("No es posible crea la Connection: "
                    + connection, ex);
        }
    }

    /**
     * 
     * @param acquirerConnection
     * @return Objeto {@link Connection Connection} con el cual es posible
     * establecer la via de comunicacion en un modo (servidor puro, cliente puro,
     * sixClient).
     * @throws TesterConnectionException
     */
    public abstract Connection getConnection(AcquirerConnection acquirerConnection);
}
