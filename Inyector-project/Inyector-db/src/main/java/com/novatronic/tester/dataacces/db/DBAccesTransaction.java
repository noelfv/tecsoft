/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.dataacces.db;

import com.novatronic.tester.dataacces.AccesTransaction;
import com.novatronic.tester.dataacces.db.exceptions.DBTesterException;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 *
 * @author ofernandez
 */
public class DBAccesTransaction implements AccesTransaction{
    private Session session;
    private Transaction transaction;
    private boolean initTransaction = false;
    
    public DBAccesTransaction(Session session) {
        this.session = session;
    }
    public Session getSession(){
        if (session == null) {
            throw new IllegalStateException("No ha sido asignado la session");
        }
        return session;
    }

    @Override
    public void beginTransaction() {
        try {
            if (this.session == null){
                throw new IllegalStateException("La sesion esta nula, debe "
                        + "obtener una sesion valida");
            }
            this.transaction = this.session.beginTransaction();
            this.initTransaction = true;
        } catch (Exception ex) {
            throw new DBTesterException("No fue posible iniciar la " +
                    "transaccion", ex);
        }
    }

    @Override
    public void endTransaction() {
        try {
            this.transaction.commit();
            this.initTransaction = false;
        } catch (Exception ex) {
            throw new DBTesterException("No fue posible finalizar la "
                    + "transaccion", ex);
        }
    }

    @Override
    public void rollBack() {
        try {
            if (this.transaction != null) {
                this.transaction.rollback();
            }
        } catch (Exception ex) {
            throw new DBTesterException("No fue posible realizar el rollback " +
                    "la transaccion", ex);
        }
    }

    @Override
    public void close() {
        try{
            if ((this.session != null) && this.session.isOpen()) {
                this.session.close();
            }
        } catch (Exception  ex){
            throw new DBTesterException("No fue posible cerrar la sesion", ex);
        }
    }

}
