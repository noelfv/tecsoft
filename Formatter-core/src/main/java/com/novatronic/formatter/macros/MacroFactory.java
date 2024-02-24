/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.formatter.macros;

import com.novatronic.formatter.exception.MacroConfigurationException;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 * @author ofernandez
 * @version 1.0
 * @since 1.0, 10/12/2010
 */
public class MacroFactory {
    private static final Logger log = Logger.getLogger(MacroFactory.class);
    private static final String PKG = "com.novatronic.formatter.macros.";
    private static final String SUFIX = "Macro";

    /**
     * Obtiene una instancia de la macro segun la configuracion asignada
     * @param macroConfig
     * @return
     * @throws MacroConfigurationException
     */
    public static Macro getMacro(Element macroConfig){
        Macro macro = getMacroInstance(macroConfig.getAttributeValue(Attr.TYPE));
        macro.readConfiguration(macroConfig);
        log.debug("Macro configurada=" + macro);
        return macro;
    }

    private static Macro getMacroInstance(String type){
        if(type == null){
            throw new MacroConfigurationException("El tipo de macro no puede ser"
                    + " null: type=" + type);
        }
        String className = PKG + type + SUFIX;
        log.debug("Cargando la clase:" + className);
        try {
            Class classField = Class.forName(className);
            log.debug("Clase[" + className + "] instanciada");
            return (Macro)classField.newInstance();
        } catch (InstantiationException ex) {
            throw new MacroConfigurationException("No es accesible la clase tipo "
                    + type + "bajo el nombre " + className, ex);
        } catch (IllegalAccessException ex) {
            throw new MacroConfigurationException("No es posible instanciar la "
                    + "clase tipo "+ type + "bajo el nombre " + className, ex);
        } catch (ClassNotFoundException ex) {
            throw new MacroConfigurationException("No es posible ubicar la "
                    + "clase tipo "+ type + "bajo el nombre " + className, ex);
        } catch(Exception ex){
            throw new MacroConfigurationException("Error desconocido para la "
                    + "clase tipo "+ type + "bajo el nombre " + className, ex);
        }
    }

    interface Attr{
        public static final String TYPE = "type";
    }
}
