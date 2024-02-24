/*
 * Copyright (c) 1998-2006 Caucho Technology -- all rights reserved
 *
 * This file is part of Resin(R) Open Source
 *
 * Each copy or derived work must preserve the copyright notice and this
 * notice unmodified.
 *
 * Resin Open Source is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Resin Open Source is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, or any warranty
 * of NON-INFRINGEMENT. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Resin Open Source; if not, write to the
 * Free SoftwareFoundation, Inc.
 * 59 Temple Place, Suite 330
 * Boston, MA 02111-1307 USA
 *
 * @author Scott Ferguson
 */
package com.novatronic.formatter.util;

import java.io.UnsupportedEncodingException;

/**
 * //TODO: Documentar el funcionamiento de cada uno de los metodos.
 * @author ofernandez
 * @version 1.0
 * @since 1.0, 10/11/2010
 */
public class VariableByteBuffer {
    /**
     * El arreglo interno de bytes en el cua se mantiene los datos
     */
    private byte[] internalBuffer;
    
    /**
     * La capacidad maxima del buffer de bytes
     */
    private int capacity;
    
    /**
     * La longitud usada del arreglo de bytes interno
     */
    private int length;

    /**
     * Crea un nuevo objeto ByteBuffer manejando internamente un arreglo de 
     * bytes. Se creara un arreglo de bytes de tamaño minimumCapacity pero cuya
     * capacidad sera igual a mayor a esta.
     * 
     * @param minimumCapacity Es el tamaño con el cual se creará internamente el
     * arreglo de bytes.
     */
    public VariableByteBuffer(int minimumCapacity) {
        capacity = 32;
        if (minimumCapacity > 0x1000) {
            capacity = (minimumCapacity + 0xfff) & ~0xfff;
        } else {
            while (capacity < minimumCapacity) {
                capacity += capacity;
            }
        }

        internalBuffer = new byte[capacity];
        length = 0;
    }

    /**
     * Crea un nuevo objeto ByteBuffer manejando internamente un arreglo de 
     * bytes. Se creara un arreglo de bytes de tamaño 32 y con capacidad igual a
     * 32 tambien.
     */
    public VariableByteBuffer() {
        internalBuffer = new byte[32];
        capacity = internalBuffer.length;
        length = 0;
    }
    
    /**
     * Convierte el arreglo interno de bytes en una cadena de caracteres en 
     * representacion hexadecimal. Se debe tener en cuenta que si bien internamente
     * el buffer puede ser mas amplio, solo se convierte a hexadecimal los bytes
     * usados en el buffer.
     * @return Una cadena que representa los bytes recibidios
     */
    public String toHexaString(){
        String result = "";
        for (int i = 0; i < length; i++) {
            result += toHexaString(internalBuffer[i]);
        }
        return result;
    }
    
    private String toHexaString(int bite){
        return Integer.toString((bite & 0xff) + 0x100, 16)
                            .substring(1).toUpperCase();
    }

    /**
     * Evuelve la capacidad actual del buffer. Es decir cuantos bytes puede
     * mantener en el arreglo antes de ser expandido.
     */
    public int getCapacity() {
        return capacity;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        for (int i = length - 1; i >= 0; i--) {
            hash = 65537 * hash + internalBuffer[i];
        }

        return hash;
    }

    /**
     * Ensure the buffer can hold at least 'minimumCapacity' bytes.
     */
    public void ensureCapacity(int minimumCapacity) {
        if (minimumCapacity <= capacity) {
            return;
        }

        if (minimumCapacity > 0x1000) {
            capacity = (minimumCapacity + 0xfff) & ~0xfff;
        } else {
            while (capacity < minimumCapacity) {
                capacity += capacity;
            }
        }

        byte[] bytes = new byte[capacity];
        System.arraycopy(internalBuffer, 0, bytes, 0, length);
        internalBuffer = bytes;
    }
    
    /**
     * Expande el buffer interno en caso de requerirlo. Este metodo se usa previo
     * a agregar una cantidad de bytes, pues calculara el tamaño actual y se la
     * cantidad de bytes por agregar sobre pasa la capacidad actual, lo expandira.
     * @param bytesLengthToAdd La cantidad de bytes que se agregaran al buffer
     * interno
     */
    private void expandIfNeeded(int bytesLengthToAdd){
        if (length + bytesLengthToAdd > capacity) {
            ensureCapacity(length + bytesLengthToAdd);
        }
    }

