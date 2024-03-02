/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.report;

/**
 *
 * @author ofernandez
 */
public class ReportItem {
    private String id;
    private String acquireIn;
    private String acquireOut;
    private String sixRecieved;
    private String result;

    public String getAcquireIn() {
        return acquireIn;
    }

    public void setAcquireIn(String acquireIn) {
        this.acquireIn = acquireIn;
    }

    public String getAcquireOut() {
        return acquireOut;
    }

    public void setAcquireOut(String acquireOut) {
        this.acquireOut = acquireOut;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getSixReceived() {
        return sixRecieved;
    }

    public void setSixRecieved(String sixRecieved) {
        this.sixRecieved = sixRecieved;
    }

    @Override
    public String toString() {
        return "ReportItem{" + "id=" + id + ",acquireIn=" + acquireIn
                + ",acquireOut=" + acquireOut + ",sixRecieved=" + sixRecieved
                + ",result=" + result + '}';
    }
}
