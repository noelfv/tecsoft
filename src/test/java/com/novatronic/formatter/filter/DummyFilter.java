/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.filter;

import java.util.Arrays;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 * @author ofernandez
 */
public class DummyFilter extends Filter {

    private static final Logger log = Logger.getLogger(DummyFilter.class);
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void readCustomConfiguration(Element elementConfig) {
        /*No presenta configuracion especial*/
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String applyFilter(String value) {
        return repeat(this.getSymbol(), value.length());
    }

    private String repeat(char symbol, int maskLength) {
        char[] mask;
        
        mask = new char[maskLength];
        Arrays.fill(mask, symbol);
        return new String(mask);
    }
}
