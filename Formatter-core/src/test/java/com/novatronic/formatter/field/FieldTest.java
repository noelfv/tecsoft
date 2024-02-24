/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.field;

import com.novatronic.formatter.Formatter;
import com.novatronic.formatter.FormatterFactory;
import com.novatronic.formatter.FormatterInfo;
import com.novatronic.formatter.internal.InternalFormat;
import com.novatronic.formatter.test.IntFormatReader;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ofernandez
 */
public class FieldTest {
    private static final Logger log = Logger.getLogger(FieldTest.class);
    private static FormatterFactory factory;
    private static Map<String,InternalFormat> ifs;
    private static final String formatConfig = "formatField.xml";
    private static final String IFtest = "FIField.xml";
    private static Formatter formatter;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        factory = new FormatterFactory(formatConfig);
        ifs = IntFormatReader.asMap(IFtest);
    }
    
    @AfterClass
    public static void tearDownClass() throws Exception {
        
    }
    
    @Test
    public void testGetParamValueNoParam(){
        log.info("--------------- testGetParamValueNoParam");
        formatter = factory.getFormatter("testGetParamValueNoParam");
        InternalFormat intFmtExpected = ifs.get("testGetParamValueNoParam");
        InternalFormat intFmtResult;
        
        intFmtResult = formatter.getInternalFormatFromConfig();
        
        assertEquals(intFmtExpected, intFmtResult);
    }
    
    @Test
    public void testGetParamValueWithParam(){
        log.info("--------------- testGetParamValueWithParam");
        formatter = factory.getFormatter("testGetParamValueWithParam");
        InternalFormat intFmtExpected = ifs.get("testGetParamValueWithParam");
        InternalFormat intFmtResult;
        
        intFmtResult = formatter.getInternalFormatFromConfig("01","11111111");
        
        assertEquals(intFmtExpected, intFmtResult);
    }
    
    @Test
    public void testGetParamValueWithParamAndNoValue(){
        log.info("--------------- testGetParamValueWithParamAndNoValue");
        formatter = factory.getFormatter("testGetParamValueWithParamAndNoValue");
        InternalFormat intFmtExpected = ifs.get("testGetParamValueWithParamAndNoValue");
        InternalFormat intFmtResult;
        
        intFmtResult = formatter.getInternalFormatFromConfig("06","11111111");
        
        assertEquals(intFmtExpected, intFmtResult);
    }
    
    @Test
    public void testPath() {
        log.info("--------------- testPath");
        FormatterInfo formatterInfo = (FormatterInfo) factory.getFormatter("testPath");
        GDummyField group;
        
        assertEquals("01", formatterInfo.getFieldById("01").getPath());
        assertEquals("02", formatterInfo.getFieldById( "02").getPath());
        assertEquals("03", formatterInfo.getFieldById("03").getPath());
        assertEquals("04", formatterInfo.getFieldById("04").getPath());
        
        group = (GDummyField) formatterInfo.getFieldById("04");
        assertEquals("04.41", group.getChildFieldById("41").getPath());
        
        group = (GDummyField)  group.getChildFieldById("42");
        assertEquals("04.42.421", group.getChildFieldById("421").getPath());
        assertEquals("04.42.422", group.getChildFieldById("422").getPath());
    }
}
