package com.novatronic.formatter.field;

import com.novatronic.formatter.exception.FieldConfigurationException;
import com.novatronic.formatter.exception.FieldException;
import com.novatronic.formatter.field.support.FieldStorage;
import com.novatronic.formatter.field.support.KeyCollection;
import com.novatronic.formatter.field.support.ParseId;
import com.novatronic.formatter.internal.InternalField;
import com.novatronic.formatter.internal.InternalFormat;
import com.novatronic.formatter.util.VariableByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.LogSF;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 * Existen campos en el formato iso8583 donde se registra data aplicativa (p.e. campo 120,
 * 125 y 126). Si bien puede colocarse un formato dado para c/u de estos campos, en
 * ciertos casos se este formato (ya como trama) esta repartido en estos campos. Por
 * ejemplo si tenemos un grupo de campos que generan una trama de 300 bytes, luego este
 * debe repartirse del siguiente modo: en el campo 120->200bytes, en el campo 125->50Bytes
 * y en 126->El resto.<br/><br/> Este campo realiza esta funcion permitiendo tener un
 * formato y repartirlo en otros campos como el ejemplo mencionado. Debido que el repartir
 * los campos (al formar la trama) o leer los campos (cuando se lee la trama), tiene
 * distintos momentos, es necesario separarlo como una suerte de dos campos que trabajan
 * de manera conjunta. FIX: No existe una forma de poder establecer comunicacion entre
 * campos y por tanto era necesario crear un para este caso, pero debido a que se empleo
 * una clase estatica, el nombre del grupo: gname, tiene conflicto al emplear el mismo en
 * una misma JVM.
 *
 * @author Omar Fernandez
 * @version 1.0
 * @since 1.0, 05 Abr. 2012
 */
public class CollectGroupField extends Field {

    private static final Logger log = Logger.getLogger(CollectGroupField.class);
    private boolean isCollector;
    private String groupName;
    private List<Field> fields;
    private List<FieldStorage> fieldsStorage;
    private KeyCollection keyCollection;

    public interface Tag {

        public static final String FIELD = "field";
    }

    public interface Attr {

        public static final String GROUP_NAME = "gname";
        public static final String EXP = "exp";
        public static final String COLLECTOR = "collec";
    }

    public static interface Const {

        public static final String SEPARATOR = "$";
        public static final String CONFIG_FIELDS = "fields";
        public static final String KEY_FIELDS = "keys";
        public static final String KEY_COLLECTION = "keyCollection";
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public InternalField getValueAsInternalField(Properties params, InternalFormat intFmtCtx) {
        InternalFormat innerIntFormat;

        innerIntFormat = new InternalFormat(getId());
        intFmtCtx.addInternalField(innerIntFormat);
        //innerIntFormat.setParent(intFmtCtx);
        for (Field field : fields) {
            if (!field.isNullable()) {
                innerIntFormat.addInternalField(field.getValueAsInternalField(params, innerIntFormat));
            }
        }
        return innerIntFormat;
    }

    /**
     * {@inheritDoc }<br/><br/>
     *
     * @throws FieldConfigurationException En caso no se haya configurado este caracter.
     */
    @Override
    protected void readCustomConfiguration(Element element) {

        try {
            groupName = element.getAttributeValue(Attr.GROUP_NAME);
            isCollector = Boolean.parseBoolean(element.getAttributeValue(Attr.COLLECTOR));
            retrieveKeyCollection();
            parseKeys(element);
            configSubFields(element);

            log.trace(logId + "keyCollection=" + keyCollection);
            log.trace(logId + "fields=" + fields);
            log.trace(logId + "ids=" + fieldsStorage);
        } catch (Exception ex) {
            throw new FieldConfigurationException(logId + "Error en la configuracion", ex);
        }
    }

    private void retrieveKeyCollection() {
        keyCollection = (KeyCollection) getConfigContext().getData(Const.KEY_COLLECTION + Const.SEPARATOR + groupName);
        if (keyCollection == null) {
            log.trace(logId + "No se tiene Key de la coleccion, se crea nuevo");
            keyCollection = new KeyCollection();
            getConfigContext().putData(Const.KEY_COLLECTION + Const.SEPARATOR + groupName, keyCollection);
        }
    }

    private void parseKeys(Element element) {
        String exp;

        retrieveKeys();
        exp = element.getAttributeValue(Attr.EXP);
        if (exp != null) {
            log.trace(logId + "Se tiene expresion de IDs, se parse");
            fieldsStorage.addAll(ParseId.getIds(exp));
        }
    }

    private void retrieveKeys() {
        fieldsStorage = (List) getConfigContext().getData(Const.KEY_FIELDS + Const.SEPARATOR + groupName);
        if (fieldsStorage == null) {
            log.trace(logId + "No se tiene IDs, se crea nuevo");
            fieldsStorage = new ArrayList<FieldStorage>();
            getConfigContext().putData(Const.KEY_FIELDS + Const.SEPARATOR + groupName, fieldsStorage);
        }
    }

    private void configSubFields(Element element) {
        Field son;
        List<Element> subcampos;

        retrieveSubFields();
        subcampos = element.getChildren(Tag.FIELD);
        if (!subcampos.isEmpty()) {
            log.trace(logId + "Se tiene subcampos:" + getId());
            keyCollection.setId(getId());
            for (int i = 0; i < subcampos.size(); i++) {
                son = FieldFactory.getField(subcampos.get(i), this);
                fields.add(son);
            }
        }
    }

