/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.macros;

import org.jdom.Element;

/**
 *
 * @author Omar
 */
public class MacroMock extends Macro{

    @Override
    protected void readCustomConfiguration(Element elementConfig) {
        /* Sin configuracion*/
    }

    @Override
    public String parse(String... args) {
        String result = "";
        
        for (int i = 0; i < args.length; i++) {
            result += (i+1) + "=[" + args[i] + "]";
            result = (i == args.length -1) ? result : result + ",";
        }
        
        return result;
    }
    
}
