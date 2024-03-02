/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.dataacces.xml;

import com.novatronic.tester.dataacces.AccesTransaction;
import com.novatronic.tester.dataacces.dao.DAOFactory;
import com.novatronic.tester.dataacces.dao.TransactionDao;
import com.novatronic.tester.domain.Transaction;
import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author nteruya
 */
public class XMLTransactionDaoTest extends TestCase {

        
    public XMLTransactionDaoTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test para obtener todas las transacciones
     */
    public void testFindAllTransactions()  {
        System.out.println("Executing testFindAllTransactions...");
        DAOFactory xmlFactory = DAOFactory.instance(XMLFactory.class);
        AccesTransaction accesTransaction = xmlFactory.getAccesTransaction();       
        TransactionDao transactionDao = xmlFactory.getTransactionDao(accesTransaction);
        List<Transaction> lista = transactionDao.findAll();
        accesTransaction.endTransaction();
        if(lista == null){
            fail("lista is null");
        }
        int expResult = 3;
        assertEquals(expResult, lista.size());
  
        System.out.println("testFindAllTransactions done");
    }

    /**
     * Test para obtener todas las transacciones según el adquiriente
     */
    public void testFindByAcquirer(){
        System.out.println("Executing testFindByAcquirer...");
        XMLFactory xmlFactory = (XMLFactory) DAOFactory.instance(XMLFactory.class);
        AccesTransaction accesTransaction = xmlFactory.getAccesTransaction();
        TransactionDao transactionDao = xmlFactory.getTransactionDao(accesTransaction);
        List<Transaction> lista = transactionDao.getByAcquirer(Long.parseLong("7963"));
        accesTransaction.endTransaction();
        if(lista == null){
            fail("lista is null");
        }
        for(Transaction tnx : lista){
            System.out.println("Tnx: keyTnx=" + tnx.getKeyTxn() + ", id=" + tnx.getId() + ", acquirerOut=" + tnx.getAcquirerOut());
        }
        int expResult = 1;
        assertEquals(expResult, lista.size());
        System.out.println("testFindByAcquirer done");
    }

}