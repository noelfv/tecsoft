/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.gui.util;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ofernandez
 */
public class UnParseTest {
    private static final Logger log = Logger.getLogger(UnParseTest.class);
    
    public UnParseTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of unparse method, of class UnParse.
     */
    @Test
    public void testUnparse() {
        log.info("-------------------------------- testUnparse ----------------------------");
        byte[] array = new byte[]{0x20,0x19,0x31,0x32, 0x7E,0x7F, 0x19,0x18};
        String expResult = " $HEX{19}12~$HEX{7F1918}";
        String result = UnParse.unparse(array);
        assertEquals(expResult, result);
    }
}