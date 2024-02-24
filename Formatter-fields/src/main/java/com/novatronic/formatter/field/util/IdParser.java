/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.field.util;

import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public class IdParser {

    public static final Logger log = Logger.getLogger(IdParser.class);
    
    private static final String KEY_TOKEN = ",";
    private static final String SUBKEY_TOKEN = "\\.";
    
    /**
     * Este metodo crea un estructura enlazada de claves las cuales seran usadas
     * para posteriormente buscar entre los valores guardados en el internalFormat
     * aun si se tiene sub-claves. Se espera un formato como el siguiente:
     * <br><br>
     * claves = "01.mo.a,02,05.b"
     * <br><br>
     * Donde las id's a buscar estan separados por comas sin contener espacios
     * entre si. Para un sub-id se usara el separador ".", el cual indica que
     * la clave a buscar se encuentra en un elemento interno y asi sucesivamente.
     * El ejemplo indica:<br>
     * El primer id se encuentra dentro de "01", luego dentro de "mo" y por ultimo
     * dentro de "a", que es el id donde finalmente se encuentra el valor a usar.
     * En total se estan indicando 3 id's.
     * @param claves Una cadena según el formato descrito.
     * @return Un arreglo donde cada elemento forma una lista enlazada
     */
    public static IdItem[] parseIds(String claves){
        String[] clavesArray = claves.split(KEY_TOKEN);

        IdItem[] ids = new IdItem[clavesArray.length];
        createIds(clavesArray, ids);
        
        return ids;
    }

    private static void createIds(String[] clavesArray, IdItem[] ids) {
        String[] subClaves;
        IdItem idRoot;
        IdItem realIdRoot;

        for (int i = 0; i < clavesArray.length; i++) {
            subClaves = clavesArray[i].split(SUBKEY_TOKEN);
            idRoot = new IdItem(subClaves[0]);
            realIdRoot = idRoot;
            idRoot = linkSubIds(subClaves, idRoot);
            ids[i] = realIdRoot;
        }
    }

    private static IdItem linkSubIds(String[] subClaves, IdItem itemRoot) {
        IdItem itemSon;

        for (int j = 1; j < subClaves.length; j++) {
            itemSon = new IdItem(subClaves[j]);
            itemRoot.setNext(itemSon);
            itemRoot = itemSon;
        }
        return itemRoot;
    }
}
