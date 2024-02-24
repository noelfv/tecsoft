/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.field;

import com.novatronic.formatter.exception.FieldConfigurationException;
import com.novatronic.formatter.exception.FieldValueException;
import com.novatronic.formatter.field.support.BitMap;
import com.novatronic.formatter.field.util.BmpUtil;
import com.novatronic.formatter.internal.InternalField;
import com.novatronic.formatter.internal.InternalFormat;
import com.novatronic.formatter.util.VariableByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 * TODO: LogMF, realizar el concatenado manualmente, debiduo a problemas de inferencias.
 * @author Omar
 */
public class BmpField extends Field {

    private static final Logger log = Logger.getLogger(BmpField.class);
    private Map<String, Field> fieldsBmp;
    /**
     * Indica si el bitmap debe comprimirse o no (Se usa bytes directamente)
     */
    private boolean compress;

    public static interface Tag {

        public static final String FIELD = "field";
    }

    public static interface Attr {

        public static final String COMPRESS = "compress";
    }

    public BmpField() {
        BmpUtil.init();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public InternalField getValueAsInternalField(Properties params, InternalFormat intFmtCtx) {
        InternalFormat intFmt = new InternalFormat(getId());
        List<Field> fields;
        
        intFmtCtx.add(getId(), intFmt);
        intFmt.setParent(intFmtCtx);
        fields = new ArrayList<Field>(fieldsBmp.values());
        for (int i = 0; i < fields.size(); i++) {
            LogMF.trace(log, "{0}Hijo default={1}", logId, fields.get(i));
            if (!fields.get(i).isNullable()) {
                intFmt.addInternalField(fields.get(i).getValueAsInternalField(params, intFmt));
            }
        }
        return intFmt;
    }

    @Override
    protected void readCustomConfiguration(Element element) {
        List<Element> subcampos;
        logId = "[ID='" + getId() + "']";
        try {
            subcampos = element.getChildren(Tag.FIELD);
            compress = Boolean.parseBoolean(element.getAttributeValue(Attr.COMPRESS));

            fieldsBmp = new LinkedHashMap<String, Field>();
            for (int i = 0; i < subcampos.size(); i++) {
                Field son = FieldFactory.getField(subcampos.get(i), this);
                fieldsBmp.put(son.getId(), son);
            }
            LogMF.debug(log, "[ID={0}]compress={0},fields={1}", getId(), compress, fieldsBmp.size());
        } catch (Exception ex) {
            throw new FieldConfigurationException(logId + "Error al configurar "
                    + "el campo", ex);
        }
    }

    @Override
    public int putBytes(InternalFormat internalFormat, VariableByteBuffer frame) {
        List<Field> fields = new ArrayList(fieldsBmp.values());
        int posBmp = frame.getLength();
        int bytesWritten = 0;
        BitMap bitmap = new BitMap(128);
        byte[] bmpTrama;

        try {
            InternalFormat intFormatBmp = internalFormat.getIFmt(this.getId());
            for (Field field : fields) {
                bytesWritten += fieldParseFrame(field, intFormatBmp, frame, bitmap);
            }
            bmpTrama = BmpUtil.makeBitmap(bitmap, compress);
            LogMF.debug(log, "{0}BMP=[{1}],insertar en={2}", logId, new String(bmpTrama), posBmp);
            frame.insert(posBmp, bmpTrama, 0, bmpTrama.length);

            return bytesWritten + bmpTrama.length;
        } catch (Exception ex) {
            throw new FieldValueException(logId + "Error al leer la trama", ex);
        }
    }

    private int fieldParseFrame(Field field, InternalFormat intFormatBmp,
            VariableByteBuffer frame, BitMap bitmap) {
        int id;

        id = Integer.parseInt(field.getId());
        LogMF.trace(log, "{0}Bitmap flag-ID={1}", logId, field.getId());
        if (id == 1) {   //El campo ISO 1(secundary bitmap) se salta
            log.trace(logId + "Campo 1 no se procesa. BMP lo hace");
            return 0;
        } else if (intFormatBmp.getInternalField(field.getId()) != null) {
            BmpUtil.setUpBitFlag(bitmap, id);
            return field.putBytes(intFormatBmp, frame);
        } else {
            return 0;
        }
    }

    @Override
    public int readBytes(InternalFormat internalFormat, VariableByteBuffer frame, int posicion) {
        LogMF.debug(log, "{0}Leyendo bytes posicion[{1}]", logId, posicion);

        int bitmapLength;
        InternalFormat intFormatBmp;
        Field field;
        BitMap bitmap = new BitMap(64);

        try {
            intFormatBmp = new InternalFormat(this.getId());
            internalFormat.addInternalField(intFormatBmp);

            BmpUtil.readBitmap(bitmap, frame, posicion, compress);
            posicion += BmpUtil.getBitmapBytesLength(bitmap, compress);
            log.debug(logId + "New.Length=" + posicion + ",BMP generado=" + bitmap);

            bitmapLength = BmpUtil.getBitmapLength(bitmap);
            for (int i = 0; i < bitmapLength; i++) {
                if (bitmap.get(i) && (i != 0)) {
                    log.debug(logId + "Encontrado=" + (i + 1));
                    field = fieldsBmp.get(String.valueOf(i + 1));
                    if (field == null) {
                        throw new FieldValueException(logId + "El bit " + (i + 1)
                                + " no tiene un campo asociado");
                    }
                    posicion = field.readBytes(intFormatBmp, frame, posicion);
                }
            }
            return posicion;
        } catch (Exception ex) {
            throw new FieldValueException(logId + "Error al leer la trama", ex);
        }
    }
}
