/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.engine;

import com.novatronic.tester.domain.AcquirerConnection;
import com.novatronic.tester.report.ReportItem;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ofernandez
 */
public class TestResult {
    private String acquirerName;
    private Long testLength;
    private Long testFailled;
    private Long testSuccesful;
    private TestState state = TestState.NOT_STARTED;
    private AcquirerConnection acquirerConnection;
    private final List<ReportItem> reportItemList;

    public TestResult(String acquirerName, Long testLength, Long testFailled,
            Long testSuccesful,AcquirerConnection acquirerConnection) {
        this.acquirerName = acquirerName;
        this.testLength = testLength;
        this.testFailled = testFailled;
        this.testSuccesful = testSuccesful;
        this.acquirerConnection = acquirerConnection;
        reportItemList = new ArrayList<ReportItem>();
    }

    public void addReportItem(ReportItem reportItem){
        this.reportItemList.add(reportItem);
    }

    public List<ReportItem> getReportItemList(){
        return this.reportItemList;
    }

    public void clearTest(){
        this.testFailled = 0L;
        this.testSuccesful = 0L;
        this.state = TestState.NOT_STARTED;
    }

    public String getAcquirerName() {
        return acquirerName;
    }

    public void setAcquirerName(String acquirerName){
        this.acquirerName = acquirerName;
    }

    public TestState getState() {
        return state;
    }

    public void setState(TestState state) {
        this.state = state;
    }

    public Long getTestFailled() {
        return testFailled;
    }

    public void setTestFailled(Long testFailled) {
        this.testFailled = testFailled;
    }

    public Long getTestLength() {
        return testLength;
    }

    public void setTestLength(Long testLength){
        this.testLength = testLength;
    }

    public Long getTestSuccesful() {
        return testSuccesful;
    }

    public void setTestSuccesful(Long testSuccesful) {
        this.testSuccesful = testSuccesful;
    }

    public AcquirerConnection getAcquirerConnection() {
        return acquirerConnection;
    }

    public void setAcquirerConnection(AcquirerConnection acquirerConnection) {
        this.acquirerConnection = acquirerConnection;
    }
    

}
