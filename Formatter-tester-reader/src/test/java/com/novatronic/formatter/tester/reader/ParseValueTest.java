/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.tester.reader;

import com.novatronic.formatter.util.VariableByteBuffer;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Omar
 */
public class ParseValueTest {
    private static final Logger log = Logger.getLogger(ParseValueTest.class);
    
    public ParseValueTest() {
    }

    /**
     * Test of parse method, of class ParseValue.
     */
    @Test
    public void testParse() {
        log.info("-------------- testParse");
        String value;
        VariableByteBuffer expResult;
        VariableByteBuffer result = new VariableByteBuffer();
        
        //                 1         2         3         4
        //       01234567890123456789012345678901234567890123456789
        value = "hola a todos $HEX{6578}, como estan $HEX{686F79}";
        expResult = new VariableByteBuffer();
        expResult.add("hola a todos ex, como estan hoy");
        result = ParseValue.parse(value);
        log.debug("result=" + result);
        assertEquals(expResult, result);
        
        value = "$HEX{0200703C058028E89A5806005002}";
        expResult = new VariableByteBuffer();
        expResult.add(new byte[]{(byte)0x02,0x00,(byte)0x70,(byte)0x3C,0x05,(byte)0x80,(byte)0x28,(byte)0xE8,(byte)0x9A,(byte)0x58,0x06,0x00,0x50,0x02});
        result = ParseValue.parse(value);
        log.debug("result=" + result);
        assertEquals(expResult, result);
    }
}
