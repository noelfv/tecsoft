package com.novatronic.formatter.field;

import com.novatronic.formatter.exception.FieldConfigurationException;
import com.novatronic.formatter.exception.FieldException;
import com.novatronic.formatter.exception.FieldValueException;
import com.novatronic.formatter.internal.InternalFormat;
import com.novatronic.formatter.util.VariableByteBuffer;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 * Esta campo leera una trama como una tira de bytes hasta el final de la misma. La
 * utilidad de este campo estriba en que para ciertos campos de campos que requieren
 * delimitadores como tokens, el ultimo campo no contiene tokens y por tanto no existe un
 * fin predecible, en consecuencia, se deberá leer hasta el final. <br><br> Otro caso de
 * uso es para cuando ciertos campos pueden presentarse o no, en cuyo caso se debera leer
 * una cierta cantidad maxima lo cual a su vez permita la encadenacion con otros campos.
 *
 * @author Omar Fernandez
 * @version 2.0.1
 * @since 2.0
 * @date 11 Dic. 2012
 */
public class ToEndField extends Field {

    private static final Logger log = Logger.getLogger(ToEndField.class);
    private boolean hasMax;
    private int max;

    interface Attr {
        static final String MAX = "max";
    }

    /**
     * {@inheritDoc }<br><br> Se lee la configuracion del caracter terminador
     *
     * @throws FieldConfigurationException En caso no se haya configurado este caracter.
     */
    @Override
    protected void readCustomConfiguration(Element element) {
        String maxValue;

        maxValue = element.getAttributeValue(Attr.MAX);
        hasMax = maxValue == null ? false : true;
        if (hasMax) {
            max = Integer.parseInt(maxValue);
        }

        log.debug(logId + toString());
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public int putBytes(InternalFormat internalFormat, VariableByteBuffer frame) {
        String value;
        int added;

        try {
            value = internalFormat.getValue(getId());
            validateValue(value);
            added = frame.add(value);

            LogMF.debug(log, "{0}Se agrega:{1}", logId, added);

            return added;
        } catch (Exception ex) {
            throw new FieldValueException(logId + "Error al generar la trama", ex);
        }
    }

    private void validateValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException(logId + "No se encuentra el valor");
        }
        
        if (hasMax && (value.getBytes().length > max)) {
            throw new IllegalArgumentException(logId + "Longitud invalida para el valor:"
                        + applyFilter(value) + ". Max=" + max);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public int readBytes(InternalFormat internalFormat, VariableByteBuffer frame, int position) {
        byte bytes[];
        int bytesToRead;

        try {
            if(hasMax){
                bytesToRead = Math.min(max, frame.getLength() - position);
            }else{
                bytesToRead = frame.getLength() - position;
            }
            LogMF.debug(log, "{0}max={1}, Bytes por leer={2}", logId, max, bytesToRead);
            bytes = frame.getBytes(position, bytesToRead);
            internalFormat.add(getId(), new String(bytes));

            return position + bytesToRead;
        } catch (Exception ex) {
            throw new FieldException(logId + "Error al leer la trama", ex);
        }
    }

    @Override
    public String toString() {
        return "ToEndField{" + "hasMax=" + hasMax + ", max=" + max + '}';
    }
}
