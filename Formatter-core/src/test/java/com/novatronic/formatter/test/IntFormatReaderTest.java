/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.test;

import com.novatronic.formatter.internal.InternalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Omar
 */
public class IntFormatReaderTest {
    private static final Logger log = Logger.getLogger(IntFormatReaderTest.class);
    
    public IntFormatReaderTest() {
    }

    /**
     * Test of readTests method, of class Reader.
     */
    @Test
    public void testAsList() {
        log.info("--------------- testAsList");
        String path = "format.test.xml";
        List expResult = expectedAsList();
        List result = IntFormatReader.asList(path);
        
        log.debug("expResult=" + expResult);
        log.debug("result=" + result);
        assertEquals(expResult, result);
        
    }
    
    @Test
    public void testAsMap() {
        log.info("--------------- testAsMap");
        String path = "format.test.xml";
        Map expResult = expectedAsMap();
        Map result = IntFormatReader.asMap(path);
        
        log.debug("expResult=" + expResult);
        log.debug("result=" + result);
        assertEquals(expResult, result);
        
    }
    
    private List<InternalFormat> expectedAsList(){
        List<InternalFormat> tests = new ArrayList<InternalFormat>();
        
        tests.add(intFormat01());
        tests.add(intFormat02());
        
        return tests;
    }
    
    private Map<String, InternalFormat> expectedAsMap(){
        Map<String, InternalFormat> tests = new HashMap<String, InternalFormat>();
        InternalFormat intFormat;
        
        intFormat = intFormat01();
        tests.put("T-01", intFormat);
        intFormat = intFormat02();
        tests.put("T-02", intFormat);
        
        return tests;
    }
    
    private InternalFormat intFormat01(){
        InternalFormat intFormat;
        InternalFormat innerIntFormat;
        
        intFormat = new InternalFormat();
        intFormat.add("01", "aaaa");
        intFormat.add("02", "bbb");
        intFormat.add("03", "cc");
        innerIntFormat = new InternalFormat("04");
        innerIntFormat.add("01", "dd");
        innerIntFormat.add("02", "eeee");
        intFormat.addInternalField(innerIntFormat);
        intFormat.add("05", "f  f  ");
        intFormat.add("06", "  ggg ");
        
        return intFormat;
    }
    
    private InternalFormat intFormat02(){
        InternalFormat intFormat;
        
        intFormat = new InternalFormat();
        intFormat.add("01", "aaaa");
        intFormat.add("02", "bbb");
        intFormat.add("03", "cc");
        
        return intFormat;
    }
}
