/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.field.util.filler;

import com.novatronic.formatter.field.util.FieldFormat;
import java.util.Arrays;
import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public class Filler {

    private static final Logger log = Logger.getLogger(Filler.class);
    private Align align;
    private char charFill = NO_CHAR_FILL;
    private int length;
    private static final char NO_CHAR_FILL = ' ';
    private static final char NUMBER_CHAR_FILL = '0';
    private static final char ALPHA_CHAR_FILL = ' ';
    private static final char ALPHA_CHAR_FILL_COMPRESS = 'F';

    /**
     *
     * @param newFormat
     * @param newLength
     */
    public void setFormat(FieldFormat format, int newLength, boolean compress) {
        if (format == null) {
            setFormat(Align.LEFT, newLength, ALPHA_CHAR_FILL, false);

        } else if (format.equals(FieldFormat.NUMBER)) {
            setFormat(Align.RIGHT, newLength, NUMBER_CHAR_FILL, compress);
        } else {
            if (compress) {
                setFormat(Align.LEFT, newLength, ALPHA_CHAR_FILL_COMPRESS, compress);
            } else {
                setFormat(Align.LEFT, newLength, ALPHA_CHAR_FILL, compress);
            }
        }
    }

    public void setFormat(FieldFormat format, int newLength, char newCharFill, boolean compress) {
        if (format == null) {
            setFormat(Align.LEFT, newLength, newCharFill, compress);
        } else if (format.equals(FieldFormat.NUMBER)) {
            setFormat(Align.RIGHT, newLength, newCharFill, compress);
        } else {
            setFormat(Align.LEFT, newLength, newCharFill, compress);
        }
    }

    public void setFormat(Align newAlign, int newLength, char newCharFill, boolean compress) {
        if (compress && (newLength % 2 != 0)) {
            length = newLength + 1;

        } else {
            length = newLength;
        }
        charFill = newCharFill;
        align = newAlign;
    }

    public String fill(String value) {
        char[] fills;
        log.debug("valueLength=" + value.length() + ",align=" + align + ",length=" + length + ", charFill=[" + charFill + "]");
        try {
            if (value == null) {
                value = "";
            } else if (value.length() >= length) {
                log.debug("No se requiere relleno");
                return value;
            }

            fills = new char[length - value.length()];
            Arrays.fill(fills, charFill);
            log.debug("Fillers=" + Arrays.toString(fills));

            if (align.equals(Align.RIGHT)) {
                value = new String(fills) + value;
            } else {
                value = value + new String(fills);
            }

            return value;

        } catch (Exception ex) {
            throw new IllegalArgumentException("No fue posible rellenar el "
                    + "valor", ex);
        }
    }

    public String undoFill(String value) {
        String result = null;
        int index;
        log.debug("Undo fill; charFill=[" + charFill + "], align=" + align + ", valueLength=[" + value.length() + "]");
        if (align.equals(Align.RIGHT)) {
            index = 0;
            while (existMoreCharAndCharNotFound(index, value)) {
                index++;
            }
            result = value.substring(index);
        } else {
            index = value.length() - 1;
            while (existMoreCharAndCharNotFound(index, value)) {
                index--;
            }
            result = value.substring(0, index + 1);
        }
        log.debug("index=" + index + ",resultLength=[" + value.length() + "]");
        return result;
    }

    private boolean existMoreCharAndCharNotFound(int index, String value) {
        return (-1 < index) && (index < value.length()) && (value.charAt(index) == charFill);
    }
}
