/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.report;

import org.jdom.Element;

/**
 *
 * @author ofernandez
 */
public class ItemElement extends Element{
    private static final String ITEM = "item";
    private static final String ACQUIRER_IN = "acquirerin";
    private static final String ACQUIRER_OUT = "acquirerout";
    private static final String RECEIVED = "received";
    private static final String RESULT = "result";
    private static final String ATT_ID = "id";

    public ItemElement(ReportItem reportItem){
        super(ITEM);
        this.setAttribute(ATT_ID, reportItem.getId());
        this.addContent(new Element(ACQUIRER_IN).setText(
                XMLStringFilter.filter(reportItem.getAcquireIn())));
        this.addContent(new Element(ACQUIRER_OUT).setText(
                XMLStringFilter.filter(reportItem.getAcquireOut())));
        this.addContent(new Element(RECEIVED).setText(
                XMLStringFilter.filter(reportItem.getSixReceived())));
        this.addContent(new Element(RESULT).setText(
                XMLStringFilter.filter(reportItem.getResult())));
    }
}
