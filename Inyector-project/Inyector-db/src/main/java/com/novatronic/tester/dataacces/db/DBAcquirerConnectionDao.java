/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.dataacces.db;

import com.novatronic.tester.dataacces.dao.AcquirerConnectionDao;
import com.novatronic.tester.domain.AcquirerConnection;
import java.util.List;
import org.hibernate.Query;

/**
 *
 * @author ofernandez
 */
public class DBAcquirerConnectionDao extends GenericHibernateDao<AcquirerConnection,Long>
        implements AcquirerConnectionDao<Long>{
    private static final String QRY_NUMBER_TRANSACTIONS = "select count(*) from DBTransaction where id=:idAcquirer";
    private static final String ID_ACQUIRER = "idAcquirer";

    @Override
    public List<AcquirerConnection> findAll(){
        List<AcquirerConnection> acquirerList = super.findAll();
        for (AcquirerConnection acquirerConnection : acquirerList) {
            Query q = this.getSession()
                    .createQuery(QRY_NUMBER_TRANSACTIONS)
                    .setLong(ID_ACQUIRER, acquirerConnection.getId());
            Long number = (Long)q.uniqueResult();
            acquirerConnection.setNumberTransactions(number);
        }
        return acquirerList;
    }
}
