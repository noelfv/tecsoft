/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.gui;

import com.novatronic.tester.engine.TestListener;
import com.novatronic.tester.engine.TestResult;
import com.novatronic.tester.engine.TestState;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author ofernandez
 */
public class TestResultTableModel extends AbstractTableModel implements TestListener{
    private String[] columnNames = {
        "AcquirerName","Nº Test","TestFailled", "TestSucccesfull","State"};
    private List<TestResult> testResultList;

    public List<TestResult> getTestResultList() {
        return testResultList;
    }

    public TestResultTableModel(List<TestResult> testResultList){
        super();
        this.testResultList = testResultList;
    }

    @Override
    public int getRowCount() {
        return this.testResultList.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex >= this.testResultList.size()){
            throw new IllegalArgumentException("rowIndex invalido");
        }
        TestResult testResult = this.testResultList.get(rowIndex);
        switch (columnIndex){
            case 0: return testResult.getAcquirerName();
            case 1: return testResult.getTestLength();
            case 2: return testResult.getTestFailled();
            case 3: return testResult.getTestSuccesful();
            case 4: return testResult.getState().toString();
            default: throw new IllegalArgumentException("columnIndex invalido");
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {

        fireTableCellUpdated(row, col);
    }

    @Override
    public void setInit(int row) {
        this.setValueAt(0, row, 2);
        this.setValueAt(0, row, 3);
    }

    @Override
    public void setFailled(Long value, int row){
        this.setValueAt(value, row, 2);
    }

    @Override
    public void setSuccesful(Long value, int row){
        this.setValueAt(value, row, 3);
    }

    @Override
    public void setState(TestState value, int row){
        this.setValueAt(value, row, 4);
    }

}