    private void retrieveSubFields() {
        fields = (List) getConfigContext().getData(Const.CONFIG_FIELDS + Const.SEPARATOR + groupName);
        if (fields == null) {
            log.trace(logId + "No se agrego campos, se crea nuevo");
            fields = new ArrayList<Field>();
            getConfigContext().putData(Const.CONFIG_FIELDS + Const.SEPARATOR + groupName, fields);
        }
    }

    /**
     * {@inheritDoc }<br><br> Para este campo se coloca el valor campo del FI y se agrega
     * en la trama el termimador al final de dicho campo
     */
    @Override
    public int putBytes(InternalFormat internalFormat, VariableByteBuffer frame) {
        InternalFormat innerIntFormat;
        Field field;
        VariableByteBuffer frameToSplit;
        String frameToSplitStr;
        FieldStorage fieldStorage;
        int offset = 0;
        int remaining;
        boolean hasMoreData = true;
        int index = 0;

        try {
            log.debug(logId + "isCollector=" + isCollector + ",keyCollection=" + keyCollection);
            if (!isCollector) { // Es divider
                log.trace(logId + "Procesando Bytes...");
                //Procesamos los subcampos para generar la trama a ser dividida
                frameToSplit = new VariableByteBuffer(100 * fields.size());
                innerIntFormat = internalFormat.getIFmt(keyCollection.getId());
                for (int i = 0; i < fields.size(); i++) {
                    field = fields.get(i);
                    field.putBytes(innerIntFormat, frameToSplit);
                }
                remaining = frameToSplit.getLength();
                log.trace(logId + "Subcampos procesados:remaining=" + remaining);

                //Con la trama armada, ahora la dividimos
                while (hasMoreData(hasMoreData, index, remaining, offset)) {
                    fieldStorage = fieldsStorage.get(index);
                    log.trace(logId + "fieldData=" + fieldStorage);
                    //FIX: La comparacion no es del todo correcta
                    if (fieldStorage.getMaxSize() > remaining) {
                        log.trace(logId + "All->target.size>remaining:" + remaining + ",offset=" + offset);
                        storeAllBytes(internalFormat, fieldStorage, remaining, frameToSplit, offset);
                        remaining -= remaining;
                        hasMoreData = false;
                    } else {
                        log.trace(logId + "Part->target.size<=remaining:" + remaining + ",offset=" + offset);
                        frameToSplitStr = new String(frameToSplit.getBytes(offset, fieldStorage.getMaxSize()));
                        //log.trace(logId + "Valor obtenido=[" + frameToSplitStr + "]");
                        remaining -= fieldStorage.getMaxSize();
                        internalFormat.add("/" + fieldStorage.getId(), frameToSplitStr);
                        offset += fieldStorage.getMaxSize();
                        hasMoreData = (remaining != 0);
                    }
                    index++;
                }
            }
            //No se colocan bytes, solo se ubican en el IF para ser procesados por otros campos
            return 0;
        } catch (Exception ex) {
            throw new FieldException(logId + "Error al generar la trama", ex);
        }
    }

    private boolean hasMoreData(boolean hasMoreData, int fieldIndex, int remaining, int offset) {
        LogSF.trace(log, "hasMoreData={},fieldIndex={},remaining={}", hasMoreData, fieldIndex, remaining);
        if (hasMoreData && (fieldIndex >= fieldsStorage.size())) {
            throw new FieldException(logId + "Aun queda data por agregar"
                    + " y no se configuro mas espacio. Restante=" + remaining
                    + ", offset=" + offset);
        }

        return hasMoreData;
    }

    private void storeAllBytes(InternalFormat internalFormat, FieldStorage fieldStorage, 
            int remaining, VariableByteBuffer frameToSplit, int offset) {
        String frameToSplitStr;
        
        frameToSplitStr = new String(frameToSplit.getBytes(offset, remaining));
        //log.trace(logId + "Valor obtenido=[" + frameToSplitStr + "]");
        internalFormat.add("/" + fieldStorage.getId(), frameToSplitStr);
    }

    /**
     * {@inheritDoc }<br><br> Para este campor se lee la trama hasta que encontrar el
     * terminador. Los caracteres leidos conformar el valor del campo sin tener en cuenta
     * el terminador
     */
    @Override
    public int readBytes(InternalFormat internalFormat, VariableByteBuffer frame, int position) {
        InternalFormat innerIntFormat;
        String value;
        Field subField;
        VariableByteBuffer tempFrame;
        int subPosition = 0;

        try {
            if (isCollector) {
                tempFrame = new VariableByteBuffer(100 * fieldsStorage.size());
                for (int i = 0; i < fieldsStorage.size(); i++) {
                    log.trace(logId + "Buscando id para unir:ID=" + fieldsStorage.get(i).getId());
                    value = internalFormat.getValue("/" + fieldsStorage.get(i).getId());
                    if (value != null) {
                        log.trace(logId + "ID Encontrado, se une");
                        tempFrame.add(value);
                    }
                }
                log.trace(logId + "Frame creado con longitud=" + tempFrame.getLength());

                innerIntFormat = new InternalFormat(this.getId());
                internalFormat.addInternalField(innerIntFormat);

                for (int index = 0; index < fields.size(); index++) {

                    subField = fields.get(index);
                    subPosition = subField.readBytes(innerIntFormat, tempFrame, subPosition);
                    log.trace(logId + "[" + subField.getId() + "]Pos=" + subPosition);
                }
            }

            return position;

        } catch (Exception ex) {
            throw new FieldException(logId + "Error al leer la trama", ex);
        }
    }
}
