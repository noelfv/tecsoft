/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.tester.connection.tcpclient;

import com.novatronic.tester.exception.TesterTimeOutConnectionException;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ofernandez
 */
public class ServerMock extends Thread {

    private static final Logger log = LoggerFactory.getLogger(ServerMock.class);
    private ServerSocket server;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private int timeOut;
    private int port;
    private long await;
    private boolean haveToRead;
    private String response;

    public ServerMock(int port, int timeOut, long await, boolean haveToRead) {
        this.port = port;
        this.timeOut = timeOut;
        this.haveToRead = haveToRead;
        this.await = await;
    }

    @Override
    public void run() {
        connect();
    }

    public void connect() {
        OutputStream rawOut;
        InputStream rawIn;

        try {
            server = new ServerSocket(port);
            server.setSoTimeout(timeOut);
            log.debug("Esperando por clientes con timeout=" + timeOut + ", port=" + port);
            socket = server.accept();       //Esto genera un bloqueo de espera
            log.debug("Cliente conectado:" + socket);
            socket.setSoTimeout(timeOut);
            log.debug("Timeout de Lectura establecido a:" + timeOut);
            rawOut = socket.getOutputStream();
            rawIn = socket.getInputStream();
            out = new DataOutputStream(rawOut);
            in = new DataInputStream(rawIn);
            
            readAndResponse();
            await();
            
        } catch (SocketTimeoutException ex) {
            throw new TesterTimeOutConnectionException("Timeout al esperar conexion"
                    + " de clientes", ex);
        } catch (Exception ex) {
            throw new TesterTimeOutConnectionException("Error general", ex);
        }

    }

    private void readAndResponse() {
        int length;
        byte[] bytes;
        
        try {
            if (haveToRead) {
                log.debug("Leyendo y respondiendo");
                length = in.readShort();
                bytes = new byte[length];
                length = in.read(bytes);
                log.debug("Leimos:[{}][{}]", length, new String(bytes));
                log.debug("Respondemos...");
                bytes = response.getBytes();
                out.writeShort(bytes.length);
                out.write(bytes);
                out.flush();
            }else{
                log.debug("No se lee ni responde");
            }
        } catch (Exception ex) {
            log.error("Error al responder", ex);
        }
    }

    private void await() {
        try {
            Thread.sleep(await);
        } catch (InterruptedException ex) {
            log.error("Se interrumpio la espera", ex);
        }
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    public void disconnnect() {
        try {
            log.debug("Realizando desconexion ...");
            if (out != null) {
                log.debug("cerrando out");
                out.close();
            }
            if (in != null) {
                log.debug("cerrando in");
                in.close();
            }
            if (socket != null) {
                log.debug("cerrando socket hacia al cliente");
                socket.close();
            }
            if (server != null) {
                log.debug("cerrando servidor");
                server.close();
            }
            log.debug("Desconexion realizada");
        } catch (IOException ex) {
            log.debug("Ocurrio un error al intentar cerra los flujos "
                    + "de enttrada y salida", ex);
        }
    }
}
