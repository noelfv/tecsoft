/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.context;

import com.novatronic.formatter.filter.Filters;
import com.novatronic.formatter.macros.ParserMacro;

/**
 *
 * @author ofernandez
 */
public interface FieldConfigurationContext {
    public ParserMacro getParserMacro();
    public FmtContext getConfigContext();
    public Filters getFilters();
    /**
     * Devuelve el identificador del formateador padre de los campos asosicados.
     * @return El identificador del formateador
     */
    public String getFormatterId();
    
    /**
     * Devuelve el path en la cual se desenvuelve un campo. Esto es puede verse
     * como la ruta del objeto que maneja a los campos
     * @return La ruta del contexto
     */
    public String getContextPath();
    
    /**
     * Devuelve el path en la cual se desenvuelve un campo. Esto es puede verse
     * como la ruta del objeto que maneja a los campos
     * @return La ruta del contexto
     */
    public String getFilterPath();
}