    /**
     * Devuelve la longitud actual de uso del buffer. El buffer podria tener una
     * mayor capacidad pero el uso de este esta dado por Length.
     */
    public int getLength() {
        return length;
    }

    /**
     * signa una nueva longitud de uso del buffer. En caso este valor sea mayor
     * a la capacidad actual del buffer, este se expandera hasta lograr el
     * tamaño que pueda mantener esta capacidad.
     */
    public void setLength(int len) {
        if (len < 0) {
            throw new IllegalArgumentException("La longitud no puede ser < 0");
        } else if (len > capacity) {
            ensureCapacity(len);
        }
        length = len;
    }

    /**
     * Coloca la longitud de uso del buffer a CERO. Es decir, se puede usar el
     * buffer desde la posicion inicial.
     */
    public void clear() {
        length = 0;
    }

    /**
     * Retorna el arreglo interno de bytes. Se debe tener especial cuidado con
     * este metodo debido aque al obtener el arreglo interno, este podria tener
     * un mayor tamaño en comparacion de los bytes realmente usados del buffer.
     * Es preferible obtener los bytes usados a partir del metodo 
     * {@link #getByteArray() getByteArray()}
     */
    public byte[] getBuffer() {
        return internalBuffer;
    }

    /**
     * Add a byte to the buffer.
     */
    public void append(int b) {
        expandIfNeeded(1);
        internalBuffer[length++] = (byte) b;
    }

    /**
     * Agrega un arreglo al buffer interno desde una posicion dada del buffer 
     * recibido como parametro. Del buffer pasado como parametro se extraera una
     * cantidad dada como parametro y desde una posicion tambien indicada.
     * @param buffer El buffer desde donde se obtendra el arreglo de bytes a
     * insertar
     * @param offset Posicion en el buffer recibido desde donde se empezara a
     * obtener los bytes por agregar
     * @param length La cantidad de bytes a obtener del buffer recibidio como 
     * parametro.
     * @return La cantidad de bytes agregados al buffer interno
     */
    public int add(byte[] buffer, int offset, int length) {
        expandIfNeeded(length);

        System.arraycopy(buffer, offset, this.internalBuffer, this.length, length);
        this.length += length;
        
        return length;
    }
    
    /**
     * Agrega un arreglo completo de bytes al buffer interno.
     * @param buffer El buffer desde donde se obtendra el arreglo de bytes a
     * insertar
     * @return La cantidad de bytes agregados al buffer interno
     */
    public int add(byte[] buffer) {
        return add(buffer, 0, buffer.length);
    }

    /**
     * Inserts a byte array
     */
    public void add(int i, int data) {
        expandIfNeeded(1);

        System.arraycopy(internalBuffer, i, internalBuffer, i + 1, length - i);
        internalBuffer[i] = (byte) data;

        length += 1;
    }

    public void add(int data) {
        expandIfNeeded(1);

        internalBuffer[length++] = (byte) data;
    }

    public void set(int i, byte[] buffer, int offset, int length) {
        System.arraycopy(buffer, offset, this.internalBuffer, i, length);
    }

    public void set(int i, int data) {
        internalBuffer[i] = (byte) data;
    }

    /**
     * Inserta una arreglo de bytes en este objeto en la posicion dada por <i>"offset"</i>.
     * El arreglo de bytes recibido dado por <i>"buffer"</i>, se obtendra un subconjunto
     * de bytes cuya posicion inicial esta dado por <i>"offset"</i> y cuya longitud esta 
     * dado por <i>"bufferLength"</i>
     * @param offset La posicion en el objeto actual donde se insertara el arreglo recibido
     * @param buffer El arreglo de bytes desde se obtendran los bytes a insertar
     * @param bufferOffset La posicion inicial del arreglo recibido a partir del cual se
     * obtendra el arreglo de bytes.
     * @param bufferLength La cnatidad de bytes a obtener del buffer recibido.
     */
    public void insert(int offset, byte[] buffer, int bufferOffset, int bufferLength) {
        expandIfNeeded(bufferLength);

        System.arraycopy(this.internalBuffer, offset, this.internalBuffer, offset + bufferLength, this.length - offset);
        System.arraycopy(buffer, bufferOffset, this.internalBuffer, offset, bufferLength);

        this.length += bufferLength;
    }

