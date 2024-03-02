/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.tester.report;

import com.novatronic.tester.engine.TestResult;
import com.novatronic.tester.report.exceptions.XMLReportException;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 *
 * @author ofernandez
 */
public class XMLReport implements Report {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
    private Calendar cal = Calendar.getInstance();
    private static final String ROOT = "report";
    private static final String REPORT_NAME = "Report_";
    private static final String FILE_TYPE = ".xml";
    private List<TestResult> testResults;

    public XMLReport(){
        testResults = new ArrayList<TestResult>();
    }

    @Override
    public void addTestResult(TestResult testResult){
        this.testResults.add(testResult);
    }

    @Override
    public void setTestResults(List<TestResult> testResults) {
        this.testResults = testResults;
    }

    /**
     * @throws XMLReportException
     */
    @Override
    public String generateReport() {
        String id = null;
        String name = null;
        String numTx = null;
        String nameReport = REPORT_NAME+sdf.format(cal.getTime()) + FILE_TYPE;

        try {
            Element root = new Element(ROOT);
            for (int i = 0; i < testResults.size(); i++) {
                TestResult testResult = testResults.get(i);
                id = testResult.getAcquirerConnection().getId().toString();
                name = testResult.getAcquirerConnection().getAcquirerName();
                numTx = testResult.getAcquirerConnection().getNumberTransactions().toString();
                AcquirerElement acquirerElement = new AcquirerElement(name, id, numTx);
                List<ReportItem> itemList = testResult.getReportItemList();
                for (int j = 0; j < itemList.size(); j++) {
                    ItemElement item = new ItemElement(itemList.get(j));
                    acquirerElement.addItemElement(item);
                }
                root.addContent(acquirerElement);
            }
            Document doc = new Document(root);

            /*Generando la salida*/
            XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
            File file = new File(nameReport);
            FileOutputStream fileOS = new FileOutputStream(file);
            out.output(doc, fileOS);
            fileOS.flush();
            fileOS.close();
            return file.getAbsolutePath();
        } catch (Exception ex) {
            throw new XMLReportException("No fue posible generar el report XML", ex);
        }
    }
}
