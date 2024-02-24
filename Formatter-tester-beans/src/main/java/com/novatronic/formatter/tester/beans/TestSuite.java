/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.tester.beans;

import com.novatronic.formatter.FormatterFactory;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public class TestSuite {
    private static final Logger log = Logger.getLogger(TestSuite.class);
    
    private List<FormatterTest> tests;
    private String formatConfigFileName;
    private FormatterFactory formatFactory;
    
    public static final String FORMAT_CONFIG = "formatconfig";

    public TestSuite(String formatConfigFileName, FormatterFactory formatFactory) {
        this.formatConfigFileName = formatConfigFileName;
        this.formatFactory = formatFactory;
    }
    
    public FormatterTest getTest(int index){
        return this.tests.get(index);
    }

    public String getFormatConfigFileName() {
        return formatConfigFileName;
    }

    public void setFormatConfigFileName(String formatConfigFileName) {
        this.formatConfigFileName = formatConfigFileName;
    }

    public FormatterFactory getFormatFactory() {
        return formatFactory;
    }

    public void setFormatFactory(FormatterFactory formatFactory) {
        this.formatFactory = formatFactory;
    }

    public List<FormatterTest> getTests() {
        return tests;
    }

    public void setTests(List<FormatterTest> tests) {
        this.tests = tests;
    }

    @Override
    public String toString() {
        return "TestSuite{" + "tests=" + tests + ", formatConfigFileName=" + formatConfigFileName + ", formatFactory=" + formatFactory + '}';
    }
    
    
}
