/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.generator.xml;

import com.novatronic.generator.Client;
import com.novatronic.generator.Transaction;
import java.util.List;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 * @author ofernandez
 * @version 1.0
 * @since 1.0, 12/11/2010
 */
public class ClientElement extends Element {
    private static final Logger log = Logger.getLogger(ClientElement.class);
    private static final String CLIENT = "client";
    private static final String ATT_ID = "id";
    
    public ClientElement(Client client){
        super(CLIENT);
        log.debug("Creando clientElement con cliente=" + client);
        this.setAttribute(ATT_ID,client.getId());
        List<Transaction> transactions = client.getTransactions();
        log.debug("Agregando transacciones[" + transactions.size() + "]");
        for(int i = 0;i < transactions.size();i++){
            this.addContent(new TransactionElement(transactions.get(i), client.getId()));
        }
    }


}
