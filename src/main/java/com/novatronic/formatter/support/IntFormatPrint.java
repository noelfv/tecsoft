/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.support;

import com.novatronic.formatter.internal.InternalField;
import com.novatronic.formatter.internal.InternalFormat;
import com.novatronic.formatter.util.VariableByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ofernandez
 */
public class IntFormatPrint {

    /**
     * Devuelve la cadena que respresenta un formato interno y ordena los campos
     * y subcampos en forma de arbol con los valores de los mismos.
     *
     * @param intFormat Formato interno
     * @param size Tamanio de la memoria que procesara la cadena del Formato
     * interno
     * @return La cadena que representa el Formato Interno
     */
    public static String prettyPrint(InternalFormat intFormat, int size) {
        VariableByteBuffer printer = new VariableByteBuffer(size);
        printer.add("\n");
        printIntFormat(intFormat, printer, "+");
        return printer.toString();
    }

    private static void printIntFormat(InternalFormat intFormat, VariableByteBuffer printer, String padding) {
        List<InternalField> list = new ArrayList<InternalField>(intFormat.getInternalFieldsAsList());
        printer.add(padding);
        printer.add(">ID=");
        printer.add(intFormat.getId() == null ? "NULL" : "'" + intFormat.getId()+ "'");
        printer.add(", VALUES=\n");
        padding += "---";
        for (InternalField intField : list) {
            if (intField instanceof InternalFormat) {
                printIntFormat((InternalFormat) intField, printer, padding);
            } else {
                printIntField(intField, printer, padding);
                printer.add("\n");
            }
        }
    }

    
    private static void printIntField(InternalField intField, VariableByteBuffer printer, String padding) {
        printer.add(padding);
        printer.add(">ID=");
        printer.add("'" + intField.getId() + "'");
        printer.add(", VALUE=");
        printer.add(intField.getValue() == null ? "NULL" : "[" + intField.getValue() + "]");
    }
}
