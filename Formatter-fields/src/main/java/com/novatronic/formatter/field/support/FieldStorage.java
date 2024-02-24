/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.field.support;

/**
 *
 * @author ofernandez
 */
public class FieldStorage {
    public static final int FULL = -1;
    private String id;
    private int maxSize;

    public FieldStorage(String id, int size) {
        this.id = id;
        this.maxSize = size;
    }

    public FieldStorage() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public String toString() {
        return "FieldData{" + "id=" + id + ", size=" + maxSize + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FieldStorage other = (FieldStorage) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        if (this.maxSize != other.maxSize) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 59 * hash + this.maxSize;
        return hash;
    }
}
