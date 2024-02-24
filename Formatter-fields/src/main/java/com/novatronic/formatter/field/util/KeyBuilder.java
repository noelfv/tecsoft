/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.field.util;

import com.novatronic.formatter.internal.InternalFormat;
import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public class KeyBuilder {
    private static final Logger log = Logger.getLogger(KeyBuilder.class);
    
    private static final String KEY_SEPARATOR = "|";
    
    public static String generateKeyCase(InternalFormat intFormat, IdItem[] ids){
        log.trace("IntFormat recibido para generar Key: [id=" + intFormat.getId() + ", path=" + intFormat.getPath() + "]");
        String keyCase;
        if(ids.length > 0){
            keyCase = getKeyValue(intFormat, ids[0]);
        } else{
            return null;
        }
        
        for (int i = 1; i < ids.length; i++) {
            keyCase += KEY_SEPARATOR + getKeyValue(intFormat, ids[i]);
        }
        //log.debug("Key generado=" + keyCase);
        return keyCase;
    }
    
    public static String getKeyValue(InternalFormat intFormat, IdItem id){
        if(id.hasNext()){
            return getKeyValue(intFormat.getIFmt(id.getId()),id.getNext());
        }else{
            return intFormat.getValue(id.getId());
        }
    }
}
