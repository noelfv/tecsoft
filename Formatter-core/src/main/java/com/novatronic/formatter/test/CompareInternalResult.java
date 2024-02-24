/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.test;

/**
 *
 * @author Omar
 */
public class CompareInternalResult {
    private boolean success;
    private String fieldID;
    private String reason;
    
    public static final String NOT_EXISTS = "NOT_EXISTS";
    public static final String EXISTS_NOT_EQUAL = "EXISTS_NOT_EQUAL";
    public static final String EXISTS_EQUAL = "EXISTS_EQUAL";
    public static final String TYPE_DIFFERENCES = "TYPE_DIFFERENCES";
    public static final String MISSING_INTERNALFORMAT = "MISSING_INTERNALFORMAT";
    public static final String NO_FIELD = "NO_FIELD";

    public CompareInternalResult() {
        this.fieldID = NO_FIELD;
    }

    public CompareInternalResult(boolean result, String failFieldId) {
        this.success = result;
        this.fieldID = failFieldId;
    }

    public CompareInternalResult(boolean success, String fieldID, String reason) {
        this.success = success;
        this.fieldID = fieldID;
        this.reason = reason;
    }

    /**
     * Devuelve una cadena con el detalle de la "razon", en caso no la comparacion no fue 
     * exitosa.
     * @return Una cadena indicando la "razon" como resultado de la comparacion.
     */
    public String getReason() {
        return reason;
    }

    /**
     * 
     * @param reason 
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * Devuelve el identificador del campo en el cual se obtuvo una diferencia al realizar
     * al realizar la comparacion.
     * @return El identificador del campo donde se produjo una diferencia.
     */
    public String getFieldID() {
        return fieldID;
    }

    /**
     * 
     * @param fieldID 
     */
    public void setFieldID(String fieldID) {
        this.fieldID = fieldID;
    }

    /**
     * Indica si la prueba fue exitosa o no.
     * @return TRUE en caso de exito. FALSE en caso contrario.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 
     * @param success 
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * {@inheritDoc  }
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CompareInternalResult other = (CompareInternalResult) obj;
        if (this.success != other.success) {
            return false;
        }
        if ((this.fieldID == null) ? (other.fieldID != null) : !this.fieldID.equals(other.fieldID)) {
            return false;
        }
        if ((this.reason == null) ? (other.reason != null) : !this.reason.equals(other.reason)) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc  }
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.success ? 1 : 0);
        hash = 37 * hash + (this.fieldID != null ? this.fieldID.hashCode() : 0);
        hash = 37 * hash + (this.reason != null ? this.reason.hashCode() : 0);
        return hash;
    }

    /**
     * {@inheritDoc  }
     */
    @Override
    public String toString() {
        return "CompareInternalResult{" + "success=" + success 
                + ", fieldID=" + fieldID 
                + ", reason=" + reason + '}';
    }
    
}
