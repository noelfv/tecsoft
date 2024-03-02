/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.generator;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 * @author ofernandez
 * @version 1.0
 * @since 1.0, 12/11/2010
 */
public class Client {

    private Logger log = Logger.getLogger(Client.class);
    private List<Transaction> transactions;
    private String id;

    public Client() {
        transactions = new ArrayList<Transaction>();
    }

    public void readConfiguration(Element configuration) {
        log.debug("Client leyendo configuracion recibida:" + configuration);
        id = configuration.getAttributeValue(Attr.ID);
        createTransactions(configuration.getChildren(Tag.TRANSACTION));
        log.debug("Configuracion guardada, transactions=" + transactions);
    }

    private void createTransactions(List<Element> transactionsAsList) {
        for (int i = 0; i < transactionsAsList.size(); i++) {
            Transaction trans = new Transaction(transactionsAsList.get(i));
            transactions.add(trans);
            log.debug("[" + id + "]Transaction guardada:" + trans);
        }
    }

    /**
     *
     */
    public List<Transaction> getTransactions(){
        return this.transactions;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Client{" + "transactions=" + transactions + "id=" + id + '}';
    }

    public interface Attr {

        public static final String ID = "id";
    }

    public interface Tag {

        public static final String TRANSACTION = "transaction";
    }
}
