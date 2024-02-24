/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.test;

import com.novatronic.formatter.internal.InternalFormat;
import java.util.Map;
import org.apache.log4j.Logger;
import org.junit.*;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Omar
 */
public class CompareInternalTest {
    private static final Logger log = Logger.getLogger(CompareInternalTest.class);
    private static Map<String,InternalFormat> intFormats = null;
    
    public CompareInternalTest() {
        intFormats = IntFormatReader.asMap("format.compare.xml");
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of compareIntFormat method, of class CompareIntFormat.
     */
    @Test
    public void testcompareInternalFormat_Succes_1() {
        log.debug("----------------------- testcompareInternalFormat_Succes_1");
        InternalFormat ifTest = intFormats.get("IF-Test");
        InternalFormat ifExpected = intFormats.get("IF-Comp-1");
        
        CompareInternalResult expResult = new CompareInternalResult(true, CompareInternalResult.NO_FIELD, CompareInternalResult.EXISTS_EQUAL);
        CompareInternalResult result = CompareInternal.compareInternalFormat(ifTest, ifExpected, null);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testcompareInternalFormat_Succes_2() {
        log.debug("----------------------- testcompareInternalFormat_Succes_2");
        InternalFormat ifTest = intFormats.get("IF-Test");
        InternalFormat ifExpected = intFormats.get("IF-Comp-2");
        
        CompareInternalResult expResult = new CompareInternalResult(true, CompareInternalResult.NO_FIELD, CompareInternalResult.EXISTS_EQUAL);
        CompareInternalResult result = CompareInternal.compareInternalFormat(ifTest, ifExpected, null);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testcompareInternalFormat_Fail_1() {
        log.debug("----------------------- testcompareInternalFormat_Fail_1");
        InternalFormat ifTest = intFormats.get("IF-Test");
        InternalFormat ifExpected = intFormats.get("IF-Comp-3");
        
        CompareInternalResult expResult = new CompareInternalResult(false, "04.02", CompareInternalResult.EXISTS_NOT_EQUAL);
        CompareInternalResult result = CompareInternal.compareInternalFormat(ifTest, ifExpected, null);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testcompareInternalFormat_Fail_2() {
        log.debug("----------------------- testcompareInternalFormat_Fail_2");
        InternalFormat ifTest = intFormats.get("IF-Test");
        InternalFormat ifExpected = intFormats.get("IF-Comp-4");
        
        CompareInternalResult expResult = new CompareInternalResult(false, "04.01", CompareInternalResult.TYPE_DIFFERENCES);
        CompareInternalResult result = CompareInternal.compareInternalFormat(ifTest, ifExpected, null);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testcompareInternalFormat_Fail_3() {
        log.debug("----------------------- testcompareInternalFormat_Fail_3");
        InternalFormat ifTest = intFormats.get("IF-Test");
        InternalFormat ifExpected = intFormats.get("IF-Comp-5");
        
        CompareInternalResult expResult = new CompareInternalResult(false, "07.02.02", CompareInternalResult.EXISTS_NOT_EQUAL);
        CompareInternalResult result = CompareInternal.compareInternalFormat(ifTest, ifExpected, null);
        assertEquals(expResult, result);
    }
}
