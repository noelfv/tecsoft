/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.generator.xml;

import com.novatronic.generator.Frame;
import com.novatronic.generator.Transaction;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 * @author ofernandez
 * @version 1.0
 * @since 1.0, 12/11/2010
 */
public class TransactionElement extends Element {
    private static final Logger log = Logger.getLogger(TransactionElement.class);

    private static final String TRANSACTION = "transaction";
    private static final String ACQUIRERIN = "acquirerIn";
    private static final String ACQUIREROUT = "acquirerOut";
    private static final String AUTORIZEDIN = "autorizedIn";
    private static final String AUTORIZEDOUT = "autorizedOut";
    private static final String DATECHANGE = "dateChange";
    private static final String DATEIN = "dateIn";;
    private static final String ID = "id";
    private static final String KEYTNX = "keyTnx";
    private static final String USERCHANGE = "userChange";
    private static final String USERIN = "userIn";

    private static final String USERCHANGE_VALUE = "EFTRPLYD";
    private static final String USERIN_VALUE = "EFTRPLYD";
    private static final String BINARY_ATTR = "binary";

    private static final String TIME = new SimpleDateFormat("dd/MM/yy")
                                            .format(Calendar.getInstance()
                                                .getTime());

    public TransactionElement(Transaction transaction, String id){
        super(TRANSACTION);
        log.debug("Agregando transaccion al xml:" + transaction);
        this.addContent(getFrameElement(transaction.getFrameIn(), ACQUIRERIN));
        this.addContent(getFrameElement(transaction.getFrameOut(), ACQUIREROUT));
        this.addContent(new Element(AUTORIZEDIN));
        this.addContent(new Element(AUTORIZEDOUT));
        this.addContent(new Element(DATECHANGE).setText(TIME));
        this.addContent(new Element(DATEIN).setText(TIME));
        this.addContent(new Element(ID).setText(id));
        this.addContent(new Element(KEYTNX));
        this.addContent(new Element(USERCHANGE).setText(USERCHANGE_VALUE));
        this.addContent(new Element(USERIN).setText(USERIN_VALUE));
        log.debug("Transaccion agregada al xml:" + this);
    }

    private Element getFrameElement(Frame frame, String name){
        Element frameElement = new Element(name);
        frameElement.setAttribute(BINARY_ATTR, String.valueOf(frame.isBinary()));
        String frameValue = frame.generateFrame();
        if(frame.isBinary()){
            frameValue = byteToUpperHex(frameValue.getBytes());
        }
        frameElement.addContent(frameValue);
        return frameElement;
    }
    
    private static String byteToUpperHex(byte[] bytes) {
        StringBuilder strBuffer = new StringBuilder();
        String output = "";
        for (int i = 0; i < bytes.length; i++) {
            output = Integer.toHexString(toInt(bytes[i]));
            output = (output.length() < 2) ? "0"+ output : output;
            strBuffer.append(output.toUpperCase());
        }
        return strBuffer.toString();
    }

    private static int toInt(byte bite) {
        return ((-128 <= bite) && (bite <= -1)) ? 256 + bite : bite;
    }
}
