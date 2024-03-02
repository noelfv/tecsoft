/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.connection.sixclient;

import com.novatronic.components.sixclient.tcp.ClienteSIXConnectionFactory;
import com.novatronic.components.sixclient.tcp.ClienteSIXExeBean;
import com.novatronic.components.sixclient.tcp.exception.ClienteSIXConnectionException;
import com.novatronic.tester.connection.Connection;
import com.novatronic.tester.connection.sixclient.exception.ApiSixTrxException;
import com.novatronic.tester.connection.sixclient.exception.SixClientTesterConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author ofernandez
 */
public class SixClientConnection implements Connection{
    private static final Logger log = LoggerFactory.getLogger(SixClientConnection.class);
    
    private ClienteSIXConnectionFactory factory;
    
    private static final String ID_SIXCLIENT_CONN = "ID_SIX";

    /**
     * El SixCliente no requiere inicio, lo realiza a demanda;
     */
    @Override
    public void connect() {}

    /**
     *
     * @param tramaOutput
     * @return SixClientTesterConnectionException
     */
    @Override
    public String execute(String tramaOutput){
        
        com.novatronic.components.Connection connection = null;
        ClienteSIXExeBean request;
        ClienteSIXExeBean response;
        
        try {
            log.debug("Mensaje por enviar=[{}]", tramaOutput);
            connection = factory.getConnection(ID_SIXCLIENT_CONN);
            log.trace("Conexion, obtenida. Preparando envio");
            request = new ClienteSIXExeBean();
            request.setId(ID_SIXCLIENT_CONN);
            request.setTrama(tramaOutput);
            log.trace("Enviando trama...");
            response = (ClienteSIXExeBean) connection.execute(request);
            log.debug("Se Recibio=[{}]", response);
            connection.liberar();
            
            return response.getTrama();

        } catch (ClienteSIXConnectionException ex) {
            log.error("Hubo un error al enviar el mensaje", ex);
            throw new ApiSixTrxException("Hubo un error al enviar el mensaje", ex);
        } catch (Exception ex) {
            log.error("Hubo un error al enviar el mensaje", ex);
            throw new ApiSixTrxException("Hubo un error al enviar el mensaje", ex);
        } finally{
            if(connection != null){
                try{
                    connection.liberar();
                }catch(Exception ex){
                    log.error("No se pudo liberar la conexion del SIX", ex);
                }
            }
            
        }
    }

    public void setSixFactory(ClienteSIXConnectionFactory factory) {
        this.factory = factory;
    }

    /**
     * @throws SixClientTesterConnectionException
     */
    @Override
    public void disconnnect(){
        try{
            this.factory.destroy();
        }  catch (Exception ex) {
           throw new SixClientTesterConnectionException("Sixfactory generar "
                   + "error al realizar close()",ex);
        }
    }
    
    
}
