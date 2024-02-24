/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.formatter.internal;

/**
 *
 * @author ofernandez
 * @version 1.0
 * @since 1.0, 08/11/2010
 */
public class InternalField {
    /**
     * Identificador del campo el cual esta asociado a la configuracion.
     */
    private String id;

    /**
     * Valor del campo
     */
    private String value;

    /**
     *
     */
    public InternalField(){

    }

    /**
     * Se crea un objeto del tipo InternalField con un identificador igual a
     * {@code id} y un valor asociado igual a {@code value}.
     * @param id
     * @param value
     * @see #value
     */
    public InternalField(String id, String value){
        this.id = id;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "InternalField{" + "id=" + id + ", value=[" + value + "]}";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final InternalField other = (InternalField) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        if ((this.value == null) ? (other.value != null) : !this.value.equals(other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 83 * hash + (this.value != null ? this.value.hashCode() : 0);
        return hash;
    }
}
