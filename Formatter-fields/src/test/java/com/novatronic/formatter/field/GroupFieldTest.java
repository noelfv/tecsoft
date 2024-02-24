/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.formatter.field;

import com.novatronic.formatter.Formatter;
import com.novatronic.formatter.FormatterFactory;
import com.novatronic.formatter.filter.SingletonFilters;
import com.novatronic.formatter.internal.InternalFormat;
import com.novatronic.formatter.test.IntFormatReader;
import com.novatronic.formatter.util.VariableByteBuffer;
import java.util.Map;
import org.apache.log4j.Logger;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ofernandez
 */
public class GroupFieldTest {
    private static Logger log = Logger.getLogger(GroupFieldTest.class);
    private static FormatterFactory factory;
    private static Map<String,InternalFormat> ifs;
    private static final String formatConfig = "formatGroup.xml";
    private static final String IFtest = "FIGroup.xml";
    private static Formatter formatter;

    public GroupFieldTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    	factory = new FormatterFactory(formatConfig);
        ifs = IntFormatReader.asMap(IFtest);
    }

    /**
     * Test of putBytes method, of class GROUPField.
     */
    //@Ignore
    @Test
    public void testPutBytes() {
        log.debug("--------------- testPutBytes");
    	formatter = factory.getFormatter("testPutBytes");
        VariableByteBuffer result;
        VariableByteBuffer expResult = new VariableByteBuffer(20);
    	InternalFormat intFormat;
    	
        intFormat = ifs.get("testPutBytes");
        log.debug("FI:" + intFormat);
        log.debug(intFormat);
        expResult.add("aabbbbccc");
        result = formatter.getFrames(intFormat);
        assertEquals(expResult, result);
    }

    /**
     * Test of readBytes method, of class GROUPField.
     */
    //@Ignore
    @Test
    public void testReadBytes() {
    	log.debug("--------------- testReadBytes");
        formatter = factory.getFormatter("testReadBytes");
        InternalFormat intFormatExpected;
        InternalFormat intFormatResult;
        String trama;
        
        intFormatExpected = ifs.get("testReadBytes");
        trama = "aabbbbccc";
        intFormatResult = formatter.createInternalFormatFromFrame(trama);
        log.debug("Leido:" + intFormatResult);
        
        assertEquals(intFormatExpected, intFormatResult);
    }
    
    /**
     * El campo group no deberia ser filtrado ya que es un campo contenedor de 
     * otros campos. Se realiza la prueba para verificar que no afecte el 
     * filtrado de sus campos contenidos.
     */
    @Test
    public void testReadBytesFilter() {
    	log.debug("--------------- testReadBytesFilter");
        formatter = factory.getFormatter("testReadBytesFilter");
        InternalFormat intFormatExpected;
        InternalFormat intFormatResult;
        String trama;
        String intFmtRes;
        String intFmtExp;
        
        intFormatExpected = ifs.get("testReadBytesFilter");
        intFmtExp = "\n+>ID=NULL, VALUES=\n" +
                    "+--->ID='01', VALUES=\n" +
                    "+------>ID='01', VALUE=[aa]\n" +
                    "+------>ID='02', VALUE=[****]\n" +
                    "+------>ID='03', VALUE=[ccc]\n";
        trama = "aabbbbccc";
        intFormatResult = formatter.createInternalFormatFromFrame(trama);
        intFmtRes = SingletonFilters.filter(formatter.getId(), intFormatResult);
        
        log.info("IF Filter Exp:" + intFmtExp);
        log.info("IF Filter Res:" + intFmtRes);
        
        assertEquals(intFormatExpected, intFormatResult);
        assertEquals(intFmtExp, intFmtRes);
    }

}