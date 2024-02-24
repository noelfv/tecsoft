package com.novatronic.formatter.field;

import com.novatronic.formatter.exception.DecoratorException;
import com.novatronic.formatter.internal.InternalField;
import com.novatronic.formatter.internal.InternalFormat;
import com.novatronic.formatter.util.VariableByteBuffer;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 * Hello world!
 *
 */
public abstract class FieldDecorator extends Field {

    private static final Logger log = Logger.getLogger(FieldDecorator.class);
    private Field subField;
    private String subFieldType;
    public static interface Attr{
        public static final String SUBTYPE = "subtype";
    }
    
    @Override
    public InternalField getValueAsInternalField() {
        return subField.getValueAsInternalField();
    }

    @Override
    protected final void readCustomConfiguration(Element element) {
        log.debug(element.getAttributes());
        subFieldType = element.getAttributeValue(Attr.SUBTYPE);
        subField = FieldFactory.getField(subFieldType,element, this);
        readDecoratorConfig(element);
    }
    
    /**
     * En este metodo se lee la configuracion propia de la implementacion
     * @param element Es el elemento de jdom el cual apunta a la configuracion
     * de este Decorador en el XML.
     */
    protected abstract void readDecoratorConfig(Element element);

    @Override
    public int putBytes(InternalFormat internalFormat, VariableByteBuffer frame) {
        int bytesAdded;
        try{
            toField(internalFormat);
            bytesAdded = subField.putBytes(internalFormat, frame);
            return bytesAdded;
        }catch(Exception ex){
            throw new DecoratorException(ex);
        }
    }
    
    /**
     * Este metodo procesa los datos recibidos en el FI antes de que estos sean
     * recibidos por el campo que finalmente lo colocara en la trama.
     * @param internalFormat Es el FI con los datos a usar para generar la trama
     */
    protected abstract void toField(InternalFormat internalFormat);

    @Override
    public int readBytes(InternalFormat internalFormat, VariableByteBuffer frame, int posicion) {
        int newReadingPosition;
        try{
            newReadingPosition = subField.readBytes(internalFormat, frame, posicion);
            fromField(internalFormat);
            return newReadingPosition;
        }catch(Exception ex){
            throw new DecoratorException(ex);
        }
    }
    
    /**
     * Procesa los datos recibidos en el FI una vez que estos han sido
     * interpretados por el campo a partir de una trama.
     * @param internalFormat Es el FI recibido donde el campo ha colocado los
     * datos interpretados desde la trama
     */
    protected abstract void fromField(InternalFormat internalFormat);

    protected Field getSubField() {
        return subField;
    }
}
