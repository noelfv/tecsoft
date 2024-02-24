/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter;

import com.novatronic.formatter.exception.CreateFrameException;
import com.novatronic.formatter.exception.FormatterException;
import com.novatronic.formatter.exception.ReadFrameException;
import com.novatronic.formatter.field.Field;
import com.novatronic.formatter.internal.InternalField;
import com.novatronic.formatter.internal.InternalFormat;
import com.novatronic.formatter.util.VariableByteBuffer;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 * @author ofernandez
 * @version 1.0
 * @since 1.0, 08/11/2010
 */
public class SimpleFormatter extends Formatter implements FormatterInfo{

    private static final Logger log = Logger.getLogger(SimpleFormatter.class);

    public SimpleFormatter() {
    }

    /**
     * {@inheritDoc }
     */
    @Override
    protected void readCustomConfiguration(Element element) {
        /*Esta clase no contiene configuracion adicional a la base*/
    }

    /**
     * {@inheritDoc }
     * @return 
     */
    @Override
    public InternalFormat getInternalFormatFromConfig() {
        return getInternalFormatFromConfig(new Properties());
    }

    /**
     * {@inheritDoc }
     * @return 
     */
    @Override
    public InternalFormat getInternalFormatFromConfig(Properties params) {
        log.debug("Creando internalFormat a partir de la configuracion...");
        List<Field> fields;
        InternalFormat intFormat;
        InternalField intField;
        Field field;
        
        fields = this.getFieldsAsList();
        intFormat = new InternalFormat();
        try {
            for (int i = 0; i < fields.size(); i++) {
                field = fields.get(i);
                log.trace("Procesando field:ID=[" + field.getId() + "],type=[" + field.getType() + "]");
                if (field.isNullable()) {
                    log.trace("Se salta campo id=" + field.getId() + ", null=" + field.isNullable());
                } else {
                    intField = field.getValueAsInternalField(params, intFormat);
                    intFormat.addInternalField(intField);
                    log.trace("Campo agregado: " + intField.getId());
                }
            }
            log.trace("Internal format creado:" + applyFilter(intFormat));
            return intFormat;
        } catch (Exception ex) {
            throw new FormatterException(ex, intFormat);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public VariableByteBuffer getFrames(InternalFormat updateInternalFormat) {
        log.trace("Creando VariableByteBuffer a partir de campos actualizados: "
                + applyFilter(updateInternalFormat));
        InternalFormat baseIntFormat = this.getInternalFormatFromConfig();
        updateInternalFormat(baseIntFormat, updateInternalFormat);
        return getFrameFromInternalFormat(baseIntFormat);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public VariableByteBuffer getFrames() {
        log.trace("Creando VariableByteBuffer a partir de los campos");
        InternalFormat intFormat = this.getInternalFormatFromConfig();
        return getFrameFromInternalFormat(intFormat);
    }

    /**
     * Este metodo los campos recibidos en updateIntFormat hacia el oldIntFormat de forma
     * que los nuevos valores tomen el lugar de los anteriores. En caso de que algunos de
     * los campos sea otro InternalFormat, se llamara esta misma metodo recursivamente a
     * fin de actualizar campo por campo dichos valores, y asi sucesivamente.
     *
     * @param oldIntFormat El internalFormat antiguo
     * @param updateIntFormat El internalFormat de donde se sacaran los nuevos valores.
     */
    private void updateInternalFormat(InternalFormat oldIntFormat,
            InternalFormat updateIntFormat) {
        log.trace("Actualizando internalFormat, por actualizar["
                + applyFilter(oldIntFormat) + "], agrega[" + applyFilter(updateIntFormat) + "]");
        List<String> ids = updateIntFormat.getIdsAsList();
        for (String id : ids) {
            InternalField updateField = updateIntFormat.getInternalField(id);
            InternalField oldField = oldIntFormat.getInternalField(updateField.getId());

            if ((updateField instanceof InternalFormat) && (oldField instanceof InternalFormat)) {
                updateInternalFormat((InternalFormat) oldField, (InternalFormat) updateField);
            } else {
                oldIntFormat.addInternalField(updateField);
            }
        }
        log.trace("Internal format actualizado:" + applyFilter(oldIntFormat));
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public VariableByteBuffer getFrameFromInternalFormat(InternalFormat intFormat) {
        log.trace("Creando VariableByteBuffer a partir del internalFormat:" + applyFilter(intFormat));
        VariableByteBuffer bytes = new VariableByteBuffer();
        List<Field> fields = this.getFieldsAsList();
        try {
            for (int i = 0; i < fields.size(); i++) {
                Field field = fields.get(i);
                log.trace("Procesando [id=" + field.getId() + "]");
                field.putBytes(intFormat, bytes);
            }
            return bytes;
        } catch (Exception ex) {
            log.error("Error en trama:" + bytes);
            throw new CreateFrameException("", ex, bytes, intFormat);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InternalFormat createInternalFormatFromFrame(VariableByteBuffer frame) {
        List<Field> fields = this.getFieldsAsList();
        InternalFormat intFormat = new InternalFormat();
        int posicion = 0;

        log.debug("Frame length=[" + frame.getLength()+ "]");
        try {
            for (int i = 0; i < fields.size(); i++) {
                Field field = fields.get(i);
                log.trace("Campo ID=" + field.getId());
                posicion = field.readBytes(intFormat, frame, posicion);
            }
            log.debug("IntFormat generado:" + applyFilter(intFormat));
            return intFormat;
        } catch (Exception ex) {
            throw new ReadFrameException("No pudo crearse el formato interno", ex, frame, intFormat);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InternalFormat createInternalFormatFromFrame(
            VariableByteBuffer frame, InternalFormat intFormat) {

        List<Field> fields = getFieldsAsList();
        int posicion = 0;

        log.debug("Frame=[" + frame.toString() + "]");
        try {
            for (int i = 0; i < fields.size(); i++) {
                Field field = fields.get(i);
                log.debug("Campo ID=" + field.getId());
                posicion = field.readBytes(intFormat, frame, posicion);
            }
            log.debug("IntFormat generado:" + applyFilter(intFormat));
            return intFormat;
        } catch (Exception ex) {
            throw new FormatterException("No pudo crearse el formato interno", ex, frame, intFormat);
        }
    }
    
    private String applyFilter(InternalFormat intFmtToFilter) {
        return getFilters().filter(getFormatterId(), intFmtToFilter);        
    }
    
    /**
     * {@inheritDoc  }
     */
    public List getFields() {
        return this.getFieldsAsList();
    }
    
    /**
     * {@inheritDoc  }
     */
    public Field getFieldById(String id) {
        return this.getField(id);
    }
    
}
