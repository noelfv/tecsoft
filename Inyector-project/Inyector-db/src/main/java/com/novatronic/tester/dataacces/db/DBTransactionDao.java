/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.dataacces.db;

import com.novatronic.tester.dataacces.dao.TransactionDao;
import com.novatronic.tester.domain.Transaction;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author ofernandez
 */
public class DBTransactionDao extends GenericHibernateDao<Transaction,Long>
        implements TransactionDao<Long>{

    private static final String ID_ACQUIRER = "id";
    //private static final String QUERY_LIST = "from EFT_TST_TRANSACTION where";

    @Override
    public Long countByAcquirer(String AcquirerId){
        if ((AcquirerId == null) || AcquirerId.equals("")){
            throw new IllegalArgumentException("argumento invalido para countByAcquirer");
        }
        Criteria crit = getSession().createCriteria(getPersistentClass());
        crit.setProjection(Projections.rowCount())
            .add(Restrictions.eq(ID_ACQUIRER, AcquirerId));
        Long count = (Long)crit.list().get(0);
        return count;
    }

    @Override
    public List<Transaction> getByAcquirer(Long AcquirerId){
        if (AcquirerId == null){
            throw new IllegalArgumentException("argumento invalido para countByAcquirer");
        }

        List transactionList = getSession().createCriteria(getPersistentClass())
                .add( Restrictions.eq(ID_ACQUIRER, AcquirerId.toString()))
                .list();
        return transactionList;
    }

}
