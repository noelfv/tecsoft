/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.test;

/**
 *
 * @author Omar
 */
public class CompareFrameResult {

    private boolean success;
    private String reason;
    private int position;
    private int testedSize;
    private int expectedSize;
    public static final String EXPECTED_INCLUDE_IN_TESTED = "EXPECTED_INCLUDE_IN_TESTED";
    public static final String TESTED_INCLUDE_IN_EXPECTED = "TESTED_INCLUDE_IN_EXPECTED";
    public static final String EQUALS = "EQUALS";
    public static final String DIFFERENTS_PART = "DIFFERENTS_PARTS";
    public static final String FRAME_MISSING = "FRAME_MISSING";

    public CompareFrameResult() {
    }

    public CompareFrameResult(boolean success, String reason, int position, int testedSize, int expectedSize) {
        this.success = success;
        this.reason = reason;
        this.position = position;
        this.testedSize = testedSize;
        this.expectedSize = expectedSize;
    }

    /**
     * Devuelve la longitud esperada de la trama comparada
     * @return La longitud esperada
     */
    public int getExpectedSize() {
        return expectedSize;
    }

    /**
     * 
     * @param expectedSize 
     */
    public void setExpectedSize(int expectedSize) {
        this.expectedSize = expectedSize;
    }

    /**
     * Devuelve la longitud de la trama la cual fue comparada.
     * @return La longitud de la trama comparada.
     */
    public int getTestedSize() {
        return testedSize;
    }

    /**
     * 
     * @param testedSize 
     */
    public void setTestedSize(int testedSize) {
        this.testedSize = testedSize;
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
     * Devuelve una cadena que indica la razon la cual tiene un detalle adicional en caso
     * no se haya superado la prueba.
     * @return 
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
     * Devuelve la posicion en la cual se detuvo la comparacion, pudiendo ser el total
     * en caso la compracion fue exitosa
     * @return La posicion final priducto de la comparacion
     */
    public int getPosition() {
        return position;
    }

    /**
     * 
     * @param position 
     */
    public void setPosition(int position) {
        this.position = position;
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
        final CompareFrameResult other = (CompareFrameResult) obj;
        if (this.success != other.success) {
            return false;
        }
        if ((this.reason == null) ? (other.reason != null) : !this.reason.equals(other.reason)) {
            return false;
        }
        if (this.position != other.position) {
            return false;
        }
        if (this.testedSize != other.testedSize) {
            return false;
        }
        if (this.expectedSize != other.expectedSize) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc  }
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.success ? 1 : 0);
        hash = 97 * hash + (this.reason != null ? this.reason.hashCode() : 0);
        hash = 97 * hash + this.position;
        hash = 97 * hash + this.testedSize;
        hash = 97 * hash + this.expectedSize;
        return hash;
    }

    /**
     * {@inheritDoc  }
     */
    @Override
    public String toString() {
        return "CompareFrameResult{" + "success=" + success 
                + ", reason=" + reason 
                + ", position=" + position 
                + ", testedSize=" + testedSize 
                + ", expectedSize=" + expectedSize + '}';
    }
}
