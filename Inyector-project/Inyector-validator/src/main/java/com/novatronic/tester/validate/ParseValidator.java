/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.tester.validate;

import com.novatronic.formatter.FormatterFactory;
import com.novatronic.tester.domain.Transaction;
import com.novatronic.tester.engine.TestResult;
import com.novatronic.tester.exception.TesterValidationException;
import com.novatronic.tester.report.ReportItem;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author ofernandez
 * @version 1.0
 * @since 1.0, 29/12/2010
 */
public class ParseValidator implements Validator {

    private static final Logger log = Logger.getLogger(ParseValidator.class);
    private String result;
    private TestResult testResult;
    private static final String CONFIGURATION_PATH = "validator.xml";
    private static final String DEFAULT_FORMATTER_CONFIG = "format.xml";
    private static String formatterConfig;
    private static FormatterFactory formatterFactory;
    private static Map<String, RuleValidator> rules;

    public interface Tag {

        public static final String RULE = "rules";
        public static final String FORMATTER = "formatter";
    }

    public interface Attr {

        public static final String CONFIGURATION = "config";
    }

    static {
        try {
            log.debug("Leyendo configuracion del ParseValidator ["
                    + CONFIGURATION_PATH + "]");
            Document doc = (new SAXBuilder()).build(CONFIGURATION_PATH);
            readDoc(doc);
            log.debug("configuracion leida");
        } catch (JDOMException ex) {
            throw new TesterValidationException(ex);
        } catch (IOException ex) {
            throw new TesterValidationException(ex);
        }
    }

    private static void readDoc(Document doc) {
        try {
            log.debug("Documento leido, obteniendo elemento raiz");
            Element root = doc.getRootElement();
            log.debug("Raiz obtenida. Configurando reglas");
            configureFormatter(root.getChild(Tag.FORMATTER));
            configureRules(root.getChild(Tag.RULE).getChildren());
            log.debug("Reglas configuradas, total reglas=" + rules.size());
        } catch (IllegalStateException ex) {
            throw new TesterValidationException(ex);
        } catch (Exception ex) {
            throw new TesterValidationException(ex);
        }
    }

    private static void configureFormatter(Element element) {
        if (element == null) {
            formatterConfig = DEFAULT_FORMATTER_CONFIG;
        } else{
            formatterConfig = element.getAttributeValue(Attr.CONFIGURATION);
        }
        log.debug("Leyendo configuracion del formateador [" + formatterConfig + "]");
        formatterFactory = new FormatterFactory(formatterConfig);
        log.debug("Formateador encontrado y configurado");
    }

    private static void configureRules(List<Element> list) {
        rules = new HashMap<String, RuleValidator>();
        log.debug("Tenemos elementos de reglas:" + list.size());
        for (Element element : list) {
            RuleValidator rule = new RuleValidator(element, formatterFactory);
            log.debug("Agregando regla:" + rule);
            rules.put(rule.getId(), rule);
        }
    }

    @Override
    public boolean validate(Transaction transaction, String tramaReceived) {
        log.debug("Realizando validacion de tramas");
        if (!validateParameters(transaction, tramaReceived)) {
            log.debug("Parametros recibidos incorrectos, nos se puede validar");
            return false;
        }
        return parseFrames(transaction, tramaReceived);

    }

    private boolean parseFrames(Transaction transaction, String tramaReceived) {
        String id = transaction.getId();
        this.result = "";
        boolean isValid ;
        
        RuleValidator rule = rules.get(id);
        log.debug("Regla[" + id + "] obtenida:" + rule);
        isValid = rule.applyRule(transaction.getAcquirerOut(),tramaReceived);
        this.result = rule.getResult();
        log.debug("Regla aplicada, resultado:" + isValid + ",result="
                + this.result);
        if(!isValid){
            addToReport(transaction, tramaReceived);
        }
        return isValid;
    }

    private boolean validateParameters(Transaction transaction, String tramaReceived) {
        /*Revisamos si tenemos el TestResult*/
        if (this.testResult == null) {
            throw new IllegalStateException("El objeto TestResult no ha sido"
                    + "asignado");
        }
        if (transaction == null) {
            throw new IllegalArgumentException("La transaccion no puede ser nula");
        }
        /*validamos la trama y seteamos el resultado con pretty printer.*/
        if ((transaction.getAcquirerOut() == null)) {
            throw new IllegalArgumentException("La trama a comparar no puede ser"
                    + "nula: AcquirerOut=" + transaction.getAcquirerOut());
        }
        if (tramaReceived == null) {
            this.result = "recibido=null";
            this.addToReport(transaction, tramaReceived);
            log.debug("Resultado de la prueba:" + this.result);
            return false;
        }
        if (tramaReceived.length() == 0) {
            this.result = "[]";
            this.addToReport(transaction, tramaReceived);
            log.debug("Resultado de la prueba:" + this.result);
            return false;
        }
        return true;
    }

    private void addToReport(Transaction transaction, String tramaReceived) {
        log.debug("Agregando al reporte: transaction[" + transaction
                + "],tramaOut[" + tramaReceived + "], result[" + this.result + "]");
        ReportItem repItem = new ReportItem();
        repItem.setId(transaction.getKeyTxn());
        repItem.setAcquireIn(transaction.getAcquirerIn());
        repItem.setAcquireOut(transaction.getAcquirerOut());
        repItem.setResult(this.result);
        repItem.setSixRecieved(tramaReceived);
        testResult.addReportItem(repItem);
        log.debug("Resultado agregado:" + repItem);
    }

    @Override
    public String getResult() {
        return this.result;
    }

    @Override
    public void setTestResult(TestResult testResult) {
        this.testResult = testResult;
    }
}
