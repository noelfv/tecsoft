/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.filter;

import com.novatronic.formatter.exception.FilterConfigurationException;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 * @author rcastillejo
 */
public class FilterFactory {

    private static final Logger log = Logger.getLogger(FilterFactory.class);
    private static final String PKG = "com.novatronic.formatter.filter.";
    private static final String SUFIX = "Filter";

    private interface Attr {

        String TYPE = "type";
    }

    /**
     * Obtiene una instancia y configura el filtro segun la configuracion
     * asignada.
     *
     * @param filterConfig
     * @return
     * @throws FilterConfigurationException
     */
    public static Filter createFilter(Element filterConfig) {
        Filter filter = getFilterInstance(filterConfig.getAttributeValue(Attr.TYPE));
        filter.readConfiguration(filterConfig);
        log.debug("Filtro configurado=" + filter);
        return filter;
    }

    private static Filter getFilterInstance(String type) {
        if (type == null) {
            throw new FilterConfigurationException("El tipo de filtro no puede ser"
                    + " null: type=" + type);
        }
        String classId = PKG + type + SUFIX;
        log.debug("Cargando la clase:" + classId);
        try {
            Class classField = Class.forName(classId);
            log.debug("Clase[" + classId + "] instanciada");
            return (Filter) classField.newInstance();
        } catch (InstantiationException ex) {
            throw new FilterConfigurationException("No es accesible la clase tipo "
                    + type + "bajo el nombre " + classId, ex);
        } catch (IllegalAccessException ex) {
            throw new FilterConfigurationException("No es posible instanciar la "
                    + "clase tipo " + type + "bajo el nombre " + classId, ex);
        } catch (ClassNotFoundException ex) {
            throw new FilterConfigurationException("No es posible ubicar la "
                    + "clase tipo " + type + "bajo el nombre " + classId, ex);
        } catch (Exception ex) {
            throw new FilterConfigurationException("Error desconocido para la "
                    + "clase tipo " + type + "bajo el nombre " + classId, ex);
        }
    }

}
