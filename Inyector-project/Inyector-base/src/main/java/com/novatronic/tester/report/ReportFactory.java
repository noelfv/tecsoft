/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.report;

import com.novatronic.tester.exception.TesterException;

/**
 *
 * @author ofernandez
 */
public abstract class ReportFactory {
    
    public static Report getReport(Class report){
        try {
            return (Report)report.newInstance();
        } catch (Exception ex) {
            throw new TesterException("No es posible crear Report: "
                    + report, ex);
        }
    }
}
