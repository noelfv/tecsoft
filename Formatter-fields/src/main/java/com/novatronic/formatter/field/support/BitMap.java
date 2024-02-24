/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.field.support;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ofernandez
 */
public class BitMap {

    public static final int NIBBLE_LENGTH = 4;
    public static final int BYTE_LENGTH = 8;
    private BitSet bitmap;
    private static final int BITMAP_LENGTH = 64;
    private static final byte[] MSK_BYTE = {(byte) 0X80, (byte) 0X40, (byte) 0X20, (byte) 0X10,
        (byte) 0X08, (byte) 0X04, (byte) 0X02, (byte) 0X01};
    private static final Map<Character, Boolean[]> MAP_CHAR;

    static {
        MAP_CHAR = new HashMap<Character, Boolean[]>();
        MAP_CHAR.put('0', new Boolean[]{false, false, false, false});
        MAP_CHAR.put('1', new Boolean[]{false, false, false, true});
        MAP_CHAR.put('2', new Boolean[]{false, false, true, false});
        MAP_CHAR.put('3', new Boolean[]{false, false, true, true});
        MAP_CHAR.put('4', new Boolean[]{false, true, false, false});
        MAP_CHAR.put('5', new Boolean[]{false, true, false, true});
        MAP_CHAR.put('6', new Boolean[]{false, true, true, false});
        MAP_CHAR.put('7', new Boolean[]{false, true, true, true});
        MAP_CHAR.put('8', new Boolean[]{true, false, false, false});
        MAP_CHAR.put('9', new Boolean[]{true, false, false, true});
        MAP_CHAR.put('A', new Boolean[]{true, false, true, false});
        MAP_CHAR.put('B', new Boolean[]{true, false, true, true});
        MAP_CHAR.put('C', new Boolean[]{true, true, false, false});
        MAP_CHAR.put('D', new Boolean[]{true, true, false, true});
        MAP_CHAR.put('E', new Boolean[]{true, true, true, false});
        MAP_CHAR.put('F', new Boolean[]{true, true, true, true});
    }

    public BitMap() {
        bitmap = new BitSet(BITMAP_LENGTH);
    }

    public BitMap(int size) {
        bitmap = new BitSet(size);
    }

    /**
     * Coloca a TRUE una posicion especifica del mapa de bits
     *
     * @param index La posicion a colocar a TRUE
     */
    public void set(int index) {
        bitmap.set(index);
    }

    /**
     * Devuelve el valor de una posicion dada del Bitmap
     *
     * @param index La posicion a obtener
     * @return TRUE si e bit esta prendido, FALSE en caso contrario
     */
    public boolean get(int index) {
        return bitmap.get(index);
    }

    /**
     * Devuelve como BYTE (8 valores del bitmap) a partir de una posicion inicial. Si la
     * posicion sobrepasa el contenido actual del bitmap, se asumira que lo bits estas
     * apagados
     *
     * @param initialPosition La posicion desde donde se creara el BYTE
     * @return Un entero con sus bits correspondientes al bitmap a partir de la posicion
     * solicitada
     */
    public int getByte(int initialPosition) {
        int bite;

        bite = 0;
        for (int i = 0; i < BYTE_LENGTH; i++) {
            if (bitmap.get(initialPosition + i)) {
                bite += 1 << (BYTE_LENGTH - i - 1);
            }
        }

        return bite;
    }

    /**
     * Devuelve como NIBBLE (4 valores del bitmap) a partir de una posicion inicial. Si la
     * posicion sobrepasa el contenido actual del bitmap, se asumira que lo bits estas
     * apagados.
     * @param initialPosition La posicion desde donde se creara el NIBBLE
     * @return Un entero con sus bits correspondientes al bitmap a partir de la posicion
     * solicitada
     */
    public int getNibble(int initialPosition) {
        byte bite;

        bite = 0;
        for (int i = 0; i < NIBBLE_LENGTH; i++) {
            if (bitmap.get(initialPosition + i)) {
                bite += 1 << (NIBBLE_LENGTH - i - 1);
            }
        }

        return bite;
    }

    /**
     * Asigna los bits del BYTE recibido como parametro al bitmap a partir de una posicion
     * inicial. Esta posicion puede ser mayor a la longitud del bitmap en cuyo caso se
     * expandira dinamicamente.
     * @param bite El BYTE desde el cual se obtendran los bits a usar como actualizacion
     * del bitmap.
     * @param position La posicion a partir de la cual se actualizar&aacute; los bits del
     * bitmap
     * @throws IllegalArgumentException Si la posicion indicada es negativa
     */
    public void populateFrom(byte bite, int position) {
        if (position < 0) {
            throw new IllegalArgumentException("La posicion no puede ser negativa:" + position);
        }
        for (int i = 0; i < BYTE_LENGTH; i++) {
            bitmap.set(i + position, ((bite & MSK_BYTE[i]) == MSK_BYTE[i]));
        }
    }

    /**
     * Asigna los bits de la representacion HEXADECIMAL del caracter recibido como
     * parametro a partir de una posicion inicial. Esta posicion puede ser mayor a la
     * longitud del bitmap en cuyo caso se expandira dinamiecamente.
     *
     * @param hexaChar El BYTE como caracter HEXADECIMAL desde el cual se obtendran los
     * bits a usar como actualizacion del bitmap. Son validos: [A_Fa_f]
     * @param position La posicion a partir de la cual se se actualizara los bits del
     * bitmap
     * @throws IllegalArgumentException Si la posicion indicada es negativa o el char 
     * recibido no es un valor hexadecimal valido
     */
    public void populateFrom(char hexaChar, int position) {
        Boolean[] flags;
        char hexCharUpper = Character.toUpperCase(hexaChar);

        flags = MAP_CHAR.get(hexCharUpper);
        if (position < 0 || flags == null) {
            throw new IllegalArgumentException("La posicion no puede ser negativa:" 
                    + position + ", o el char debe ser hexadecimal:" + hexaChar);
        }
        
        for (int i = 0; i < NIBBLE_LENGTH; i++) {
            bitmap.set(position + i, flags[i]);
        }
    }

    /**
     * {@inheritDoc  }
     */
    @Override
    public String toString() {
        StringBuilder map = new StringBuilder(128);

        map.append('{');
        int i = bitmap.nextSetBit(0);
        if (i != -1) {
            map.append(i + 1);
            for (i = bitmap.nextSetBit(i + 1); i >= 0; i = bitmap.nextSetBit(i + 1)) {
                int endOfRun = bitmap.nextClearBit(i);
                do {
                    map.append(", ").append(i + 1);
                } while (++i < endOfRun);
            }
        }
        map.append('}');

        return map.toString();
    }
}
