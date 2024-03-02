/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.connection.tcpserver;

import com.novatronic.tester.connection.Connection;
import com.novatronic.tester.connection.ConnectionFactory;
//import com.novatronic.tester.dataacces.AcquirerConnection;
import com.novatronic.tester.domain.AcquirerConnection;

/**
 *
 * @author ofernandez
 */
public class TCPServerConnectionFactory extends ConnectionFactory {

    @Override
    public Connection getConnection(AcquirerConnection acquirerConnection) {
        String ip = null;
        String port = null;
        String timeout = null;
        int portNumber = 0;
        int timeoutNumber = 0;
        TCPServerConnection tcpServerConnection = null;

        try{
        ip = acquirerConnection.getIp();
        port = acquirerConnection.getPort();
        portNumber = Integer.parseInt(port);
        timeoutNumber = acquirerConnection.getTimeOut();

        tcpServerConnection = new
                TCPServerConnection(ip, portNumber, timeoutNumber*1000);

        return tcpServerConnection;

        } catch(NumberFormatException ex){
            throw new IllegalArgumentException("el formato del puerto[" +
                    port + "] y el timeout[" + timeout + "] son invalidos", ex);
        }
    }

}
