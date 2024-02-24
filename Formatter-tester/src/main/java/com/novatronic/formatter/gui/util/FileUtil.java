/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.util;

import com.novatronic.formatter.util.VariableByteBuffer;
import java.io.File;
import java.io.FileOutputStream;
import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public class FileUtil {
    private static final Logger log = Logger.getLogger(FileUtil.class);
    private static final String LS = System.getProperty("line.separator");
    
    public static boolean SaveFrame(String fileName, VariableByteBuffer frame) {
        File fileTrama = new File(fileName);
        try {
            FileOutputStream fos = new FileOutputStream(fileTrama);
            fos.write(("Trama:" + LS + "[").getBytes());
            fos.write(frame.getByteArray());
            fos.write(("]"+ LS).getBytes());
            fos.write(("Trama HEXA:" + LS + "[").getBytes());
            fos.write(ConverterUtil.toHexaString(frame.getByteArray()).getBytes());
            fos.write("]".getBytes());
            fos.flush();
            fos.close();
            log.debug("Trama Guardada en:" + fileName);
            return true;
        } catch (Exception ex) {
            log.error("No fue posible escribir la trama en el archivo=" 
                    + fileName + ", causa:" + ex.getMessage(), ex);
            return false;
        }
    }
}
