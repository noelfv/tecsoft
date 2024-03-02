/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.tester.connection.tcpclient;

import com.novatronic.tester.connection.Connection;
import com.novatronic.tester.connection.ConnectionFactory;
import com.novatronic.tester.domain.AcquirerConnection;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ofernandez
 */
public class TCPClientConnectionTest {
    private static final Logger log = LoggerFactory.getLogger(TCPClientConnectionTest.class);
    private ConnectionFactory connectionFactory;
    
    public TCPClientConnectionTest() {
        connectionFactory = new TCPClientConnectionFactory();
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of connect method, of class TCPClientConnection.
     */
    @Test
    public void testConnect() {
        log.info("--------------------- testConnect -----------------------");
        Connection instance;
        AcquirerConnection acquirerConnection;
        ServerMock server;
        
        server = new ServerMock(9001, 10000, 2000,false);
        server.start();
        acquirerConnection = new AcquirerConnection(5,"localhost", "9001");
        
        await(1000);
        instance = connectionFactory.getConnection(acquirerConnection);
        instance.connect();
        
        assertTrue(((TCPClientConnection)instance).isConnected());
        instance.disconnnect();
        log.info("Test OK");
    }
    
    private void await(long miliseconds){
        try {
            Thread.sleep(miliseconds);
        } catch (InterruptedException ex) {
            log.error("Se interrumpio la espera",ex);
        }
    }

    /**
     * Test of execute method, of class TCPClientConnection.
     */
    @Test
    public void testExecute() {
        log.info("--------------------- testExecute -----------------------");
        Connection instance;
        AcquirerConnection acquirerConnection;
        ServerMock server;
        String request;
        String response;
        
        server = new ServerMock(9000, 10000, 2000,true);
        server.setResponse("respuesta...");
        server.start();
        acquirerConnection = new AcquirerConnection(5,"localhost", "9000");
        request = "mensaje test";
        
        await(1000);
        instance = connectionFactory.getConnection(acquirerConnection);
        instance.connect();
        response = instance.execute(request);
        log.debug("Recibido:[{}]", response);
        
        assertEquals(server.getResponse(), response);
        
        instance.disconnnect();
        log.info("Test OK");
    }
}
