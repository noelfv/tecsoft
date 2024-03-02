/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.generator;

import com.novatronic.formatter.Formatter;
import com.novatronic.formatter.FormatterFactory;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 * @author ofernandez
 * @version 1.0
 * @since 1.0, 12/11/2010
 */
public abstract class FrameGenerator {

    private static final Logger log = Logger.getLogger(FrameGenerator.class);
    private Map<String, Client> clients;
    private static FormatterFactory formatterFactory;

    /**
     *
     */
    public void readConfiguration(Element configuration) {
        clients = new LinkedHashMap<String, Client>();
        readCustomConfiguration(configuration);
        configureClients(configuration.getChildren(Tag.CLIENT));
    }

    private void configureClients(List<Element> generatorFieldAsList) {
        log.debug("Configurando clientes...");
        for (int i = 0; i < generatorFieldAsList.size(); i++) {
            Client client = new Client();
            client.readConfiguration(generatorFieldAsList.get(i));
            clients.put(client.getId(), client);
            log.debug("Cliente agregado :" + client);
        }
        log.debug("Clientes agregados:" + clients);
    }

    /**
     * 
     */
    protected abstract void readCustomConfiguration(Element configuration);

    /**
     * 
     */
    public abstract String generateFrames();

    /**
     * 
     * @return
     */
    public static Formatter getFormatter(String id) {
        return formatterFactory.getFormatter(id);
    }

    /**
     *
     * @param formatter
     */
    public void setFormatterFactory(FormatterFactory formatterFactory) {
        log.debug("Agregando formatterFactory:" + formatterFactory);
        FrameGenerator.formatterFactory = formatterFactory;
    }

    /**
     *
     * @return
     */
    public int getNumberOfClients() {
        return clients.size();
    }

    /**
     * 
     * @return
     */
    public Map<String, Client> getClients(){
        return clients;
    }

    /**
     * 
     * @return
     */
    public List<Client> getClientsAsList(){
        return new ArrayList<Client>(clients.values());
    }

    public interface Tag {

        public static final String CLIENT = "client";
    }
}
