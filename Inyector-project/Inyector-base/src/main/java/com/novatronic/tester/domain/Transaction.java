/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.tester.domain;

import java.util.Date;

/**
 *
 * @author ofernandez
 */
public class Transaction {

    private String id;
    private String keyTxn;
    private String acquirerIn;
    private String acquirerOut;
    private String autorizedIn;
    private String autorizedOut;
    private String userIn;
    private Date dateIn;
    private String userChange;
    private Date dateChange;

    public String getAcquirerIn() {
        return acquirerIn;
    }

    public void setAcquirerIn(String acquirerIn) {
        this.acquirerIn = acquirerIn;
    }

    public String getAcquirerOut() {
        return acquirerOut;
    }

    public void setAcquirerOut(String acquirerOut) {
        this.acquirerOut = acquirerOut;
    }

    public String getAutorizedIn() {
        return autorizedIn;
    }

    public void setAutorizedIn(String autorizedIn) {
        this.autorizedIn = autorizedIn;
    }

    public String getAutorizedOut() {
        return autorizedOut;
    }

    public void setAutorizedOut(String autorizedOut) {
        this.autorizedOut = autorizedOut;
    }

    public Date getDateChange() {
        return dateChange;
    }

    public void setDateChange(Date dateChange) {
        this.dateChange = dateChange;
    }

    public Date getDateIn() {
        return dateIn;
    }

    public void setDateIn(Date dateIn) {
        this.dateIn = dateIn;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKeyTxn() {
        return keyTxn;
    }

    public void setKeyTxn(String keyTxn) {
        this.keyTxn = keyTxn;
    }

    public String getUserChange() {
        return userChange;
    }

    public void setUserChange(String userChange) {
        this.userChange = userChange;
    }

    public String getUserIn() {
        return userIn;
    }

    public void setUserIn(String userIn) {
        this.userIn = userIn;
    }

    @Override
    public String toString() {
        return "DBTransaction{" + "id=" + id + ",keyTxn=" + keyTxn
                + ",acquirerIn=" + acquirerIn + ",acquirerOut=" + acquirerOut
                + ",autorizedIn=" + autorizedIn + ",autorizedOut=" + autorizedOut
                + ",userIn=" + userIn + ",dateIn=" + dateIn + ",userChange="
                + userChange + ",dateChange=" + dateChange + "}\n";
    }


}
