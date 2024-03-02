/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.exception;

/**
 *
 * @author ofernandez
 */
public class TesterReportException extends TesterException{

    public TesterReportException() {

    }

    public TesterReportException(String message) {
        super(message);
    }

    public TesterReportException(String message, Throwable cause) {
        super(message, cause);
    }

    public TesterReportException(Throwable cause) {
        super(cause);
    }

}
