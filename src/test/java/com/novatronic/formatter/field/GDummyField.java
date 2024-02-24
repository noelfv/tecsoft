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
 */
public class GDummyField extends Field {

    private static final Logger log = Logger.getLogger(GDummyField.class);
    private List<Field> fieldsGroup;
    private String ID;

    private class Tag {

        private static final String FIELD = "field";
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
        List<Element> subcampos;

        ID = "[id=" + getId() + "]";
        subcampos = element.getChildren(Tag.FIELD);
        fieldsGroup = new ArrayList<Field>();
        if(subcampos != null){
            for (int i = 0; i < subcampos.size(); i++) {
                Field son = FieldFactory.getField(subcampos.get(i), this);
                fieldsGroup.add(son);
                log.trace("Agregado campo hijo:ID=[" + son.getId() + "],type=[" + son.getType() + "],path=[" + son.getPath() + "]");
            }
        }
        LogMF.debug(log, "{0}fieldsAgrupados={1}", ID, fieldsGroup);
    }

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
            throw new FieldException(ID + "Error al generar la trama", ex);
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
                        ID, fieldBuilder.getId(), posicion);
            }

            log.debug("Grupo leido");
            return posicion;
        } catch (Exception ex) {
            throw new FieldException(ID + "Error al leer la trama", ex);
        }
    }

    public List<Field> getChildrenField() {
        return fieldsGroup;
    }
    public Field getChildFieldById(String id) {
        Field el;
        
        el = null;
        for (Field field : fieldsGroup) {
            if (field.getId().equals(id)) {
                el = field;
                break;
            }
        }
        return el;
    }
}
