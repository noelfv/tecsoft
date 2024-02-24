/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.field;

import com.novatronic.formatter.exception.FieldConfigurationException;
import com.novatronic.formatter.exception.FieldValueException;
import com.novatronic.formatter.field.util.Converter;
import com.novatronic.formatter.field.util.FieldFormat;
import com.novatronic.formatter.field.util.Validator;
import com.novatronic.formatter.field.util.filler.Align;
import com.novatronic.formatter.field.util.filler.Filler;
import com.novatronic.formatter.internal.InternalField;
import com.novatronic.formatter.internal.InternalFormat;
import com.novatronic.formatter.util.VariableByteBuffer;
import java.nio.charset.Charset;
import java.util.Properties;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 * @author ofernandez
 * @version 1.0
 * @since 1.0, 10/11/2010
 */
public class FixedField extends Field {

    private static final Logger log = Logger.getLogger(FixedField.class);
    private int length;
    private int byteLength;
    private FieldFormat format;
    private boolean compress;
    private boolean undoFill;
    private Align align;
    private Validator validator;
    private Filler filler;
    private String fillerChar;
    private Charset charset;

    public interface Attr {

        public static final String LENGTH = "length";
        public static final String FORMAT = "format";
        public static final String COMPRESS = "compress";
        public static final String ALIGN = "align";
        public static final String FILLER_CHAR = "filler";
        public static final String UNDO_FILL = "undofill";
        public static final String CHARSET = "charset";
    }

    public FixedField() {
        validator = new Validator();
        filler = new Filler();
    }
    
    /**
     * {@inheritDoc }
     *
     * @throws FieldValueException {@inheritDoc }
     */
    @Override
    public InternalField getValueAsInternalField(Properties params, InternalFormat intFmtCtx) {
        String value;
        
        value = getValue(params, intFmtCtx);
        if (!validator.isValid(value)) {
            throw new FieldValueException(logId + "El valor [" + applyFilter(value) + "] no "
                    + "corresponde a length=" + length + ",format=" + format);
        }
        return new InternalField(this.getId(), value);
    }

    /**
     * {@inheritDoc }
     *
     * @throws FieldConfigurationException {@inheritDoc}
     */
    @Override
    protected void readCustomConfiguration(Element element) {
        try {
            length = Integer.parseInt(element.getAttributeValue(Attr.LENGTH));
            format = FieldFormat.searchFormat(element.getAttributeValue(Attr.FORMAT));
            align = Align.searchAlign(element.getAttributeValue(Attr.ALIGN));
            compress = Boolean.parseBoolean(element.getAttributeValue(Attr.COMPRESS));
            fillerChar = element.getAttributeValue(Attr.FILLER_CHAR);
            undoFill = element.getAttributeValue(Attr.UNDO_FILL) == null ? true
                    : Boolean.parseBoolean(element.getAttributeValue(Attr.UNDO_FILL));
            charset = element.getAttributeValue(Attr.CHARSET) == null
                    ? Charset.defaultCharset()
                    : Charset.forName(element.getAttributeValue(Attr.CHARSET));

            validator.setFormat(format, length);
            if ((fillerChar != null) && (align != null)) {
                filler.setFormat(align, length, fillerChar.charAt(0), compress);
            } else if (fillerChar != null) {
                filler.setFormat(format, length, fillerChar.charAt(0), compress);
            } else {
                filler.setFormat(format, length, compress);
            }

            byteLength = getByteLength();

            log.debug(logId + "Leido: " + toString());
        } catch (Exception ex) {
            throw new FieldConfigurationException(logId + ",Error al leer"
                    + "la configuracion", ex);
        }
    }

    private int getByteLength() {
        if (compress) {
            return ((length % 2) == 1) ? (1 + length / 2) : length / 2;
        } else {
            return length;
        }
    }

    /**
     * {@inheritDoc }
     *
     * @throws FieldValueException {@inheritDoc}
     */
    @Override
    public int putBytes(InternalFormat internalFormat, VariableByteBuffer frame) {
        String value = internalFormat.getValue(this.getId());

        if (value == null) {
            throw new FieldValueException(logId + "Valor invalido =["
                    + applyFilter(value) + "], longitud=" + length + ",formato=" + format);
        }
        LogMF.trace(log, "{0}colocando value={1}", logId, applyFilter(value));

        if (!validator.isValid(value)) {
            throw new FieldValueException(logId + "Valor invalido =["
                    + applyFilter(value) + "], longitud=" + length + ",formato=" + format);
        }
        try {
            value = filler.fill(value);
        } catch (Exception ex) {
            throw new FieldValueException(logId +"No fue posible rellenar el campo con"
                    + " el valor=" + applyFilter(value), ex);
        }

        if (compress) {
            log.trace(logId + "compress.process value=[" + applyFilter(value) + "]");
            byte[] bytes = Converter.hexaToBytes(value);
            return frame.add(bytes, 0, bytes.length);
        } else {
            return frame.add(value);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public int readBytes(InternalFormat internalFormat, VariableByteBuffer frame,
            int posicion) {
        byte[] array;
        String value;

        try {
            LogMF.debug(log, "{0}Leyendo internalFormat en posicion={1}"
                    + ", byteLength={2}", logId, posicion, byteLength);

            array = frame.getBytes(posicion, byteLength);
            log.debug(logId + "Leido cant. bytes=" + array.length);

            if (compress) {
                value = undoFill 
                        ? filler.undoFill(Converter.toHexaString(array)) 
                        : Converter.toHexaString(array);
            } else {
                value = undoFill 
                        ? filler.undoFill(new String(array, charset.name())) 
                        : new String(array);
            }

            if (!validator.isValid(value)) {
                throw new FieldValueException(logId + "Valor invalido =["
                        + applyFilter(value) + "], longitud=" + length + ",formato=" + format);
            }
            internalFormat.add(this.getId(), value);

            return posicion + array.length;
        } catch (Exception ex) {
            throw new FieldValueException(logId +"Error al escribir en la trama", ex);
        }
    }

    @Override
    public String toString() {
        return "FixedField{"
                + "length=" + length
                + ", byteLength=" + byteLength
                + ", format=" + format
                + ", compress=" + compress
                + ", undoFill=" + undoFill
                + ", validator=" + validator
                + ", filler=" + filler
                + ", encode=" + charset
                + ", fillerChar=" + fillerChar + '}';
    }
}
