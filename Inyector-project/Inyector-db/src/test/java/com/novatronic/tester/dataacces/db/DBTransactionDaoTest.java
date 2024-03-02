/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.dataacces.db;

import com.novatronic.tester.dataacces.AccesTransaction;
import com.novatronic.tester.dataacces.dao.DAOFactory;
import com.novatronic.tester.dataacces.dao.TransactionDao;
import com.novatronic.tester.domain.Transaction;
import java.util.List;
import junit.framework.TestCase;
import org.apache.log4j.Logger;

/**
 *
 * @author ofernandez
 */
public class DBTransactionDaoTest extends TestCase {
    private static final Logger log = Logger.getLogger(DBTransactionDaoTest.class);
    
    private static final Class DaoFactoryClass = HibernateDaoFactory.class;

    public DBTransactionDaoTest(String testName) {
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

    public void testTransactionsByAdquirerId() {
        log.debug("---------- testTransactionsByAdquirerId");
        Long idAcquirerConnection = 621000L;
        List<Transaction> transactionList;

        DAOFactory daoFactory = DAOFactory.instance(DaoFactoryClass);
        AccesTransaction trans = daoFactory.getAccesTransaction();
        TransactionDao transactionDao = daoFactory.getTransactionDao(trans);
        trans.beginTransaction();
        transactionList = transactionDao.getByAcquirer(idAcquirerConnection);
        trans.endTransaction();

        log.debug(transactionList);
        
    }

}
