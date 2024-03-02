/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.dataacces.db;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author ofernandez
 */
public interface GenericDao<T, ID extends Serializable> {

    public T findById(ID id, boolean lock);

    public List<T> findAll();

    public Long count();
}
