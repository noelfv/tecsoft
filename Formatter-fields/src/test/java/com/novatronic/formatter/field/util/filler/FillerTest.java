/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.field.util.filler;

import com.novatronic.formatter.field.util.FieldFormat;
import org.apache.log4j.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Omar
 */
public class FillerTest {
    private static final Logger log = Logger.getLogger(FillerTest.class);
    
    private FieldFormat format;
    private String formatExpected;
    private String formatResult;
    private int length;
    private String value;
    private String result;
    private char charFill;
    private String resultExpected;
    
    public FillerTest() {
    }

    /**
     * Test of setFormat method, of class Filler.
     */
    @Test
    public void testFillerWithoutCharFill() {
        log.info("------------- testFillerWithoutCharFill");
        Filler instance;
        boolean compress;
        
        instance = new Filler();
        format = FieldFormat.NUMBER;
        length = 6;
        compress = false;
        instance.setFormat(format, length, compress);
        
        value = "2345";
        formatExpected = "002345";
        formatResult = instance.fill(value);
        assertEquals(formatExpected, formatResult);
        
        value = "1234567";
        formatExpected = "1234567";
        formatResult = instance.fill(value);
        assertEquals(formatExpected, formatResult);
        
        format = FieldFormat.ALPHA;
        instance.setFormat(format, length, compress);
        value = "123";
        formatExpected = "123   ";
        formatResult = instance.fill(value);
        assertEquals(formatExpected, formatResult);
        
    }

    /**
     * Test of setFormat method, of class Filler.
     */
    @Test
    public void testFillerWithtCharFill() {
        log.info("--------------- testFillerWithtCharFill");
        Filler instance;
        boolean compress;
        
        compress = false;
        instance = new Filler();
        format = FieldFormat.NUMBER;
        length = 6;
        charFill = '0';
        instance.setFormat(format, length, charFill, compress);
        
        value = "2345";
        formatExpected = "002345";
        formatResult = instance.fill(value);
        assertEquals(formatExpected, formatResult);
        
        value = "1234567";
        formatExpected = "1234567";
        formatResult = instance.fill(value);
        assertEquals(formatExpected, formatResult);
        
        format = FieldFormat.ALPHA;
        charFill = ' ';
        instance.setFormat(format, length, compress);
        value = "123";
        formatExpected = "123   ";
        formatResult = instance.fill(value);
        assertEquals(formatExpected, formatResult);
        
        format = FieldFormat.ALPHA;
        charFill = 'F';
        instance.setFormat(format, length,charFill, compress);
        value = "123";
        formatExpected = "123FFF";
        formatResult = instance.fill(value);
        assertEquals(formatExpected, formatResult);
        
        
    }
    
    @Test
    public void testUndoFiller() {
        log.info("--------------- testFillerWithtCharFill");
        Filler instance;
        boolean compress;
        
        compress = false;
        instance = new Filler();
        instance.setFormat(Align.RIGHT, 4, '0', compress);
        //       1234
        value = "00000000500";
        resultExpected = "500";
        result = instance.undoFill(value);
        assertEquals(resultExpected, result);
        
        instance = new Filler();
        instance.setFormat(Align.LEFT, 4, ' ', compress);
        //       012345678
        value = "5002     ";
        resultExpected = "5002";
        result = instance.undoFill(value);
        assertEquals(resultExpected, result);
    }
    
    @Test
    public void testFillerWithoutCharFillAndCompress() {
        log.info("------------- testFillerWithoutCharFillAndCompress");
        Filler instance;
        boolean compress;
        
        instance = new Filler();
        format = FieldFormat.NUMBER;
        length = 6;
        compress = true;
        instance.setFormat(format, length, compress);
        
        value = "2345";
        formatExpected = "002345";
        formatResult = instance.fill(value);
        assertEquals(formatExpected, formatResult);
        
        value = "1234567";
        formatExpected = "1234567";
        formatResult = instance.fill(value);
        assertEquals(formatExpected, formatResult);
        
        format = FieldFormat.ALPHA;
        instance.setFormat(format, length, compress);
        value = "123";
        formatExpected = "123FFF";
        formatResult = instance.fill(value);
        assertEquals(formatExpected, formatResult);
        
    }
    
}
