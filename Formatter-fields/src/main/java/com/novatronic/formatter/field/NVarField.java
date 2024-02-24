/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.field;

import com.novatronic.formatter.exception.FieldConfigurationException;
import com.novatronic.formatter.exception.FieldValueException;
import com.novatronic.formatter.field.util.Converter;
import com.novatronic.formatter.field.util.FieldFormat;
import com.novatronic.formatter.field.util.StringUtil;
import com.novatronic.formatter.field.util.filler.Filler;
import com.novatronic.formatter.internal.InternalFormat;
import com.novatronic.formatter.util.VariableByteBuffer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 * @author nteruya
 * TODO: cambiar el atributo de length a max.
 * TODO: max no debe ser obligatorio, por defecto debe tomar el valor maximo 
 * acorde a la cantidad de l(ej: nl=2, x defecto max=99)
 * TODO: agregar la validacion del maximo, en caso se especifique.
 * TODO: test a realizar con formato binario y ascii.
 */
public class NVarField extends Field {

    public final static Logger log = Logger.getLogger(NVarField.class);
    /**
     *     */
    private NumberFormat headerFormat;
    /**
     * Indica la maxima longitud del contenido del campo
     */
    private int length;
    /**
     * Indica el tamaño de la cabecera: LL,LLL,...
     */
    private int headerLength;
    /**
     * Indica si la cabecera debe comprimirse o no (Se usa formato BCD)
     */
    private boolean compress;
    /**
     *      */
    private FieldFormat format;
    private Filler filler;
    private char fillerChar;
    private boolean useFillerChar;

    public static interface Attr {

        public static final String LENGTH = "length";
        public static final String LENGTH_INDICATOR = "nl";
        public static final String COMPRESS = "compress";
        public static final String FORMAT = "format";
        public static final String FILLER_CHAR = "filler";
    }
    
    public static interface Const {

        public static final String L_FILLER = "0";
    }

    public NVarField() {
        filler = new Filler();
    }

    /**
     * {@inheritDoc }
     *
     * @throws FieldConfigurationException {@inheritDoc }
     */
    @Override
    protected void readCustomConfiguration(Element element) {
        String fillerCharString;
        
        try {
            length = Integer.parseInt(element.getAttributeValue(Attr.LENGTH));
            headerLength = Integer.parseInt(element.getAttributeValue(
                    Attr.LENGTH_INDICATOR));
            compress = Boolean.parseBoolean(element.getAttributeValue(Attr.COMPRESS));
            format = FieldFormat.searchFormat(element.getAttributeValue(Attr.FORMAT));
            format = (format == null) ? FieldFormat.ALL : format;
            fillerCharString = element.getAttributeValue(Attr.FILLER_CHAR);
            useFillerChar = fillerCharString != null;

            if (useFillerChar) {
                fillerChar = fillerCharString.charAt(0);
            }

            processLengthIndicator();

        } catch (Exception ex) {
            throw new FieldConfigurationException(logId +"Error al leer la "
                    + "configuracion", ex);
        }
    }

    private void processLengthIndicator() {
        headerFormat = new DecimalFormat(StringUtil.repeatString(Const.L_FILLER, headerLength));
        LogMF.debug(log, "{0}Longitud cabecera:{1}", logId ,headerLength);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public int putBytes(InternalFormat internalFormat, VariableByteBuffer frame) {
        LogMF.debug(log, "{0}Colocando bytes ", logId);
        String value = internalFormat.getValue(this.getId());
        int dataLength = value.length();
        int bytesLength = frame.add(getHeader(value.length()));

        LogMF.trace(log, "{0}Format={1},useFillerChar={2},value={3},fillerChar={4}",
                new Object[]{logId,format, useFillerChar, applyFilter(value), fillerChar});
        try {
            if (compress && !format.equals(FieldFormat.ALL)) {
                log.debug("Compress.format = " + format);
                dataLength = (dataLength % 2 == 1) ? dataLength + 1 : dataLength;
                if (useFillerChar) {
                    filler.setFormat(format, dataLength, fillerChar, compress);
                    value = filler.fill(value);
                } else {
                    filler.setFormat(format, dataLength, compress);
                    value = filler.fill(value);
                }
                bytesLength += frame.add(Converter.hexaToBytes(value));
            } else {
                log.debug(logId + "NoCompres.format = " + format);
                bytesLength += frame.add(value);
            }
            LogMF.debug(log, "{0}Se agregaron {1} bytes", logId, bytesLength);
            
        } catch (Exception ex) {
            throw new FieldValueException(logId + "No fue posible rellenar el "
                    + "campo con el valor=" + applyFilter(value), ex);
        }

        return bytesLength;
    }

    private byte[] getHeader(int num) {
        String header = headerFormat.format(num);
        if (compress) {
            return Converter.hexaToBytes(header);
        } else {
            return header.getBytes();
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public int readBytes(InternalFormat internalFormat, VariableByteBuffer frame,
            int posicion) {
        int lengthField;
        byte[] bytesRead;

        try {
            LogMF.debug(log, "{0}intFormat en posicion={1}, longMax={2}",logId,posicion, length);
            bytesRead = frame.getBytes(posicion, getLength());
            posicion += bytesRead.length;
            lengthField = getLengthField(bytesRead);
            LogMF.debug(log, "{0}Cabecera={1}", logId,lengthField);

            if (lengthField == 0) {
                internalFormat.add(getId(), "");
            } else {
                posicion += readData(lengthField, posicion, frame, internalFormat);
            }

            return posicion;
        } catch (Exception ex) {
            throw new FieldValueException(logId + "Error al leer de la trama", ex);
        }
    }

    private int readData(int lengthField, int posicion, VariableByteBuffer frame,
            InternalFormat internalFormat) {
        byte[] bytesRead;
        String value;

        if (compress && !format.equals(FieldFormat.ALL)) {
            LogMF.debug(log, "{0}procesando data comprimida", logId);
            if ((lengthField % 2) == 1) {
                lengthField = (lengthField + 1) / 2;
                bytesRead = frame.getBytes(posicion, lengthField);
                value = Converter.toHexaString(bytesRead);
                value = value.substring(0, value.length() - 1);
                internalFormat.add(getId(), value);
            } else {
                lengthField = lengthField / 2;
                bytesRead = frame.getBytes(posicion, lengthField);
                value = Converter.toHexaString(bytesRead);
                internalFormat.add(getId(), value);
            }
        } else {
            bytesRead = frame.getBytes(posicion, lengthField);
            value = new String(bytesRead);
            internalFormat.add(getId(), value);
        }

        LogMF.debug(log, "{0}value=[{1}]", logId, applyFilter(value));
        
        return bytesRead.length;
    }

    private int getLength() {
        if (compress) {
            return ((headerLength % 2) == 1) ? (1 + headerLength / 2) : headerLength / 2;
        } else {
            return headerLength;
        }
    }

    private int getLengthField(byte[] bytesRead) {
        if (compress) {
            return Integer.parseInt(Converter.toHexaString(bytesRead));
        } else {
            return Integer.parseInt(new String(bytesRead));
        }
    }

    @Override
    public String toString() {
        return "NVarField{" + "headerFormat=" + headerFormat 
                + ", length=" + length 
                + ", headerLength=" + headerLength 
                + ", compress=" + compress 
                + ", format=" + format 
                + ", filler=" + filler 
                + ", fillerChar=" + fillerChar 
                + ", useFillerChar=" + useFillerChar + '}';
    }
    
}
