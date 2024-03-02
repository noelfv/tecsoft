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
public class AcquirerConnection {

    private Long id;
    private String acquirerBin;
    private String acquirerName;
    private Long numberTransactions;
    private String connectionType;
    private String connectionMode;
    private String ip;
    private String port;
    private String host;
    private String userName;
    private String userPassword;
    private String serverAppl;
    private String sixDriverType;
    private Integer timeOut;
    private Long sleep;
    private String userIn;
    private Date dateIn;
    private String userChange;
    private Date dateChange;

    public AcquirerConnection() {
    }

    public AcquirerConnection(Integer timeOut, String ip, String port) {
        this.timeOut = timeOut;
        this.ip = ip;
        this.port = port;
    }
    
    public String getAcquirerBin() {
        return acquirerBin;
    }

    public void setAcquirerBin(String acquirerBin) {
        this.acquirerBin = acquirerBin;
    }

    public String getAcquirerName() {
        return acquirerName;
    }

    public void setAcquirerName(String acquirerName) {
        this.acquirerName = acquirerName;
    }

    public Long getNumberTransactions() {
        return numberTransactions;
    }

    public void setNumberTransactions(Long numberTransactions) {
        this.numberTransactions = numberTransactions;
    }

    public String getConnectionMode() {
        return connectionMode;
    }

    public void setConnectionMode(String connectionMode) {
        this.connectionMode = connectionMode;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    public Long getSleep() {
        return sleep;
    }

    public void setSleep(Long Sleep) {
        this.sleep = Sleep;
    }

    public Integer getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(Integer timeOut) {
        this.timeOut = timeOut;
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

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getServerAppl() {
        return serverAppl;
    }

    public void setServerAppl(String serverAppl) {
        this.serverAppl = serverAppl;
    }

    public String getSixDriverType() {
        return sixDriverType;
    }

    public void setSixDriverType(String sixDriverType) {
        this.sixDriverType = sixDriverType;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }
}