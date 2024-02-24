/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter;

import com.novatronic.formatter.field.Field;
import java.util.List;

/**
 * Interfaz creada para obtener informacion formateador.
 * @author rcastillejo
 */
public interface FormatterInfo {
    /**
     * Devuelve el listado de campos. Esto con el fin de manera informativa.
     * @return Listado de campos
     */
    public List getFields();
    /**
     * Realiza una busqueda del listado de campos por un identificado.
     * @param id Identificador del campo
     * @return Campo obtenido
     */
    public Field getFieldById(String id);
}
