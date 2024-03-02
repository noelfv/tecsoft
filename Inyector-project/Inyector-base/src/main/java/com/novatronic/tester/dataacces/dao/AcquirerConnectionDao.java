/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.dataacces.dao;


import com.novatronic.tester.domain.AcquirerConnection;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author ofernandez
 * @param <ID>
 */
public interface AcquirerConnectionDao <ID extends Serializable> {
    
    public AcquirerConnection findById(ID id, boolean lock);

    public List<AcquirerConnection> findAll();

    public Long count();
}
