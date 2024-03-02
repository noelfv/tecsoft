/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.connection;

import com.novatronic.tester.exception.TesterConnectionException;
import com.novatronic.tester.exception.TesterTimeOutConnectionException;

/**
 *
 * @author ofernandez
 */
public interface Connection {
    public final static String SIXCLIENT_KEY = "SIXCLIENTE";
    public final static String TCPCLIENT_KEY = "TCPCLIENTE";
    public final static String TCPSERVIDOR_KEY = "TCPSERVIDOR";

    /**
     * Establece conexion con con el "servidor". Esta comunicación se puede
     * establecer en varios modos como: servidor, cliente o vía el apiSix.
     * @throws TesterTimeOutConnectionException Si se produce un timeout al
     * tratar de establecer conexión hacia el servidor o por espera de conexión.
     * @throws TesterConnectionException Si se produce al intentar establecer
     * conexión hacia el servidor.
     */
    public void connect() throws TesterTimeOutConnectionException, TesterConnectionException;

    /**
     *
     * @param tramaOutput La trama a ser enviada a traves de esta conexion.
     * @return La trama de respuesta
     * @throws IllegalArgumentException Si la trama pasada como parametro es nula
     * @throws TesterTimeOutConnectionException En caso de obtener un timeout de
     * conexion.
     */
    public String execute(String tramaOutput) throws TesterConnectionException;

    /**
     * @throws TesterConnectionException
     */
    public void disconnnect() throws TesterConnectionException;
}
