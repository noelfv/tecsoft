/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.gui;

import com.novatronic.tester.domain.AcquirerConnection;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author ofernandez
 */
public class AcquirerConnectionTableModel extends AbstractTableModel{
    private String[] columnNames = {
        "ID","AcquirerName","Nº Transacc.","ConnectionMode", "ConnectionType",
        "Host", "IP","Port", "ServeAppl", "UserName", "UserPassword"};
    private List<AcquirerConnection> acquirerConnectionList;

    public AcquirerConnectionTableModel(List<AcquirerConnection> acquirerConnectionList){
        super();
        this.acquirerConnectionList = acquirerConnectionList;
    }
    
    @Override
    public int getRowCount() {
        return this.acquirerConnectionList.size();
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
        if (rowIndex >= this.acquirerConnectionList.size()){
            throw new IllegalArgumentException("rowIndex invalido");
        }
        AcquirerConnection acquirerConnection = this.acquirerConnectionList.get(rowIndex);
        switch (columnIndex){
            case 0: return acquirerConnection.getId();
            case 1: return acquirerConnection.getAcquirerName();
            case 2: return acquirerConnection.getNumberTransactions();
            case 3: return acquirerConnection.getConnectionMode();
            case 4: return acquirerConnection.getConnectionType();
            case 5: return acquirerConnection.getHost();
            case 6: return acquirerConnection.getIp();
            case 7: return acquirerConnection.getPort();
            case 8: return acquirerConnection.getServerAppl();
            case 9: return acquirerConnection.getUserName();
            case 10: return acquirerConnection.getUserPassword();
            default: throw new IllegalArgumentException("columnIndex invalido");
        }
    }

}
