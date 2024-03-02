/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.report;

import com.novatronic.tester.engine.TestResult;
import java.util.List;

/**
 *
 * @author ofernandez
 */
public interface Report {

    public void setTestResults(List<TestResult> testResult);

    /**
     * @return La ruta del archivo generado
     * @throws TesterReportException
     */
    public String generateReport();

    public void addTestResult(TestResult testResult);
}
