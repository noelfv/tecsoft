/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.report.exceptions;

import com.novatronic.tester.exception.TesterReportException;

/**
 *
 * @author ofernandez
 */
public class XMLReportException extends TesterReportException{

    public XMLReportException() {
    }

    public XMLReportException(String message) {
        super(message);
    }

    public XMLReportException(String message, Throwable cause) {
        super(message, cause);
    }

    public XMLReportException(Throwable cause) {
        super(cause);
    }

}
