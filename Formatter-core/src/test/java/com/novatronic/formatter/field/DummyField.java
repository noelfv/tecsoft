/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.field;

import com.novatronic.formatter.internal.InternalFormat;
import com.novatronic.formatter.util.VariableByteBuffer;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 * @author ofernandez
 */
public class DummyField extends Field{
    private static final Logger log  = Logger.getLogger(DummyField.class);
    private int length;
    private String ID;
    
    private class Attr{
        private static final String LENGTH = "length";
    }
    @Override
    protected void readCustomConfiguration(Element element) {
        length = Integer.parseInt(element.getAttributeValue(Attr.LENGTH));
    }

    @Override
    public int putBytes(InternalFormat internalFormat, VariableByteBuffer frame) {
        String value;
        int bytesAdded;
        
        ID = "[id=" + getId() + "]";
        value = internalFormat.getValue(getId());
        log.trace(ID + "valor recibido:" + applyFilter(value));
        value = String.format("%1$-" + length + "s", value);
        bytesAdded = frame.add(value);
        log.trace(ID + "valor agregado:" + applyFilter(value));
        
        return bytesAdded;
    }

    @Override
    public int readBytes(InternalFormat internalFormat, VariableByteBuffer frame, int posicion) {
        byte[] bites;
        String value;
        
        bites = frame.getBytes(posicion, length);
        value = new String(bites);
        log.trace(ID + "Se agrega:" + applyFilter(value));
        internalFormat.add(getId(), value);
        
        return bites.length;
    }

    @Override
    public String toString() {
        return "DummyField{" + "length=" + length + ", ID=" + ID + ", path=" + getPath() + '}';
    }
    
    
}
