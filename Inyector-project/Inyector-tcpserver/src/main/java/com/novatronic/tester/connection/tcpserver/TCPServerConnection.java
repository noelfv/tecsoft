/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.connection.tcpserver;

import com.novatronic.tester.connection.Connection;
import com.novatronic.tester.connection.tcpserver.exceptions.TCPServerConnectionException;
import com.novatronic.tester.exception.TesterTimeOutConnectionException;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import org.apache.log4j.Logger;

/**
 *
 * @author ofernandez
 */
public class TCPServerConnection implements Connection {

    private static final Logger log = Logger.getLogger(TCPServerConnection.class);
    private DataInputStream in;
    private DataOutputStream out;
    private String ip;
    private int port;
    private int timeOut;
    private ServerSocket server;
    private Socket socket;
    private boolean connect;

    /**
     * {@inheritDoc }
     * @param ip Ip destino hacia la cual ser realizar&aacute; la conexi&oacute;n
     * @param port Puerto por el cual se enviar&aacute; los datos
     * @param timeOut Tiempo de espera m&aacutexima al establecer la conexi&oacute;n
     * y no haber recibido respuesta. Este parametro debe ser enviado en 
     * milisegundos.
     */
    public TCPServerConnection(String ip, int port, int timeOut) {
        this.ip = ip;
        this.port = port;
        this.timeOut = timeOut;
        this.connect = false;
    }

    /**
     *
     * @throws TesterTimeOutConnectionException
     * @throws TCPServerConnectionException
     */
    @Override
    public void connect() {
        log.debug("procesando conexion, socket[" +socket + "]");
        if((socket != null) && !socket.isClosed()){
            log.debug("Socket ya esta conectado, no se realiza conexion");
            return;
        }
        log.debug("Server desconectado, se realiza conexion");
        
        OutputStream rawOut;
        InputStream rawIn;

        try {
            server = new ServerSocket(port);
            server.setSoTimeout(timeOut);
            this.connect = true;
            log.debug("Esperando por clientes con timeout=" + timeOut + ", port=" + port);
            socket = server.accept();       //Esto genera un bloqueo de espera
            log.debug("Cliente conectado:" + socket);
            socket.setSoTimeout(timeOut);
            log.debug("Timeout de Lectura establecido a:" + timeOut);
            rawOut = socket.getOutputStream();
            rawIn = socket.getInputStream();
            BufferedOutputStream buffOut = new BufferedOutputStream(rawOut);
            out = new DataOutputStream(buffOut);
            in = new DataInputStream(rawIn);
        } catch (SocketTimeoutException ex) {
            throw new TesterTimeOutConnectionException("Timeout al esperar conexion"
                    + " de clientes", ex);
        } catch (IOException ex) {
            throw new TCPServerConnectionException("No es posible abrir el "
                    + "puerto de escucha:" + this.port, ex);
        }catch (Exception ex) {
            throw new TCPServerConnectionException("No es posible conectarse al"
                    + " servidor y obtener los flujos de entrada y salida", ex);
        }
    }

    /**
     *
     * @param tramaOutput
     * @return
     * @throws IllegalArgumentException
     * @throws TCPServerConnectionException
     * @throws TesterTimeOutConnectionException
     */
    @Override
    public String execute(String tramaOutput) {
        Long tam;
        int length;
        int size;
        String tramaInput;
        byte[] dataOutput;
        byte[] dataInput;

        if (tramaOutput == null) {
            throw new IllegalArgumentException("El argumento no puede ser nulo");
        }

        try {
            log.debug("Realizando conexion");
            this.connect();             //Conexion a demanda
            log.debug("Ejecutando envio de tramas");
            /*Enviamos la trama e iniciamos el timer*/
            tam = new Long(tramaOutput.length());
            size = new Long(tam).intValue();
            dataOutput = tramaOutput.toString().getBytes();
            out.writeShort(size);
            out.write(dataOutput);
            out.flush();

            /*Leemos la trama recibida*/
            /*Leemos la cabecera*/
            length = in.readShort();
            log.debug("Se recibio como cabecera[" + length + "]");
            /*Leemos los datos*/
            dataInput = new byte[length];
            length = in.read(dataInput);
            log.debug("cantidad de caracters leidos, trama[" + length + "]");
            tramaInput = new String(dataInput);
            log.debug("Leimos [" + tramaInput + "]");

            /*Devolvemos los datos*/
            return tramaInput;
        } catch (EOFException ex) {
            throw new TCPServerConnectionException("Se recibio data nula y no "
                    + "es posble procesarla", ex);
        } catch (IOException ex) {
            throw new TCPServerConnectionException("Los flujos I/O han sido "
                    + "cerrados", ex);
        } catch (TesterTimeOutConnectionException ex) {
            throw new TesterTimeOutConnectionException(ex);
        } catch (Exception ex) {
            throw new TCPServerConnectionException("error generico en el "
                    + "cliente", ex);
        }
    }

    /**
     * {@inheritDoc  }
     * Desactiva el timer que controla el timeout, cierra los flujos de entrada
     * y salida, Cierra el socket al cliente.
     */
    @Override
    public synchronized void disconnnect() {
        try {
            connect = false;
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

    public int getTimeOut(){
        return this.timeOut;
    }

    public boolean isConnected(){
        return this.connect;
    }
}
