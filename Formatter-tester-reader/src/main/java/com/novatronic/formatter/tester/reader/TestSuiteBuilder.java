/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.tester.reader;

import com.novatronic.formatter.Formatter;
import com.novatronic.formatter.FormatterFactory;
import com.novatronic.formatter.tester.beans.FormatterTest;
import com.novatronic.formatter.tester.beans.TestSuite;
import java.io.File;
import java.util.List;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author Omar
 */
public class TestSuiteBuilder {
    private static final Logger log = Logger.getLogger(TestSuiteBuilder.class);
    public static final String FORMAT_CONFIG = "formatconfig";
    /**
     * 
     * @param fileTestPath
     * @param formatsDir
     * @return 
     * @throws ReaderException
     */
    public static TestSuite newTestSuite(String fileTestPath, String formatsDir){
        TestSuite suite;
        Element root;
        String formatFileConfig;
        FormatterFactory factory;
        
        Document doc;
        try {
            doc = (new SAXBuilder()).build(fileTestPath);
            log.debug("Documento leido, obteniendo elemento raiz");
            root = doc.getRootElement();
            formatFileConfig = root.getAttributeValue(FORMAT_CONFIG);
            factory = new FormatterFactory(formatsDir + File.separator + formatFileConfig);
            suite = new TestSuite(formatFileConfig, factory);
            suite.setTests(Reader.readTests(root));
            loadFormatForEachTest(suite);
            return suite;
        } catch (Exception ex) {
            log.error("No se pudo leer el archivo[" + fileTestPath + "]", ex);
            throw new ReaderException("No se pudo leer el archivo[" + fileTestPath + "], causa:" + ex.getMessage() ,ex);
        }
    }
    
    private static void loadFormatForEachTest(TestSuite suite){
        List<FormatterTest> tests = suite.getTests();
        Formatter formatter;
        String formatId;
        for (FormatterTest formatterTest : tests) {
            formatId = formatterTest.getFormatId();
            formatter = suite.getFormatFactory().getFormatter(formatId);
            if(formatter == null){
                throw new ReaderException("El formato=" + formatId + ",no pudo ser encontrado");
            }
            formatterTest.setFormat(formatter);
            log.debug("Agregado formato=" + formatId + ", a test=" + formatterTest.getId());
        }
    }
}
