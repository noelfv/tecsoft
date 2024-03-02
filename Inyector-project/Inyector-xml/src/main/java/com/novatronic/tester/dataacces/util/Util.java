/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.tester.dataacces.util;

import com.novatronic.tester.domain.Transaction;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 * @author nteruya
 */
public class Util {

    private static final Logger logger = Logger.getLogger(Util.class);
    private static final int BASELENGTH = 255;
    private static final int LOOKUPLENGTH = 16;
    private static byte[] hexNumberTable = new byte[BASELENGTH];
    private static byte[] lookUpHexAlphabet = new byte[LOOKUPLENGTH];

    static {
        for (int i = 0; i < BASELENGTH; i++) {
            hexNumberTable[i] = -1;
        }
        for (int i = '9'; i >= '0'; i--) {
            hexNumberTable[i] = (byte) (i - '0');
        }
        for (int i = 'F'; i >= 'A'; i--) {
            hexNumberTable[i] = (byte) (i - 'A' + 10);
        }
        for (int i = 'f'; i >= 'a'; i--) {
            hexNumberTable[i] = (byte) (i - 'a' + 10);
        }

        for (int i = 0; i < 10; i++) {
            lookUpHexAlphabet[i] = (byte) ('0' + i);
        }
        for (int i = 10; i <= 15; i++) {
            lookUpHexAlphabet[i] = (byte) ('A' + i - 10);
        }
    }

    /**
     * Converts a string to Date
     * @param stDate
     * @return
     */
    public static Date parseDate(String stDate) {
        if (stDate.isEmpty()) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(Constantes.DATE_FORMAT_DD_MM_YY);
        try {
            return sdf.parse(stDate);
        } catch (ParseException ex) {
            logger.error("Error de conversion de String to Date ", ex);
        }
        return null;
    }

    /**
     * Converts a string to long
     * @param stLong
     * @return
     */
    public static Long parseLong(String stLong) {
        return (stLong.isEmpty() ? 0 : Long.parseLong(stLong));
    }

    /**
     * Converts a string to Integer
     * @param stInteger
     * @return
     */
    public static Integer parseInteger(String stInteger) {
        return (stInteger.isEmpty() ? 0 : Integer.parseInt(stInteger));
    }

    /**
     * Converts a Date to string
     * @param date
     * @param format
     * @return
     */
    public static String dateToString(Date date, String format) {
        String strfecha = "";

        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            strfecha = sdf.format(date);
        } catch (Exception ex) {
            logger.error("Error de conversion de Date to String ", ex);
        }

        return strfecha;
    }

    /**
     * Converts a hexa string to string
     * @param hexa
     * @return
     */
    public static String hexaToString(String hexa) {
        String value = new String(stringToBytes(hexa));
        return value;
    }

    /**
     * Converts a hex string to a byte array.
     */
    public static byte[] stringToBytes(String hexEncoded) {
        return decode(hexEncoded.getBytes());
    }

    /**
     *
     * @param binaryData
     * @return
     */
    public static byte[] decode(byte[] binaryData) {
        if (binaryData == null) {
            return null;
        }
        int lengthData = binaryData.length;
        if (lengthData % 2 != 0) {
            return null;
        }

        int lengthDecode = lengthData / 2;
        byte[] decodedData = new byte[lengthDecode];
        for (int i = 0; i < lengthDecode; i++) {
            if (!isHex(binaryData[i * 2]) || !isHex(binaryData[i * 2 + 1])) {
                return null;
            }
            decodedData[i] = (byte) ((hexNumberTable[binaryData[i * 2]] << 4) | hexNumberTable[binaryData[i * 2 + 1]]);
        }
        return decodedData;
    }

    /**
     * byte to be tested if it is Base64 alphabet
     *
     * @param octect
     * @return
     */
    static boolean isHex(byte octect) {
        return (hexNumberTable[octect] != -1);
    }

        /**
     * Método que convierte un dato hexadecimal a string en caso que
     * el atributo binary sea true; caso contrario devuelve el valor original
     * @param element
     * @return El valor de la trama
     */
    public static String frameValue(Element element) {
        String isBinary = element.getAttributeValue(Constantes.ATTRIBUTE_BINARY);
        if (isBinary.equalsIgnoreCase(Constantes.BINARY_TRUE)) {
            return hexaToString(element.getValue());
        } else {
            return element.getValue();
        }
    }


    public static Transaction retrieveTransaction(Element nodeTransaction) {

        Transaction transaction = new Transaction();

        Element element = nodeTransaction.getChild("acquirerIn");
        transaction.setAcquirerIn(frameValue(element));
        element = nodeTransaction.getChild("acquirerOut");
        transaction.setAcquirerOut(frameValue(element));
        element = nodeTransaction.getChild("autorizedIn");
        transaction.setAutorizedIn(element.getValue());
        element = nodeTransaction.getChild("autorizedOut");
        transaction.setAutorizedOut(element.getValue());
        element = nodeTransaction.getChild("dateChange");
        transaction.setDateChange(Util.parseDate(element.getValue()));
        element = nodeTransaction.getChild("dateIn");
        transaction.setDateIn(Util.parseDate(element.getValue()));
        element = nodeTransaction.getChild("id");
        transaction.setId(element.getValue());
        element = nodeTransaction.getChild("keyTnx");
        transaction.setKeyTxn(element.getValue());
        element = nodeTransaction.getChild("userChange");
        transaction.setUserChange(element.getValue());
        element = nodeTransaction.getChild("userIn");
        transaction.setUserIn(element.getValue());

        return transaction;
    }
}
