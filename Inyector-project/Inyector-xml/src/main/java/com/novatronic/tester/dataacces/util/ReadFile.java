/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.tester.dataacces.util;

import com.novatronic.tester.dataacces.exception.XMLException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;
import org.apache.log4j.Logger;

/**
 * Clase que lee un archivo con una estructura de datos definida por dos valores separados
 * por un delimitador en este caso es la coma (,); este delimitador se puede cambiar.
 * Se utiliza la clase Scanner que sólo es para lectura y no escritura.
 * 
 */
public class ReadFile {
    private final String DELIMITER = ",";
    private final static Logger logger = Logger.getLogger(ReadFile.class);
    private File fFile = null;
    private int lineNumber = 0;


    private static void log(Object aObject) {
        logger.debug(String.valueOf(aObject));
    }

    public ReadFile(String fileName) {
        fFile = new File(fileName);
    }

    /** Template method that calls {@link #processLine(String)}.  */
    public final void processLineByLine()  {
        Scanner scanner = null;
        try {
            scanner = new Scanner(new FileReader(fFile));
            //first use a Scanner to get each line
            while (scanner.hasNextLine()) {
                if (lineNumber > 0) {
                    processLine(scanner.nextLine());
                } else {
                    processHeaderLine(scanner.nextLine());
                }
                lineNumber++;
            }
        } catch(FileNotFoundException ex) {
            logger.error("No se encontró el archivo " + fFile.getName(), ex);
            throw new XMLException("No se encontró el archivo " + fFile.getName(), ex);
        }finally {
            //ensure the underlying stream is always closed
            //this only has any effect if the item passed to the Scanner
            //constructor implements Closeable (which it does in this case).
            log("Total : " + lineNumber + " lines");
            scanner.close();
        }
    }

    protected void processLine(String aLine) {        
        //use a second Scanner to parse the content of each line
        Scanner scanner = new Scanner(aLine);
        scanner.useDelimiter(DELIMITER);
        if (scanner.hasNext()) {
            String longitud = scanner.next();
            String acquirer = scanner.next();
            log("Longitud : " + longitud.trim() + " and Trama is : " + acquirer.trim());
        } else {
            log("Empty or invalid line. Unable to process.");
        }        
    }

    protected void processHeaderLine(String aLine) {
        //use a second Scanner to parse the content of each line
        Scanner scanner = new Scanner(aLine);
        scanner.useDelimiter(DELIMITER);
        if (scanner.hasNext()) {
            String acquirerId = scanner.next();
            String numTransactions = scanner.next();
            log("AcquirerId : " + acquirerId.trim() + " and Transactions : " + numTransactions.trim());
        } else {
            log("Empty or invalid line. Unable to process.");
        }        
    }
}
