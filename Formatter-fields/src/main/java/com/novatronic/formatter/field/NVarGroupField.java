/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.field;

import com.novatronic.formatter.exception.FieldConfigurationException;
import com.novatronic.formatter.exception.FieldValueException;
import com.novatronic.formatter.field.util.Converter;
import com.novatronic.formatter.field.util.StringUtil;
import com.novatronic.formatter.internal.InternalField;
import com.novatronic.formatter.internal.InternalFormat;
import com.novatronic.formatter.util.VariableByteBuffer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
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
 * TODO: cambiar el atributo de length a max.
 * TODO: max no debe ser obligatorio, por defecto debe tomar el valor maximo 
 * acorde a la cantidad de l(ej: nl=2, x defecto max=99)
 * TODO: agregar la validacion del maximo, en caso se especifique.
 * TODO: test a realizar con formato binario y ascii.
 */
public class NVarGroupField extends Field {

    private static Logger log = Logger.getLogger(NVarGroupField.class);
    /**
     * Indica si la cabecera debe comprimirse o no (Se usa formato BCD)
     */
    private boolean compress;
    /**
     *
     */
    private List<Field> fields;
    /**
     *
     */
    private NumberFormat headerFormat;
    /**
     * Indica el tamaño de la cabecera: LL,LLL,...
     */
    private int headerLength;

    public interface Tag {

        public static final String FIELD = "field";
    }

    public interface Attr {

        public static final String LENGTH_INDICATOR = "nl";
        public static final String COMPRESS = "compress";
    }

    public static interface Const {

        public static final String L_FILLER = "0";
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public InternalField getValueAsInternalField(Properties params, InternalFormat intFmtCtx) {
        InternalFormat internalFormat;

        internalFormat = new InternalFormat(getId());
        internalFormat.setParent(intFmtCtx);
        LogMF.trace(log, "{0}Creando intFormat por omision", logId);
        for (Field field : fields) {
            if (!field.isNullable()) {
                log.trace(logId + "Procesando:" + field.getId());
                internalFormat.addInternalField(field.getValueAsInternalField(params, internalFormat));
            }
        }
        return internalFormat;
    }

    @Override
    protected void readCustomConfiguration(Element element) {
        try {
            headerLength = Integer.parseInt(element.getAttributeValue(
                    Attr.LENGTH_INDICATOR));
            compress = Boolean.parseBoolean(element.getAttributeValue(Attr.COMPRESS));

            processLengthIndicator();
            processSubFields(element);
            LogMF.debug(log, "{0}longitud:{1}, compress:{2}", logId, headerLength, compress);
        } catch (Exception ex) {
            throw new FieldConfigurationException(logId + " Error al leer"
                    + " la configuracion", ex);
        }
    }

    private void processLengthIndicator() {
        headerFormat = new DecimalFormat(
                StringUtil.repeatString(Const.L_FILLER, headerLength));
    }

    private void processSubFields(Element element) {
        List<Element> subcampos = element.getChildren(Tag.FIELD);
        fields = new ArrayList<Field>();
        for (int i = 0; i < subcampos.size(); i++) {
            Field son = FieldFactory.getField(subcampos.get(i), this);
            fields.add(son);
            LogMF.debug(log, "{0}Agregando campo hijo:{1}", logId, son);
        }
    }

    @Override
    public int putBytes(InternalFormat internalFormat, VariableByteBuffer frame) {
        InternalFormat intFormat;
        VariableByteBuffer buffer;
        int bytesWritten;

        try {
            intFormat = internalFormat.getIFmt(getId());
            buffer = new VariableByteBuffer(fields.size() * 30);

            for (int i = 0; i < fields.size(); i++) {
                fields.get(i).putBytes(intFormat, buffer);
            }
            LogMF.debug(log, "{0}Buffer interno[{1}]", logId, buffer.getLength());
            bytesWritten = frame.add(getHeader(buffer.getLength()));
            bytesWritten += frame.add(buffer.getByteArray());

            return bytesWritten;
        } catch (Exception ex) {
            throw new FieldValueException(logId + "Error al generar la trama", ex);
        }


    }

    private byte[] getHeader(int num) {
        String header = headerFormat.format(num);
        if (compress) {
            return Converter.hexaToBytes(header);
        } else {
            return header.getBytes();
        }
    }

    @Override
    public int readBytes(InternalFormat internalFormat, VariableByteBuffer frame, int posicion) {
        int contentLength;
        int innerPosition;
        byte[] bytesRead;
        InternalFormat innerIntFormat;

        try {
            innerIntFormat = new InternalFormat(this.getId());
            internalFormat.addInternalField(innerIntFormat);

            bytesRead = frame.getBytes(posicion, getLength());
            posicion += bytesRead.length;
            innerPosition = posicion;

            contentLength = getContentLength(bytesRead);
            LogMF.debug(log, "{0}cabecera={1},In.Pos={2}", logId, contentLength, innerPosition);

            if (contentLength != 0) {
                for (int i = 0; i < fields.size(); i++) {
                    innerPosition = fields.get(i).readBytes(innerIntFormat, frame, innerPosition);
                    LogMF.debug(log, "{0}In.Pos={1}", logId, innerPosition);
                }
            }
            LogMF.trace(log, "{0}Agregando FI creado con Campos.hijos:", logId, frame.getLength());
            log.trace(logId + "Agregando FI creado:" + innerIntFormat.getPath());
            internalFormat.addInternalField(innerIntFormat);
            LogMF.trace(log, "{0}Devolvemos nueva posicion={1}", logId, posicion + contentLength);

            return posicion + contentLength;

        } catch (Exception ex) {
            throw new FieldValueException(logId + "Error al leer la trama", ex);
        }
    }

    private int getLength() {
        if (compress) {
            return ((headerLength % 2) == 1) ? (1 + headerLength / 2) : headerLength / 2;
        } else {
            return headerLength;
        }
    }

    private int getContentLength(byte[] bytesRead) {
        if (compress) {
            return Integer.parseInt(Converter.toHexaString(bytesRead));
        } else {
            return Integer.parseInt(new String(bytesRead));
        }
    }
}
