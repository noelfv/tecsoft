/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.generator;

import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 * @author ofernandez
 * @version 1.0
 * @since 1.0, 12/11/2010
 */
public class Transaction {
    private static final Logger log = Logger.getLogger(Transaction.class);

    private Frame frameIn;
    private Frame frameOut;

    public Transaction(Element configuration){
        log.debug("Creando objeto Transaction...");
        frameIn = new Frame(configuration.getChild(Tag.TRANSACTION_IN));
        frameOut = new Frame(configuration.getChild(Tag.TRANSACTION_OUT));
        log.debug("Objeto Transaction creado");
    }

    @Override
    public String toString() {
        return "Transaction{" + "frameIn=" + frameIn + "frameOut=" + frameOut + '}';
    }

    public Frame getFrameIn() {
        return frameIn;
    }

    public Frame getFrameOut() {
        return frameOut;
    }

    public String getFrameInAsString(){
        return this.frameIn.generateFrame();
    }

    public String getFrameOutAsString(){
        return this.frameOut.generateFrame();
    }

    public interface Tag{
        public static final String TRANSACTION_IN = "transaction-in";
        public static final String TRANSACTION_OUT = "transaction-out";
    }

    public interface Attr{
        public static final String FORMATTER = "formatter=";
    }

    
}
