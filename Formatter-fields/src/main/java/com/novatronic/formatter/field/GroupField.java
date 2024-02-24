/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.field;

import com.novatronic.formatter.exception.FieldException;
import com.novatronic.formatter.internal.InternalField;
import com.novatronic.formatter.internal.InternalFormat;
import com.novatronic.formatter.util.VariableByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 * @author ofernandez
 * @version 1.0
 * @since 1.0, 15/11/2010
 */
public class GroupField extends Field {

    private static final Logger log = Logger.getLogger(GroupField.class);
    private List<Field> fieldsGroup;

    public interface Tag {

        public static final String FIELD = "field";
    }

    @Override
    public InternalField getValueAsInternalField(Properties params, InternalFormat intFmtCtx) {
        InternalFormat innerIntFormat = new InternalFormat(getId());

        innerIntFormat.setParent(intFmtCtx);
        for (Field field : fieldsGroup) {
            if (!field.isNullable()) {
                innerIntFormat.addInternalField(field.getValueAsInternalField(params, innerIntFormat));
            }
        }

        return innerIntFormat;

    }

    @Override
    protected void readCustomConfiguration(Element element) {
        List<Element> subcampos = element.getChildren(Tag.FIELD);
        fieldsGroup = new ArrayList<Field>();
        for (int i = 0; i < subcampos.size(); i++) {
            Field son = FieldFactory.getField(subcampos.get(i), this);
            fieldsGroup.add(son);
        }
        LogMF.debug(log, "{0}fieldsAgrupados={1}", logId, fieldsGroup);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public int putBytes(InternalFormat internalFormat, VariableByteBuffer frame) {
        int length = 0;
        InternalFormat innerIntFormat;
        Field fieldBuilder;

        try {
            innerIntFormat = internalFormat.getIFmt(getId());
            for (int position = 0; position < fieldsGroup.size(); position++) {
                fieldBuilder = fieldsGroup.get(position);
                length += fieldBuilder.putBytes(innerIntFormat, frame);
            }

            return length;
        } catch (Exception ex) {
            throw new FieldException(logId + "Error al generar la trama", ex);
        }
    }

    @Override
    public int readBytes(InternalFormat internalFormat, VariableByteBuffer frame, int posicion) {
        InternalFormat innerIntFormat;
        Field fieldBuilder;

        try {
            innerIntFormat = new InternalFormat(this.getId());
            internalFormat.addInternalField(innerIntFormat);

            for (int position = 0; position < fieldsGroup.size(); position++) {
                fieldBuilder = fieldsGroup.get(position);
                posicion = fieldBuilder.readBytes(innerIntFormat, frame, posicion);
                LogMF.debug(log, "{0}Procesando hijo={1},pos={2}",
                        logId, fieldBuilder.getId(), posicion);
            }

            log.debug(logId + "Grupo leido");
            return posicion;
        } catch (Exception ex) {
            throw new FieldException(logId + "Error al leer la trama", ex);
        }
    }
}
