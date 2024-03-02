/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.tester.validate;

import com.novatronic.tester.domain.Transaction;
import com.novatronic.tester.report.ReportItem;
import com.novatronic.tester.engine.TestResult;
import com.novatronic.tester.exception.TesterValidationException;
import org.apache.log4j.Logger;

/**
 *
 * @author ofernandez
 */
public class EqualsValidator implements Validator {

    private static final Logger log = Logger.getLogger(EqualsValidator.class);
    private String result;
    private TestResult testResult;

    /**
     *
     * {@inheritDoc  }
     * @throws TesterValidationException Si el objeto TestResult donde se aagregaran
     * los resultados de la prueba no ha sido asignado.
     */
    @Override
    public boolean validate(Transaction transaction, String tramaOut) {
        String tramaEsperada;
        log.debug("Realizando validacion de tramas:Transaction[" + transaction
                + "],tramaOut[" + tramaOut + "]");
        if (!validateParameters(transaction, tramaOut)) {
            return false;
        }
        log.debug("Parametros correctos, obteniendo tramas a comparar");

        tramaEsperada = transaction.getAcquirerOut();
        int tam = (tramaEsperada.length() > tramaOut.length())
                ? tramaOut.length()
                : tramaEsperada.length();
        return makeComparison(tramaEsperada, tramaOut, tam, transaction);
    }

    private boolean makeComparison(String tramaEsperada, String tramaOut, int tam,
            Transaction transaction) {
        log.debug("Realizando comparacion de tramaEsperada[" + tramaEsperada
                + "], tramaOut[" + tramaOut);
        StringBuilder sb = new StringBuilder("");
        boolean valid = true;
        for (int i = 0; i < tam; i++) {
            if (tramaEsperada.charAt(i) == tramaOut.charAt(i)) {
                sb.append(tramaOut.charAt(i));
            } else {
                sb.append('[');
                sb.append(tramaOut.substring(i));
                sb.append(']');
                valid = false;
                this.result = sb.toString();
                this.addToReport(transaction, tramaOut);
                log.debug("Resultado de la prueba:" + this.result + ", posicion=" + i);
                break;
            }
        }
        return valid;
    }

    private boolean validateParameters(Transaction transaction, String tramaReceived) {
        /*Revisamos si tenemos el TestResult*/
        if (this.testResult == null) {
            log.debug("ValidateParameters, testResult=null");
            throw new TesterValidationException("El objeto TestResult no ha sido"
                    + "asignado");
        }
        log.debug("this.testResult <> null");

        if (transaction == null) {
            log.debug("ValidateParameters, transaction=null");
            throw new TesterValidationException("La transaccion no puede ser nula");
        }
        log.debug("transaction <> null");

        /*validamos la trama y seteamos el resultado con pretty printer.*/
        if (transaction.getAcquirerOut() == null) {
            log.debug("ValidateParameters, transaction.getAcquirerOut=null");
            throw new TesterValidationException("La trama a comparar no puede ser"
                    + "nula: AcquirerOut=" + transaction.getAcquirerOut());
        }
        log.debug("transaction.getAcquirerOut() <> null");

        if (tramaReceived == null) {
            log.debug("ValidateParameters, tramaReceived=null");
            this.result = "recibido=null";
            this.addToReport(transaction, tramaReceived);
            log.debug("Resultado de la prueba:" + this.result);
            return false;
        }
        log.debug("tramaReceived <> null");

        if (tramaReceived.length() == 0) {
            log.debug("ValidateParameters, tramaReceived.length() == 0");
            this.result = "[]";
            this.addToReport(transaction, tramaReceived);
            log.debug("Resultado de la prueba:" + this.result);
            return false;
        }
        log.debug("tramaReceived.length() <> 0");

        return true;
    }

    private void addToReport(Transaction transaction, String tramaOut) {
        log.debug("Agregando al reporte: transaction[" + transaction
                + "],tramaOut[" + tramaOut + "], result[" + this.result + "]");
        ReportItem repItem = new ReportItem();
        repItem.setId(transaction.getKeyTxn());
        repItem.setAcquireIn(transaction.getAcquirerIn());
        repItem.setAcquireOut(transaction.getAcquirerOut());
        repItem.setResult(this.result);
        repItem.setSixRecieved(tramaOut);
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
