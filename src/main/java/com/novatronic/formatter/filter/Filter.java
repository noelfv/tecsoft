/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.filter;

import com.novatronic.formatter.exception.FilterConfigurationException;
import com.novatronic.formatter.exception.MacroConfigurationException;
import org.jdom.Element;

/**
 *
 * @author rcastillejo
 * @version 1.0
 * @since 1.0, 29/11/2013
 */
public abstract class Filter {

    private String id;
    private char symbol;

    private interface Attr {
        String ID = "id";
        String SYMBOL = "symbol";
    }
    

    private interface Const {
        char DEAULT_SYMBOL = '*';
    }
    
    /*
     * Devuelve el nombre unico con el cual esta macro sera parseado.
     * @return el nombre utilizado para parsear.
     */
    public String getId() {
        return id;
    }
    
    /*
     * Devuelve el nombre unico con el cual esta macro sera parseado.
     * @return el nombre utilizado para parsear.
     */
    public char getSymbol() {
        return symbol;
    }

    /**
     *
     * @param elementConfig
     * @throws MacroConfigurationException
     */
    public void readConfiguration(Element elementConfig){
        String strSymbol;
        id = elementConfig.getAttributeValue(Attr.ID);
        strSymbol = elementConfig.getAttributeValue(Attr.SYMBOL);
        if((id == null) || id.equals("")){
            throw new FilterConfigurationException("El parametro "
                    + Attr.ID + " es obligatorio");
        }
        if((strSymbol == null) || strSymbol.equals("")){
            symbol = Const.DEAULT_SYMBOL; 
        }else{
            symbol = strSymbol.charAt(0);       
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
     * @param value
     * @return Valor filtrado
     */
    public String filter(String value){
        if(value == null || value.equals("")){
            return value;
        }
        return this.applyFilter(value);
    }
    
    /**
     *
     * @param value
     * @return Valor filtrado
     */
    protected abstract String applyFilter(String value);

    @Override
    public String toString() {
        return "Filter{" + "id=" + id + ", symbol=" + symbol + '}';
    }
    
}
