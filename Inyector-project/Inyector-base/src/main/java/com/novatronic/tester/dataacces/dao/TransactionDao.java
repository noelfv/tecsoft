/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.dataacces.dao;

import com.novatronic.tester.domain.Transaction;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author ofernandez
 * @param <ID>
 */
public interface TransactionDao <ID extends Serializable>{

    public Transaction findById(ID id, boolean lock);

    public List<Transaction> findAll();

    public Long count();

    public Long countByAcquirer(String AcquirerId);

    public List<Transaction> getByAcquirer(Long AcquirerId);
}
