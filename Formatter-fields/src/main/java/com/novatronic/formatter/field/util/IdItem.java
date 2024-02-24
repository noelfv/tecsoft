/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.field.util;

/**
 *
 * @author Omar
 */
public class IdItem {
    private String id;
    private IdItem next;
    
    public IdItem(String id){
        this.id = id;
    }
    
    public IdItem(String id, IdItem next){
        this.id = id;
        this.next = next;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public IdItem getNext() {
        return next;
    }

    public void setNext(IdItem next) {
        this.next = next;
    }
    
    public boolean hasNext(){
        return next != null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IdItem other = (IdItem) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        if (this.next != other.next && (this.next == null || !this.next.equals(other.next))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 23 * hash + (this.next != null ? this.next.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "IdItem{" + "id=" + id + ", next=" + next + '}';
    }
}
