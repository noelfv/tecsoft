/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.tester.connection.tcpclient;

import com.novatronic.tester.connection.Connection;
import com.novatronic.tester.connection.tcpclient.exception.TCPClientConnectionException;
import com.novatronic.tester.exception.TesterConnectionException;
import com.novatronic.tester.exception.TesterTimeOutConnectionException;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ofernandez
 */
public class TCPClientConnection implements Connection {

    private static final Logger log = LoggerFactory.getLogger(TCPClientConnection.class);
    private DataInputStream in;
    private DataOutputStream out;
    private String ip;
    private int port;
    private int timeOut;
    private Socket socket;
    private SocketAddress sockaddr;
    private boolean connect;

    /**
     * @param ip Ip destino hacia la cual ser realizar&aacute; la conexi&oacute;n
     * @param port Puerto por el cual se enviar&aacute; los datos. Este puerto
     * puede ser en modo servidor(escucha) o cliente(envio)
     * @param timeOut Tiempo de espera m&aacutexima al establecer la conexi&oacute;n
     * y no haber recibido respuesta. Este parametro debe ser enviado en milisegundos
     * @throws TCPClientConnectionException Si el ip pasado como par&aacute;metro
     * no puede ser determinada.
     */
    public TCPClientConnection(String ip, int port, int timeOut) {
        this.ip = ip;
        this.port = port;
        this.timeOut = timeOut;
        try {
            InetAddress addr = InetAddress.getByName(this.ip);
            sockaddr = new InetSocketAddress(addr, this.port);
        } catch (UnknownHostException ex) {
            throw new TCPClientConnectionException("Servidor desconocido:" + ip, ex);
        }
        this.connect = false;
    }

    /**
     * {@inheritDoc }
     * @throws TesterTimeOutConnectionException {@inheritDoc }
     * @throws TesterConnectionException {@inheritDoc }
     */
    @Override
    public void connect() {
        log.debug("Procesando conexion... socket=" + socket);
        if((socket != null) && !socket.isClosed()){
            log.debug("Socket ya esta conectado, no se realiza conexion "
                    + "[socket=" + socket + ",connected=" + socket.isConnected()
                    + ", closed=" + socket.isClosed() + "]");
            return;
        }
        log.debug("Socket desconectado, se realiza conexion");
        /*Realizamos la conexion*/
        socket = new Socket();
        OutputStream rawOut;
        InputStream rawIn;

        try {
            socket.setSoTimeout(timeOut);
            socket.connect(sockaddr, timeOut);
            rawOut = socket.getOutputStream();
            rawIn = socket.getInputStream();
            BufferedOutputStream buffOut = new BufferedOutputStream(rawOut);
            out = new DataOutputStream(buffOut);
            in = new DataInputStream(rawIn);
            log.debug("socket conectado [socket=" + socket + ",connected="
                    + socket.isConnected() + ", closed=" + socket.isClosed() + "]");
            this.connect = true;
        } catch (Exception ex) {
            throw new TCPClientConnectionException("No es posible conectarse al"
                    + " servidor y obtener los flujos de entrada y salida", ex);
        }
    }

    /**
     * {@inheritDoc  }
     */
    @Override
    public String execute(String tramaOutput) {
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
            this.connect();
            log.debug("Ejecutando envio de tramas");
            /*Enviamos la trama e iniciamos el timer*/
            dataOutput = tramaOutput.toString().getBytes();
            size = dataOutput.length;
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
        } catch (SocketTimeoutException ex) {
            throw new TesterTimeOutConnectionException("Se ha producido un"
                    + " timeout de conexion", ex);
        } catch (EOFException ex) {
            throw new TCPClientConnectionException("Se recibio data nula y no es"
                    + " posble procesarla", ex);
        } catch (IOException ex) {
            throw new TCPClientConnectionException("Los flujos I/O han sido "
                    + "cerrados", ex);
        } catch (TesterTimeOutConnectionException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new TCPClientConnectionException("error generico en el "
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
                log.debug("cerrando DataOutputStream");
                out.close();
            }
            if (in != null) {
                log.debug("cerrando DataInputStream");
                in.close();
            }
            if (socket != null) {
                log.debug("cerrando Socket");
                socket.close();
            }
            log.debug("Desconexion realizada");
        } catch (IOException ex) {
            log.debug("Ocurrio un error al intentar cerrar los flujos "
                    + "de entrada y salida", ex);
        }
    }

    public int getTimeOut(){
        return this.timeOut;
    }

    public boolean isConnected(){
        return this.connect;
    }
}
