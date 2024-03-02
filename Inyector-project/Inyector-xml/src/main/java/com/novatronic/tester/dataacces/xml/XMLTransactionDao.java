/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.tester.dataacces.xml;

import com.novatronic.tester.dataacces.dao.TransactionDao;
import com.novatronic.tester.dataacces.exception.XMLException;
import com.novatronic.tester.dataacces.util.Util;
import com.novatronic.tester.domain.Transaction;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author ofernandez
 */
public class XMLTransactionDao implements TransactionDao<Long> {

    private final static Logger log = Logger.getLogger(XMLTransactionDao.class);
    private InputStream inputStream;

    public XMLTransactionDao(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public Long countByAcquirer(String AcquirerId) {
        
        return null;
    }

    @Override
    public List<Transaction> getByAcquirer(Long AcquirerId) {
        log.debug("Obteniendo Adquiriente[" + AcquirerId + "]");
        List<Transaction> listTransaction = new ArrayList<Transaction>();

        if (AcquirerId == null) {
            throw new IllegalArgumentException("argumento invalido para countByAcquirer");
        }
        Element acquirer = getAcquirerNode(String.valueOf(AcquirerId));
        if (acquirer == null) {
            log.debug("Adquiriente no encontrado:[" + String.valueOf(AcquirerId) + "]");
            return listTransaction;
        }
        List transactions = acquirer.getChildren("transaction");
        if (!transactions.isEmpty()) {
            Iterator iterator = transactions.iterator();
            while (iterator.hasNext()) {
                Element e = (Element) iterator.next();
                listTransaction.add(Util.retrieveTransaction(e));
            }
        }
        return listTransaction;
    }

    @Override
    public Transaction findById(Long id, boolean lock) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Transaction> findAll() {
        List<Transaction> listTransaction = null;

        Document doc = this.getDocument();
        if (doc == null) {
            throw new IllegalStateException("Document is null");
        }
        Element raiz = doc.getRootElement();
        // Se obtiene todos los clientes conectados
        List clients = raiz.getChildren("client");
        if (clients.isEmpty()) {
            return null;
        }
        listTransaction = new ArrayList<Transaction>();
        Iterator iteratorClient = clients.iterator();
        while (iteratorClient.hasNext()) {
            Element client = (Element) iteratorClient.next();
            // Se obtiene todas las transacciones
            List transactions = client.getChildren("transaction");
            if (!transactions.isEmpty()) {
                Iterator iterator = transactions.iterator();
                while (iterator.hasNext()) {
                    Element e = (Element) iterator.next();
                    listTransaction.add(Util.retrieveTransaction(e));
                }
            }
        }


        return listTransaction;
    }

    @Override
    public Long count() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected Element getAcquirerNode(String id) {
        boolean found = false;
        Element client = null;
        try {
            Document doc = this.getDocument();
            if (doc == null) {
                throw new IllegalStateException("Document is null");
            }
            Element raiz = doc.getRootElement();
            // Se obtiene todos los clientes conectados
            List clients = raiz.getChildren("client");
            if (clients.isEmpty()) {
                return null;
            }
            Iterator iteratorClient = clients.iterator();
            // Se busca el cliente
            while (iteratorClient.hasNext() && !found) {
                client = (Element) iteratorClient.next();
                if (client.getAttributeValue("id").equalsIgnoreCase(id)) {
                    found = true;
                }
            }
            // Si no existe el adquiriente devuelve nulo
            return found ? client : null;

        } catch (NullPointerException ex) {
            log.error("Id client does not exist " + ex.getMessage());
            throw new XMLException("Node or attribute is null ", ex);
        }
    }

    protected Document getDocument() {
        Document document = null;
        try {
            SAXBuilder builder = new SAXBuilder(false);
            document = builder.build(inputStream);

            return document;

        } catch (JDOMException ex) {
            log.error("Cannot read the xml " + ex.getMessage());
            throw new XMLException("Cannot read the xml  ", ex);
        } catch (IOException ex) {
            log.error("Error with xml file " + ex.getMessage());
            throw new XMLException("Error with xml file ", ex);
        }
    }
}
