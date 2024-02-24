/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.field;

import com.novatronic.formatter.exception.FieldConfigurationException;
import com.novatronic.formatter.exception.FieldException;
import com.novatronic.formatter.exception.FieldValueException;
import com.novatronic.formatter.internal.InternalField;
import com.novatronic.formatter.internal.InternalFormat;
import com.novatronic.formatter.util.VariableByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 * @author ofernandez
 * @version 1.0
 * @since 1.0, 15/11/2010
 */
public class ListListField extends Field {

    private static final Logger log = Logger.getLogger(ListListField.class);
    private String refId;
    private int size;
    private boolean isReferenceMode;
    private boolean refRoot;
    private List<Field> fieldsGroup;
    private static final String FIELD_PATH_SEPARATOR = ".";
    private static final String SEQ = "#";

    public interface Tag {

        public static final String FIELD = "field";
    }

    public interface Attr {

        public static final String REF_ID = "refId";
        public static final String SIZE = "size";
        public static final String REF_ROOT = "refRoot";
    }

    /**
     * {@inheritDoc }
     *
     * @throws FieldValueException {@inheritDoc }
     */
    @Override
    public InternalField getValueAsInternalField(Properties params, InternalFormat intFmtCtx) {
        String value;
        int paramSize;
        InternalFormat fieldElement;
        InternalFormat ifList;

        try {
            log.trace(logId + "Procesando valor por omision...");
            value = getParamValue(params, intFmtCtx);
            paramSize = (value == null) ? 1 : Integer.parseInt(value);
            ifList = new InternalFormat(getId());
            ifList.setParent(intFmtCtx);
            for (int i = 0; i < paramSize; i++) {
                fieldElement = new InternalFormat(String.valueOf(i));
                for (Field fieldBuilder : fieldsGroup) {
                    if (!fieldBuilder.isNullable()) {
                        fieldElement.addInternalField(fieldBuilder.getValueAsInternalField(params, intFmtCtx));
                        log.debug(logId + "FI de configuracion, agrega campo=" + fieldElement.getId());
                    }
                }
                ifList.addInternalField(fieldElement);
            }
            return ifList;
        } catch (Exception ex) {
            throw new FieldValueException(logId + "No se pudo obtener el valor omision", ex);
        }
    }

    @Override
    protected void readCustomConfiguration(Element element) {
        try {
            selectRefMode(element);
            refRoot = Boolean.parseBoolean(element.getAttributeValue(Attr.REF_ROOT));
            List<Element> subcampos = element.getChildren(Tag.FIELD);
            fieldsGroup = new ArrayList<Field>();
            for (int i = 0; i < subcampos.size(); i++) {
                Field son = FieldFactory.getField(subcampos.get(i), this);
                log.trace(logId + "Field hijo obtenido[parentPath=" + son.getParentPath() 
                        + ", path=" + son.getPath() + ']');
                fieldsGroup.add(son);
            }
            log.debug(logId + "RefId=" + refId + ", fieldsAgrupados=" + fieldsGroup);
        } catch (Exception ex) {
            throw new FieldConfigurationException(logId + "Error en la configuracion", ex);
        }
    }

    private void selectRefMode(Element element) {
        refId = element.getAttributeValue(Attr.REF_ID);
        if (refId == null) {
            size = Integer.parseInt(element.getAttributeValue(Attr.SIZE));
            isReferenceMode = false;
        } else {
            isReferenceMode = true;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public int putBytes(InternalFormat internalFormat, VariableByteBuffer frame) {
        int length = 0;
        List<InternalField> fields;
        Field fieldBuilder;
        InternalField fieldData;
        InternalFormat listData;

        try {
            listData = internalFormat.getIFmt(getId());
            if (listData == null) {
                length = 0;
            } else {
                fields = listData.getInternalFieldsAsList();
                for (int i = 0; i < fields.size(); i++) {
                    fieldData = fields.get(i);
                    fillDefaultValues((InternalFormat) fieldData);
                    for (int j = 0; j < fieldsGroup.size(); j++) {
                        fieldBuilder = fieldsGroup.get(j);
                        log.debug(logId + fieldData);
                        length += fieldBuilder.putBytes((InternalFormat) fieldData, frame);
                    }
                }
            }
            
            return length;
            
        } catch (Exception ex) {
            throw new FieldException(logId + "Error al generar la trama", ex);
        }
    }

    private void fillDefaultValues(InternalFormat intFormat) {
        for (int i = 0; i < fieldsGroup.size(); i++) {
            log.debug(logId + "Hijo default=" + fieldsGroup.get(i));
            Field field = fieldsGroup.get(i);
            InternalField intField = intFormat.getInternalField(field.getId());
            if ((intField == null) && !field.isNullable()) {
                log.trace(logId + "Valor cte para[" + field.getId() + "]");
                intFormat.addInternalField(field.getValueAsInternalField());
            }

        }
    }

    @Override
    public int readBytes(InternalFormat internalFormat, VariableByteBuffer frame, int posicion) {
        InternalFormat innerIntFormat;
        int nuevaPos;
        int groupSize;

        try {
            log.debug(logId + "Leyendo bytes del grupo, posicion=" + posicion);
            innerIntFormat = new InternalFormat(this.getId());
            internalFormat.addInternalField(innerIntFormat);
            groupSize = getSize(internalFormat);
            log.debug(logId + "Grupo de tamaño:" + groupSize);


            nuevaPos = posicion;
            for (int i = 0; i < groupSize; i++) {
                nuevaPos = readInnerFields(innerIntFormat, frame, nuevaPos, i);
                log.debug(logId + "[c-" + i + "]Leido=" + nuevaPos + " bytes");
            }
            log.debug(logId + "Grupo leido");

            return nuevaPos;

        } catch (Exception ex) {
            throw new FieldException(logId + "Error al leer la trama", ex);
        }
    }

    private int getSize(InternalFormat internalFormat) {
        if (isReferenceMode) {
            if (refRoot) {
                return Integer.valueOf(internalFormat.getValue("/" + refId));
            } else {
                return Integer.valueOf(internalFormat.getValue(refId));
            }
        } else {
            return size;
        }
    }
    /**
     * TODO: No esta aplicando el filtrado a los campos que contiene este campo; 
     * debido a que, se le antepone un id de secuencia. Sobreescribir el metodo 
     * de filtrado excluyendo el secuencia del path de los campos.
     * 
     * @param intFormat
     * @param frame
     * @param posicion
     * @param id
     * @return 
     */
    private int readInnerFields(InternalFormat intFormat, VariableByteBuffer frame,
            int posicion, int id) {
        InternalFormat innerIntFormat;
        int nuevaPos;

        log.debug(logId + "Leyendo campos inner en posicion=" + posicion + ", total"
                + " sub-campos=" + fieldsGroup.size());
        innerIntFormat = new InternalFormat(String.valueOf(id));
        intFormat.addInternalField(innerIntFormat);

        nuevaPos = posicion;
        for (int i = 0; i < fieldsGroup.size(); i++) {
            log.debug(logId + "[" + i + "]Leyendo campo interno en poscion=" + nuevaPos);
            nuevaPos = fieldsGroup.get(i).readBytes(innerIntFormat, frame, nuevaPos);
        }
        log.debug(logId + "Campos agregados en innerIntFormat");

        return nuevaPos;
    }

    /**
     * THOT: Se deberia crear un Rule Builder, el cual tenga un constructor de 
     * reglas
     */
    @Override
    public String getFilterPath() {
        return getPath() + FIELD_PATH_SEPARATOR + SEQ;
    }
        
}
