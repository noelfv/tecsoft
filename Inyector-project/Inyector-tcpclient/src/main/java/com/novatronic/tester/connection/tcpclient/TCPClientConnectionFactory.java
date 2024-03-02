/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.tester.connection.tcpclient;

import com.novatronic.tester.connection.Connection;
import com.novatronic.tester.connection.ConnectionFactory;
import com.novatronic.tester.connection.tcpclient.exception.TCPClientConnectionException;
import com.novatronic.tester.domain.AcquirerConnection;

/**
 *
 * @author ofernandez
 */
public class TCPClientConnectionFactory extends ConnectionFactory {

    /**
     * {@inheritDoc }
     *
     * @param acquirerConnection
     * @return
     * @throws TCPClientConnectionException
     * @throws IllegalArgumentException
     */
    @Override
    public Connection getConnection(AcquirerConnection acquirerConnection) {
        String ip;
        String port = null;
        int portNumber;
        int timeoutNumber;
        TCPClientConnection tcpClienteConnection;

        try {
            ip = acquirerConnection.getIp();
            port = acquirerConnection.getPort();
            portNumber = Integer.parseInt(port);
            timeoutNumber = acquirerConnection.getTimeOut();

            tcpClienteConnection = new TCPClientConnection(ip, portNumber, timeoutNumber * 1000);

            return tcpClienteConnection;

        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("el formato del puerto[" + port 
                    + "] es invalido", ex);
        }

    }
}
