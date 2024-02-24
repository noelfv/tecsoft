/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.formatter.field;

import com.novatronic.formatter.context.FieldConfigurationContext;
import com.novatronic.formatter.exception.FieldConfigurationException;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 * @author ofernandez
 * @version 1.0
 * @since 1.0, 10/11/2010
 */
public class FieldFactory {
    private static final Logger log = Logger.getLogger(FieldFactory.class);
    private static final String PKG = "com.novatronic.formatter.field.";
    private static final String SUFIX = "Field";
    private static final String EMPTY_PARENT_PATH = "";
    private static final String FIELD_PATH_SEPARATOR = ".";
    
    interface Attr{
        public static final String TYPE = "type";
        public static final String ID = "id";
    }
    
    /**
     * Devuelve un campo configurado e instanciado segun el tipo requerido.
     * Además asiganara un referencia del parseador de macros pasado por parametro
     * de forma que este campo pueda ademas procesar macros.
     * @param fieldConfig Un elemento JDom que contiene los datos de creacion del campo.
     * Este dato aemas sera leido por el campo para que lea los datos propios de este.
     * @param ctxField Es un objeto que tiene datos del contexto que seran usados para
     * crear el campo. Estos pueden ser el mismo campo o en otros caso un objeto formatter
     * @return El campo instanciado y con el contexto creado
     * @throws FieldConfigurationException
     */
    public static Field getField(Element fieldConfig, FieldConfigurationContext ctxField){
        return getField(fieldConfig.getAttributeValue(Attr.TYPE), fieldConfig, ctxField);
    }
    
    /**
     * Devuelve un campo configurado e instanciado segun el tipo requerido.
     * Además asignara un referencia del parseador de macros pasado por parametro
     * de forma que este campo pueda ademas procesar macros.
     * @param fieldType El nombre de la clase la cual se asume debe estar ubicado en el 
     * paquete {@value #PKG }
     * @param fieldConfig Es el elemento jdom de donde se leeran el resto de parametros
     * de configuracion
     * @param ctxField Es un objeto de donde se obtendran referencias a los contextos del
     * campo. De este tipo se tienen los objetos Field, y Formatter.
     * @return Una instancia del campo ya configurado.
     */
    public static Field getField(String fieldType, Element fieldConfig, FieldConfigurationContext ctxField){
        Field field = getFieldInstance(fieldType);
        field.setId(fieldConfig.getAttributeValue(Attr.ID));
        field.setFormatterId(ctxField.getFormatterId());
        field.setParentPath(ctxField.getFilterPath());
        field.setPath(makePath(field.getId(), ctxField.getFilterPath()));
        field.setParserMacro(ctxField.getParserMacro());
        field.setFilters(ctxField.getFilters());
        field.setConfigContext(ctxField.getConfigContext());
        field.readConfiguration(fieldConfig);
        addFilterFromField(field, ctxField);
        return field;
    }
    
    private static void addFilterFromField(Field field, FieldConfigurationContext ctxField){
        log.trace("Agregando filtro desde campo[id=" + field.getId() + ", filter=" + field.getFilter() + "]...");
        if(field.isProtectable()){
            ctxField.getFilters().addFilter(field.getFormatterId(), 
                                            field.getPath(),
                                            field.getFilter());
        }
    }
    
    private static String makePath(String id, String parentPath){
        log.trace("Creando path [id=" + id + ", parentPath=" + parentPath + "] ...");
        return parentPath == null  || parentPath.equals(EMPTY_PARENT_PATH) 
                ? id 
                : parentPath + FIELD_PATH_SEPARATOR + id;
    }

    private static Field getFieldInstance(String type){
        String className = PKG + type + SUFIX;
        log.trace("Cargando Clase[" + className + "]");
        try {
            Class classField = Class.forName(className);
            log.debug("Clase[" + className + "] instanciada");
            return (Field)classField.newInstance();
        } catch (InstantiationException ex) {
            throw new FieldConfigurationException("No es accesible la clase tipo "
                    + type + " bajo el nombre " + className, ex);
        } catch (IllegalAccessException ex) {
            throw new FieldConfigurationException("No es posible instanciar la "
                    + "clase tipo "+ type + " bajo el nombre " + className, ex);
        } catch (ClassNotFoundException ex) {
            throw new FieldConfigurationException("No es posible ubicar la "
                    + "clase tipo "+ type + " bajo el nombre " + className, ex);
        } catch(Exception ex){
            throw new FieldConfigurationException("Error desconocido para la "
                    + "clase tipo "+ type + " bajo el nombre " + className, ex);
        }
    }
    
    
        /**
     * Devuelve un campo configurado e instanciado segun el tipo requerido. Este
     * campo via este metodo no tendra soporte para macros. Para esto es necesario
     * usar la sobrecarga de este metodo
     * @param fieldConfig
     * @param formatterAttributes
     * @return
     * @throws FieldConfigurationException
     * @deprecated En su lugar usar el metodo {@link #getField(org.jdom.Element) 
     * getField(Element)}
     */
    public static Field getField(Element fieldConfig, Element formatterAttributes){
        Field field = getFieldInstance(fieldConfig.getAttributeValue(Attr.TYPE));
        field.readConfiguration(fieldConfig);
        return field;
    }
}
