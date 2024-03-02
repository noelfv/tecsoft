/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.generator;

import com.novatronic.formatter.internal.InternalFormat;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 * @author ofernandez
 * @version 1.0
 * @since 1.0, 12/11/2010
 */
public class GeneratorField {

    private final static Logger log = Logger.getLogger(GeneratorField.class);
    private String id;
    private List<GeneratorField> fields;
    private String text;
    private String value;

    public void readConfiguration(Element configuration) {
        log.debug("Leyendo configuracion de Campo:" + configuration);
        id = configuration.getAttributeValue(Attr.ID);
        text = configuration.getText();
        formatValue();
        List<Element> childrenFields = configuration.getChildren(Tag.FIELD);
        if (childrenFields.size() > 0) {
            log.debug("[" + id +"]Se encontro campos hijos:" + childrenFields);
            fields = new ArrayList<GeneratorField>();
            for (int i = 0; i < childrenFields.size(); i++) {
                GeneratorField field = new GeneratorField();
                field.readConfiguration(childrenFields.get(i));
                fields.add(field);
                log.debug("[" + id +"]Se agrega el campo hijo:"+field);
            }
            log.debug("[" + id +"]Campos hijos agregados:" + fields);
        } else {
            log.debug("[" + id +"]No Se encontro campos hijos, se obtiene texto");
            text = configuration.getText();
        }
    }

    /**
     * 
     * @param internalFormat
     */
    public void registerField(InternalFormat internalFormat){
        log.debug("Registrando nuevo valor en internalFormat...");
        if((fields == null) || fields.isEmpty()){
            log.debug("No se encontraron sub-campos, se agrega field:" + this);
            internalFormat.add(id, value);
        } else{
            log.debug("Se encontraron sub-campos, se procesan...");
            InternalFormat InnerIF = new InternalFormat();
            for(GeneratorField field : fields){
                field.registerField(InnerIF);
            }
            internalFormat.add(InnerIF.getId(), InnerIF);
        }
        log.debug("Registro terminado");
    }

    /**
     * 
     * @return
     */
    private void formatValue(){
        //FIXME aca se debe invocar al parser para modificar el texto
        value = getText();
    }

    public String getText(){
        return text;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "GeneratorField{" + "id=" + id + "fields=" + fields + "text="
                + text + "value=" + value + '}';
    }

    public interface Attr {
        public static final String ID = "id";
    }

    public interface Tag{
        public static final String FIELD = "field";
    }
}
