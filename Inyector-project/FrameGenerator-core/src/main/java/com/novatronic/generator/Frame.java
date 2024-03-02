/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.generator;

import com.novatronic.formatter.internal.InternalFormat;
import com.novatronic.formatter.util.VariableByteBuffer;
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
public class Frame {
    private static final Logger log = Logger.getLogger(Frame.class);

    private List<GeneratorField> fields;
    private String formatterId;
    private boolean binary;

    public Frame(Element configuration){
        log.debug("Creando objeto Frame...");
        formatterId = configuration.getAttributeValue(Attr.FORMATTER);
        binary = Boolean.parseBoolean(configuration.getAttributeValue(Attr.BINARY));
        fields = new ArrayList<GeneratorField>();
        createGeneratorFields(configuration.getChildren(Tag.FIELD));

    }

    private void createGeneratorFields(List<Element> fieldsAsList){
        log.debug("Agregando campos del Frame:");
        for(int i = 0;i < fieldsAsList.size();i++){
            GeneratorField field = new GeneratorField();
            field.readConfiguration(fieldsAsList.get(i));
            fields.add(field);
            log.debug("Campo agregado al Frame:" + field);
        }
    }

    public String generateFrame(){
        log.debug("Generando trama con formatter ID:" + formatterId);
        /*1. Crear internal frame si tenemos field*/
        if (!fields.isEmpty()){
            log.debug("Se encontraron campos, se crea internalFormat");
            InternalFormat internalFormat = new InternalFormat();
            for(GeneratorField field : fields){
                log.debug("Registrando campo[" + field + "] al internalFormat creado");
                field.registerField(internalFormat);
                log.debug("Se registro el campo:"+ field);
            }
            VariableByteBuffer frame = FrameGenerator.getFormatter(formatterId)
                    .getFrames(internalFormat);
            log.debug("Trama generada=[" + frame + "]");
            return new String(frame.getByteArray());       //TODO puede invocarseEncodeBase64
        } else{
            VariableByteBuffer frame = FrameGenerator.getFormatter(formatterId).getFrames();
            log.debug("Trama generada=[" + frame + "]");
            return new String(frame.getByteArray());       //TODO puede invocarseEncodeBase64
        }
    }

    public boolean isBinary() {
        return binary;
    }

    @Override
    public String toString() {
        return "Frame{" + "fields=" + fields + '}';
    }

    public interface Tag{
        public static final String FIELD = "field";
    }

    public interface Attr{
        public static final String FORMATTER = "formatter";
        public static final String BINARY = "binary";
    }

}