    /**
     * Replace an array of bytes in this object for other array of vytes that you
     * give it as parameter: buffer.
     * @param start starting position in this object when do you will start to replace.
     * @param buffer the source array
     * @param bufferOffset starting position in the source array
     * @param sourceLength the number of array elements to be used for replacement
     * or number of bytes that you will replace.
     */
    public void replace(int start, byte[] buffer, int bufferOffset, int sourceLength) {
        System.arraycopy(buffer, bufferOffset, this.internalBuffer, start, sourceLength);
    }
    
    /**
     * Replace all bytes that you give it as parameter starting from "start"
     * @param start starting position in this object when do you will start to replace.
     * @param buffer the source array that you use for replacement
     */
    public void replace(int start, byte[] buffer) {
        System.arraycopy(buffer, 0, this.internalBuffer, start, buffer.length);
    }

    /**
     * Inserts a byte array
     */
    public void append(byte[] buffer, int offset, int length) {
        expandIfNeeded(length);

        System.arraycopy(buffer, offset, this.internalBuffer, this.length, length);

        this.length += length;
    }

    public void addByte(int v) {
        add(v);
    }

    /**
     * Inserts a short into the buffer
     */
    public void replaceShort(int i, int s) {
        internalBuffer[i] = (byte) (s >> 8);
        internalBuffer[i + 1] = (byte) (s);
    }

    /**
     * Appends a short (little endian) in the buffer
     */
    public void appendShort(int s) {
        expandIfNeeded(2);

        replaceShort(length, s);

        length += 2;
    }

    public void addShort(int s) {
        expandIfNeeded(2);

        internalBuffer[length++] = (byte) (s >> 8);
        internalBuffer[length++] = (byte) s;
    }

    public void addShort(int i, int s) {
        add(i, (byte) (s >> 8));
        add(i + 1, (byte) (s));
    }

    public void setShort(int i, int s) {
        internalBuffer[i] = (byte) (s >> 8);
        internalBuffer[i + 1] = (byte) (s);
    }

    /**
     * Inserts a int (little endian) into the buffer
     */
    public void replaceInt(int i, int v) {
        internalBuffer[i] = (byte) (v >> 24);
        internalBuffer[i + 1] = (byte) (v >> 16);
        internalBuffer[i + 2] = (byte) (v >> 8);
        internalBuffer[i + 3] = (byte) (v);
    }

    /**
     * Appends an int (little endian) in the buffer
     */
    public void appendInt(int s) {
        expandIfNeeded(4);

        internalBuffer[length++] = (byte) (s >> 24);
        internalBuffer[length++] = (byte) (s >> 16);
        internalBuffer[length++] = (byte) (s >> 8);
        internalBuffer[length++] = (byte) s;
    }

    public void addInt(int s) {
        expandIfNeeded(4);

        internalBuffer[length++] = (byte) (s >> 24);
        internalBuffer[length++] = (byte) (s >> 16);
        internalBuffer[length++] = (byte) (s >> 8);
        internalBuffer[length++] = (byte) s;
    }

    public void addInt(int i, int s) {
        add(i + 0, (byte) (s >> 24));
        add(i + 1, (byte) (s >> 16));
        add(i + 2, (byte) (s >> 8));
        add(i + 3, (byte) (s));
    }

    public void setInt(int i, int v) {
        internalBuffer[i] = (byte) (v >> 24);
        internalBuffer[i + 1] = (byte) (v >> 16);
        internalBuffer[i + 2] = (byte) (v >> 8);
        internalBuffer[i + 3] = (byte) (v);
    }

    public void addLong(long v) {
        expandIfNeeded(8);

        internalBuffer[length++] = (byte) (v >> 56L);
        internalBuffer[length++] = (byte) (v >> 48L);
        internalBuffer[length++] = (byte) (v >> 40L);
        internalBuffer[length++] = (byte) (v >> 32L);

        internalBuffer[length++] = (byte) (v >> 24L);
        internalBuffer[length++] = (byte) (v >> 16L);
        internalBuffer[length++] = (byte) (v >> 8L);
        internalBuffer[length++] = (byte) v;
    }

