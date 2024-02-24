package com.novatronic.formatter.field;

import com.novatronic.formatter.exception.FieldConfigurationException;
import com.novatronic.formatter.exception.FieldException;
import com.novatronic.formatter.internal.InternalFormat;
import com.novatronic.formatter.util.VariableByteBuffer;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 * Esta campo leera una trama como una tira de caracteres hasta un encontrar un caracter
 * terminador el cua indicara el fin del campo. Luego se guardara dicho valor en el FI
 * pero sin el terminador. El caracter terminador sera configurable. De esta misma forma
 * colocara el terminador al colocar el campo nuevamente en la trama.
 *
 * @author Omar Fernandez
 * @version 1.0
 * @since 1.0 @date 05 Abr. 2012
 */
public class TokenField extends Field {

    private static final Logger log = Logger.getLogger(TokenField.class);
    private String token;
    private int tokenLength;
    private boolean jump;
    private boolean toend;

    private class Attr {

        public static final String TOKEN = "token";
        public static final String JUMP = "jump";
        public static final String TO_END = "toend";
    }

    private class Const {

        public static final int BYTE_SIZE = 1;
    }

    /**
     * {@inheritDoc }<br><br> Se lee la configuracion del caracter terminador
     *
     * @throws FieldConfigurationException En caso no se haya configurado este caracter.
     */
    @Override
    protected void readCustomConfiguration(Element element) {
        logId = "[id=" + getId() + "]";
        token = element.getAttributeValue(Attr.TOKEN);
        tokenLength = token.getBytes().length;
        jump = Boolean.parseBoolean(element.getAttributeValue(Attr.JUMP));
        toend = Boolean.parseBoolean(element.getAttributeValue(Attr.TO_END));
        if (token == null) {
            throw new FieldConfigurationException("se requiere el attributo " + Attr.TOKEN);
        }

        log.trace(logId + "token=" + token + ",tokenLength=" + tokenLength
                + ",jump=" + jump);
    }

    /**
     * {@inheritDoc }<br><br> Para este campo se coloca el valor campo del FI y se agrega
     * en la trama el termimador al final de dicho campo
     */
    @Override
    public int putBytes(InternalFormat internalFormat, VariableByteBuffer frame) {
        String value;
        int added;

        try {
            value = internalFormat.getValue(getId());
            log.trace(logId + "Agregando=" + applyFilter(value));
            added = frame.add(value);
            if (!jump) {
                log.trace(logId + "Agregando token=" + token);
                added += frame.add(token);
            }

            log.debug(logId + "Se agrego cant.bytes=" + added);

            return added;
        } catch (Exception ex) {
            throw new FieldException(logId + "Error al generar la trama", ex);
        }

    }

    /**
     * {@inheritDoc }<br><br> Para este campor se lee la trama hasta que encontrar el
     * terminador. Los caracteres leidos conformar el valor del campo sin tener en cuenta
     * el terminador
     */
    @Override
    public int readBytes(InternalFormat internalFormat, VariableByteBuffer frame, int position) {
        String temp;
        boolean frameWasFinished;
        boolean terminatorWasFound = false;
        VariableByteBuffer byteValue;
        byte bytes[];

        try {
            log.trace(logId + "leyendo:offset=[" + position + "]");
            byteValue = new VariableByteBuffer();
            frameWasFinished = ((frame.getLength() - position) == 0) && toend;
            while (!terminatorWasFound && !frameWasFinished) {
                bytes = frame.getBytes(position, tokenLength);
                temp = new String(bytes);
                if (temp.equals(token)) {
                    log.trace(logId + "Token encontrado, offset=" + position);
                    terminatorWasFound = true;
                    position += tokenLength;

                } else {
                    byteValue.add(frame.getBytes(position, Const.BYTE_SIZE));
                    position++;
                }
                frameWasFinished = ((frame.getLength() - position) == 0) && toend;
            }
            if (jump) {
                position -= tokenLength;
                log.trace(logId + "Jump de Token activo, se salta. Nuevo offset=" + position);
            }
            
            log.trace(logId + "Nuevo offset ubicado en=" + position + ",value=[" + applyFilter(byteValue.toString()) +"]");
            internalFormat.add(getId(), byteValue.toString());

            return position;

        } catch (Exception ex) {
            throw new FieldException(logId + "]Error al leer la trama", ex);
        }
    }
}
