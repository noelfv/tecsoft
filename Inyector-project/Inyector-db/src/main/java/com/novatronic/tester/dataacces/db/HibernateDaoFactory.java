/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.tester.dataacces.db;

import com.novatronic.tester.dataacces.AccesTransaction;
import com.novatronic.tester.dataacces.dao.AcquirerConnectionDao;
import com.novatronic.tester.dataacces.dao.DAOFactory;
import com.novatronic.tester.dataacces.dao.TransactionDao;
import org.apache.log4j.Logger;
import org.hibernate.Session;

/**
 *
 * @author ofernandez
 */
public class HibernateDaoFactory extends DAOFactory {

    private final static Logger log = Logger.getLogger(HibernateDaoFactory.class);

    private GenericHibernateDao instantiateDAO(Class daoClass) {
        try {
            GenericHibernateDao dao = (GenericHibernateDao) daoClass.newInstance();
            dao.setSession(getCurrentSession());
            return dao;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Can not instantiate DAO: "
                    + daoClass, ex);
        }
    }

    protected Session getCurrentSession() {
        return HibernateUtil.getSessionFactory().getCurrentSession();
    }

    public AccesTransaction getAccesTransaction(){
        DBAccesTransaction dbAccesTransaction =
                new DBAccesTransaction(this.getCurrentSession());
        return dbAccesTransaction;
    }

    @Override
    public AcquirerConnectionDao getAcquirerConnectionDao(AccesTransaction accesTransaction) {
        DBAccesTransaction dbAccesTransaction = null;
        if (!(accesTransaction instanceof DBAccesTransaction)) {
            throw new IllegalArgumentException("El accesTransaction no es "
                    + "compatible con HibernateDaoFactory");
        } else {
            dbAccesTransaction = (DBAccesTransaction) accesTransaction;
        }

        DBAcquirerConnectionDao dbAcqConnectionDao =
                (DBAcquirerConnectionDao) instantiateDAO(DBAcquirerConnectionDao.class);
        dbAcqConnectionDao.setSession(dbAccesTransaction.getSession());
        return dbAcqConnectionDao;
    }

    @Override
    public TransactionDao getTransactionDao(AccesTransaction accesTransaction) {
        DBAccesTransaction dbAccesTransaction = null;
        if (!(accesTransaction instanceof DBAccesTransaction)) {
            throw new IllegalArgumentException("El accesTransaction no es "
                    + "compatible con HibernateDaoFactory");
        } else {
            dbAccesTransaction = (DBAccesTransaction) accesTransaction;
        }

        DBTransactionDao dbTransactionDao =
                (DBTransactionDao) instantiateDAO(DBTransactionDao.class);
        dbTransactionDao.setSession(dbAccesTransaction.getSession());
        return dbTransactionDao;
    }
}