    public void addFloat(float v) {
        expandIfNeeded(4);

        int bits = Float.floatToIntBits(v);

        internalBuffer[length++] = (byte) (bits >> 24);
        internalBuffer[length++] = (byte) (bits >> 16);
        internalBuffer[length++] = (byte) (bits >> 8);
        internalBuffer[length++] = (byte) bits;
    }

    public void addDouble(double v) {
        expandIfNeeded(8);

        long bits = Double.doubleToLongBits(v);

        internalBuffer[length++] = (byte) (bits >> 56);
        internalBuffer[length++] = (byte) (bits >> 48);
        internalBuffer[length++] = (byte) (bits >> 40);
        internalBuffer[length++] = (byte) (bits >> 32);
        internalBuffer[length++] = (byte) (bits >> 24);
        internalBuffer[length++] = (byte) (bits >> 16);
        internalBuffer[length++] = (byte) (bits >> 8);
        internalBuffer[length++] = (byte) bits;
    }

    public void addString(String s) {
        int len = s.length();
        expandIfNeeded(len);

        for (int i = 0; i < len; i++) {
            internalBuffer[length++] = (byte) s.charAt(i);
        }
    }

    /**
     * Adds a string with a specified encoding.
     */
    public void addString(String s, String encoding) throws UnsupportedEncodingException {
        if (encoding == null || encoding.equals("ISO-8859-1")) {
            addString(s);
            return;
        }

        // XXX: special case for utf-8?

        byte[] bytes = null;

        bytes = s.getBytes(encoding);

        int len = bytes.length;
        expandIfNeeded(len);

        for (int i = 0; i < len; i++) {
            internalBuffer[length++] = bytes[i];
        }
    }

    /**
     * Agrega una cadena al buffer interno. Previamente extraera los caracteres
     * para luego ir agregandolos uno a uno al buffer interno. En caso de pasarle
     * una cadena null, no agregara nada.
     * @param s La cadena por agregar
     * @return La cantidad de bytes agregados al buffer interno.
     */
    public int add(String s) {
        int len;
        if(s == null){
            return 0;
        }
        len = s.length();
        expandIfNeeded(len);
        for (int i = 0; i < len; i++) {
            internalBuffer[length++] = (byte) s.charAt(i);
        }
        return len;
    }

    /**
     * Agrega una lso carateres pasados por parametro y segun la posicion y
     * cantidad indicada
     * @param chars El arreglo de caracteres por agregar
     * @param offset Posicion en el arreglo desde donde se empezara a obtener
     * los caracteres
     * @param len La cantidad de caracteres a obtener del arreglo.
     * @throws IndexOutOfBoundsException Si el offset no corresponde a una
     * posicion valida en el arreglo o la longitud por obtener sobrepaso el
     * tamaño del arreglo
     */
    public void add(char[] chars, int offset, int len) {
        expandIfNeeded(len);

        for (int i = 0; i < len; i++) {
            internalBuffer[length++] = (byte) chars[offset + i];
        }
    }

    /**
     * Retira una cantidad de bytes del buffer interno. Com producto de esto, la
     * longitud usada del arreglo se reduce en la cantidad retirada.
     * @param begin Posicion desde donde se emepzara a retirar los bytes
     * @param length La cantidad de bytes por retirar
     * @throws IndexOutOfBoundsException Si la posicion inicial sobrepasa el
     * tamaño del buffer interno o si la longitud a retirar hace lo mismo. 
     */
    public void remove(int begin, int length) {
        System.arraycopy(internalBuffer, begin + length, internalBuffer, begin,
                capacity - length - begin);

        this.length -= length;
    }

    /**
     * Appends an int (little endian) in the buffer
     */
    public void append(String string) {
        for (int i = 0; i < string.length(); i++) {
            append(string.charAt(i));
        }
    }

    /**
     * Returns the byte at the specified offset.
     */
    public byte byteAt(int i) {
        if (i < 0 || i > length) {
            throw new IllegalArgumentException("La posicion no existe");
        }

        return internalBuffer[i];
    }

    /**
     * Returns the byte at the specified offset.
     */
    public void setByteAt(int i, int b) {
        internalBuffer[i] = (byte) b;
    }

    public byte get(int i) {
        if (i < 0 || i >= length) {
            throw new IndexOutOfBoundsException("out of bounds: " + i + " len: " + length);
        }

        return internalBuffer[i];
    }

