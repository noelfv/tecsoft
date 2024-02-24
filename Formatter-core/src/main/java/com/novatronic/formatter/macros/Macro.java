/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.formatter.macros;

import com.novatronic.formatter.exception.MacroConfigurationException;
import com.novatronic.formatter.exception.ParseException;
import org.jdom.Element;

/**
 *
 * @author ofernandez
 * @version 1.0
 * @since 1.0, 10/12/2010
 */
public abstract class Macro {
    private String parseName;
    
    public interface Attr{
        public static final String PARSE_NAME = "parsename";
    }
    
    /**
     * Devuelve el nombre unico con el cual esta macro sera parseado.
     * @return el nombre utilizado para parsear.
     */
    public String getParseName() {
        return parseName;
    }

    /**
     *
     * @param elementConfig
     * @throws MacroConfigurationException
     */
    public void readConfiguration(Element elementConfig){
        parseName = elementConfig.getAttributeValue(Attr.PARSE_NAME);
        if((parseName == null) || parseName.equals("")){
            throw new MacroConfigurationException("El parametro "
                    + Attr.PARSE_NAME + " es obligatorio");
        }
        
        this.readCustomConfiguration(elementConfig);
    }

    /**
     * 
     * @param elementConfig
     * @throws MacroConfigurationException
     */
    protected abstract void readCustomConfiguration(Element elementConfig);

    /**
     *
     * @param lineToParse
     * @return
     * @throws ParseException 
     */
    public abstract String parse(String... args);

    @Override
    public String toString() {
        return "Macro{" + "parseName=" + parseName + '}';
    }
}
