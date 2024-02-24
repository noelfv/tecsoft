/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.formatter.macros;

import org.jdom.Element;

/**
 *
 * @author ofernandez
 * @version 1.0
 * @since 1.0, 10/12/2010
 */
public interface ParserMacro {

    /**
     *
     * @param elementConfig
     * @throws MacroConfigurationException
     */
    public void readConfiguration(Element elementConfig);

    /**
     * @param lineToParse
     * @return
     * @throws ParseException
     */
    public String parse(String lineToParse);
}
