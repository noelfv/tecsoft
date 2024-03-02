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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import org.apache.log4j.Logger;


/**
 *
 * @author nteruya
 */
public class XMLFactory extends DAOFactory {

    private final static Logger log = Logger.getLogger(XMLFactory.class);

    @Override
    public AcquirerConnectionDao getAcquirerConnectionDao(AccesTransaction accesTransaction) {
        XMLAccesTransaction access = (XMLAccesTransaction) accesTransaction;
        FileInputStream is = null;
        try {
            is = new FileInputStream("connections.xml");
            access.setInputStream(is);
        } catch (FileNotFoundException ex) {
            throw new XMLException("Cannot found the file transactions.xml" + ex.getMessage());
        }
        XMLAcquirerConnectionDao xmlAcquirerConnecionDao = new XMLAcquirerConnectionDao(is);
        return xmlAcquirerConnecionDao;
    }

    @Override
    public TransactionDao getTransactionDao(AccesTransaction accesTransaction) {
        FileInputStream is = null;
        XMLAccesTransaction access = (XMLAccesTransaction)accesTransaction;
        try {
            is = new FileInputStream("transactions.xml");
            access.setInputStream(is);
        } catch (FileNotFoundException ex) {
            throw new XMLException("Cannot found the file transactions.xml" +  ex.getMessage());
        }

        XMLTransactionDao xmlTransactionDao = new XMLTransactionDao(is);
        return xmlTransactionDao;
    }

   
    @Override
    public AccesTransaction getAccesTransaction() {
        XMLAccesTransaction xmlAccesTransaction = new XMLAccesTransaction();          
        return xmlAccesTransaction;
    }

    
   

}
