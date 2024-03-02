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
public class AcquirerElement extends Element{
    private static final String ACQUIRER = "acquirer";
    private static final String ATT_ID = "id";
    private static final String ATT_NAME = "name";
    private static final String ATT_TOTAL_TEST = "totaltest";

    public AcquirerElement(String name, String id, String numTest){
        super(ACQUIRER);
        this.setAttribute(ATT_ID,id);
        this.setAttribute(ATT_NAME,name);
        this.setAttribute(ATT_TOTAL_TEST,numTest);
    }

    public void addItemElement(ItemElement itemElement){
        this.addContent(itemElement);
    }
}
