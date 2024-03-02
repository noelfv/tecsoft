/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.tester.dataacces.xml;

import com.novatronic.tester.dataacces.AccesTransaction;
import com.novatronic.tester.dataacces.dao.AcquirerConnectionDao;
import com.novatronic.tester.dataacces.dao.DAOFactory;
import com.novatronic.tester.dataacces.dao.TransactionDao;
import com.novatronic.tester.dataacces.exception.XMLException;
import com.novatronic.tester.dataacces.util.Util;
import com.novatronic.tester.domain.AcquirerConnection;
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
public class XMLAcquirerConnectionDao implements AcquirerConnectionDao<Long> {

    private final static Logger log = Logger.getLogger(XMLAcquirerConnectionDao.class);
    private InputStream inputStream;

    public XMLAcquirerConnectionDao(InputStream inputStream){
        this.inputStream = inputStream;
    }
    
    @Override
    public AcquirerConnection findById(Long id, boolean lock) {
         AcquirerConnection acquirerConnection = null;

        try {
            if (inputStream == null) {
                throw new IllegalStateException("Cannot get the acquirer connection requested. XMLAcquirerConnection file no loaded");
            }
            List<AcquirerConnection> lista = findAll();
            if(lista == null){
                return null;
            }
            for(AcquirerConnection acq : lista){
                if(acq.getId().compareTo(id) == 0){                    
                    acquirerConnection = acq;
                }
            }
        } catch (XMLException ex) {
           log.error("XMLException " + ex.getMessage());
        }
        return acquirerConnection;
    }

    @Override
    public List<AcquirerConnection> findAll() {
            List<AcquirerConnection> listAcquirerConnection = null;
        try {
            SAXBuilder builder = new SAXBuilder(false);

            Document doc = builder.build(inputStream);
            if(doc == null){
                throw new IllegalStateException("Document is null");
            }
            Element raiz = doc.getRootElement();
            List connections = raiz.getChildren("connection");
            if(connections.isEmpty()){
                return null;
            }
            listAcquirerConnection = new ArrayList<AcquirerConnection>();
            Iterator i = connections.iterator();
            while (i.hasNext()) {
                AcquirerConnection acquirerConnection = new AcquirerConnection();
                Element e = (Element) i.next();
                Element element = e.getChild("acquirerBin");
                acquirerConnection.setAcquirerBin(element.getText());
                element = e.getChild("acquirerName");
                acquirerConnection.setAcquirerName(element.getText());
                element = e.getChild("connectionMode");
                acquirerConnection.setConnectionMode(element.getText());
                element = e.getChild("connectionType");
                acquirerConnection.setConnectionType(element.getText());
                element = e.getChild("sleep");
                acquirerConnection.setSleep(Util.parseLong(element.getText()));
                element = e.getChild("timeOut");
                acquirerConnection.setTimeOut(Util.parseInteger(element.getText()));
                element = e.getChild("dateChange");
                acquirerConnection.setDateChange(Util.parseDate(element.getText()));
                element = e.getChild("dateIn");
                acquirerConnection.setDateIn(Util.parseDate(element.getText()));
                element = e.getChild("host");
                acquirerConnection.setHost(element.getText());
                element = e.getChild("id");
                acquirerConnection.setId(Util.parseLong(element.getText()));
                acquirerConnection.setNumberTransactions(getNumberTransactions(
                        acquirerConnection.getId()));
                element = e.getChild("ip");
                acquirerConnection.setIp(element.getText());
                element = e.getChild("port");
                acquirerConnection.setPort(element.getText());
                element = e.getChild("serverAppl");
                acquirerConnection.setServerAppl(element.getText());
                element = e.getChild("sixDriverType");
                acquirerConnection.setSixDriverType(element.getText());
                element = e.getChild("userChange");
                acquirerConnection.setUserChange(element.getText());
                element = e.getChild("userIn");
                acquirerConnection.setUserIn(element.getText());
                element = e.getChild("userName");
                acquirerConnection.setUserName(element.getText());
                element = e.getChild("userPassword");
                acquirerConnection.setUserPassword(element.getText());
                listAcquirerConnection.add(acquirerConnection);
            }

        } catch (JDOMException ex) {
            log.error("Cannot read the connection properties " , ex);
            throw new XMLException("Cannot read the connection properties " + ex.getMessage());
        } catch (IOException ex) {
            log.error("Cannot read the connection properties " , ex);
            throw new XMLException("Cannot read the connection properties " + ex.getMessage());
        } 
        return listAcquirerConnection;
    }


    @Override
    public Long count() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    /**
     * Obtiene el  número de transacciones a partir del xml de
     * transacciones
     * @param acquirerId
     * @return
     */
    protected Long getNumberTransactions(Long acquirerId){
        DAOFactory factory = DAOFactory.instance(XMLFactory.class);
        AccesTransaction accesTransaction = factory.getAccesTransaction();
        TransactionDao transactionDao = factory.getTransactionDao(accesTransaction);
        List<Transaction> lista = transactionDao.getByAcquirer(acquirerId);
        accesTransaction.endTransaction();
        return (long)lista.size();
    }

}