    /**
     * 
     * @param i
     * @return 
     */
    public short getShort(int i) {
        if (i < 0 || i + 1 >= length) {
            throw new IndexOutOfBoundsException("out of bounds: " + i + " len: " + length);
        }

        return (short) (((internalBuffer[i] & 0xff) << 8)
                + (internalBuffer[i + 1] & 0xff));
    }

    /**
     * Obtiene un entero del arreglo. El entero se considera de una longitud de 4 bytes
     * @param i la posicion en arreglo interno desde donde se recupera el entero.
     * @return El entero leido
     * @throws IndexOutOfBoundsException Si la posicion a leer es negativa o si no es
     * posible leer los 4 bytes consecutibos necesarios. Esto sucede cuando la posicion 
     * esta muy cercano al final del arreglo interno
     */
    public int getInt(int i) {
        if (i < 0 || i + 3 >= length) {
            throw new IndexOutOfBoundsException("out of bounds: " + i + " len: " + length);
        }

        return (((internalBuffer[i + 0] & 0xff) << 24)
                + ((internalBuffer[i + 1] & 0xff) << 16)
                + ((internalBuffer[i + 2] & 0xff) << 8)
                + ((internalBuffer[i + 3] & 0xff)));
    }
    
    /**
     * Obtiene el byte ubicado en la poscion recibida como parametro.
     * @param i la posicion desde donde se obtendra el byte
     * @return EL byte obtenido de la posicion solicitada
     * @throws IndexOutOfBoundsException Si la posicion es negativa o si esta posicion 
     * esta fuera del rango del arreglo de bytes interno.
     */
    public byte getByte(int i) {
        if (i < 0 || i >= length) {
            throw new IndexOutOfBoundsException("out of bounds: " + i + " len: " + length);
        }
        
        return internalBuffer[i];
    }

    /**
     * 
     * @param offset
     * @param lengthRead
     * @return
     * @throws IndexOutOfBoundsException
     */
    public byte[] getBytes(int offset, int lengthRead){
        if (offset < 0 || offset + lengthRead > length) {
            throw new IndexOutOfBoundsException("out of bounds[ offset= " + offset
                    + ",lengthRead=" + lengthRead + "], len: " + length);
        }

        byte[] array = new byte[lengthRead];
        System.arraycopy(internalBuffer, offset, array, 0, lengthRead);
        return array;
    }

    public void print(int i) {
        expandIfNeeded(16);

        if (i < 0) {
            internalBuffer[length++] = (byte) '-';
            i = -i;
        } else if (i == 0) {
            internalBuffer[length++] = (byte) '0';
            return;
        }

        int start = length;
        while (i > 0) {
            internalBuffer[length++] = (byte) ((i % 10) + '0');
            i /= 10;
        }

        for (int j = (length - start) / 2; j > 0; j--) {
            byte temp = internalBuffer[length - j];
            internalBuffer[length - j] = internalBuffer[start + j - 1];
            internalBuffer[start + j - 1] = temp;
        }
    }

    /**
     * Devuelve una copia de los bytes usados del buffer interno. Es decir, se
     * devuelve los bytes desde la opsicion cero "0" hasta la posicion marcada
     * por {@link #length length}
     */
    public byte[] getByteArray() {
        byte[] bytes = new byte[length];

        System.arraycopy(internalBuffer, 0, bytes, 0, length);

        return bytes;
    }

    /**
     * Clones the buffer
     */
    @Override
    public Object clone() {
        VariableByteBuffer newBuffer = new VariableByteBuffer(length);

        System.arraycopy(internalBuffer, 0, newBuffer.internalBuffer, 0, length);

        return newBuffer;
    }

    @Override
    public boolean equals(Object b) {
        if (!(b instanceof VariableByteBuffer)) {
            return false;
        }

        final VariableByteBuffer bb = (VariableByteBuffer) b;
        if (bb.length != length) {
            return false;
        }

        for (int i = length - 1; i >= 0; i--) {
            if (bb.internalBuffer[i] != internalBuffer[i]) {
                return false;
            }
        }

        return true;
    }

    /**
     * String representation of the buffer.
     */
    @Override
    public String toString() {
        return new String(internalBuffer, 0, length);
    }

    public String toString(String encoding) {
        try {
            return new String(internalBuffer, 0, length, encoding);
        } catch (Exception e) {
            return new String(internalBuffer, 0, length);
        }
    }
}
