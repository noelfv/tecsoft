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
import static org.junit.Assert.assertNotNull;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Omar Fernandez
 * @version 1.0
 * @since 1.0
 * @date 05 Abr. 2012
 */
public class ToEndFieldTest{
    
    private static final Logger log = Logger.getLogger(ToEndFieldTest.class);
    private static FormatterFactory factory;
    private static Formatter formatter;
    private static Map<String,InternalFormat> ifs;
    private static final String formatConfig = "formatToEnd.xml";
    private static final String IFtest = "FIToEnd.xml";
    
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        factory = new FormatterFactory(formatConfig);
        ifs = IntFormatReader.asMap(IFtest);
    }
    
    @Test
    public void testConfig() {
        log.info("--------------- testConfig");
        formatter = factory.getFormatter("testConfig");
        
        assertNotNull(formatter);
    }
    
    /**
     * Test of putBytes method, of class FIXEDField.
     */
    @Test
    public void testPutBytes() {
        log.info("--------------- testPutBytes");
        formatter = factory.getFormatter("testPutBytes");
        VariableByteBuffer frameExpected = new VariableByteBuffer();
        VariableByteBuffer frame;
        InternalFormat intFormat = ifs.get("testPutBytes");
        
        frameExpected.add("aaa123456");
        frame = formatter.getFrames(intFormat);
        log.info("[" + frame + "]");
        
        assertEquals(frameExpected.toString(), frame.toString());
    }

    @Test
    public void testReadBytes(){
        log.info("--------------- testReadBytes");
        formatter = factory.getFormatter("testReadBytes");
        VariableByteBuffer trama = new VariableByteBuffer(12);
        InternalFormat intFormat;
        InternalFormat intFormatExpected = ifs.get("testReadBytes");
        
        trama.add("mmm123456aabbcc");
        log.debug("Trama[" + trama + "]");
        intFormat = formatter.createInternalFormatFromFrame(trama);
        log.info(intFormat);
        
        assertEquals(intFormatExpected, intFormat);
    }
    
    @Test
    public void testReadBytesToEndTwice(){
        log.info("--------------- testReadBytesToEndTwice");
        formatter = factory.getFormatter("testReadBytesToEndTwice");
        VariableByteBuffer trama = new VariableByteBuffer(12);
        InternalFormat intFormat;
        InternalFormat intFormatExpected = ifs.get("testReadBytesToEndTwice");
        
        trama.add("mmm123456aabbcc");
        log.debug("Trama[" + trama + "]");
        intFormat = formatter.createInternalFormatFromFrame(trama);
        log.info(intFormat);
        
        assertEquals(intFormatExpected, intFormat);
    }
    
    @Test
    public void testReadBytesMax(){
        log.info("--------------- testReadBytesMax");
        formatter = factory.getFormatter("testReadBytesMax");
        VariableByteBuffer trama = new VariableByteBuffer(12);
        InternalFormat intFormat;
        InternalFormat intFormatExpected = ifs.get("testReadBytesMax");
        
        trama.add("mmm123");
        log.debug("Trama[" + trama + "]");
        intFormat = formatter.createInternalFormatFromFrame(trama);
        log.info(intFormat);
        
        assertEquals(intFormatExpected, intFormat);
    }
    
    @Test
    public void testPutBytesMax(){
        log.info("--------------- testPutBytesMax");
        formatter = factory.getFormatter("testPutBytesMax");
        VariableByteBuffer frameExpected = new VariableByteBuffer();
        VariableByteBuffer frame;
        InternalFormat intFormat = ifs.get("testPutBytesMax");
        
        frameExpected.add("aaa123456789");
        frame = formatter.getFrames(intFormat);
        log.info("[" + frame + "]");
        
        assertEquals(frameExpected.toString(), frame.toString());
    }
    
    @Test
    public void testReadBytesMaxFilter(){
        log.info("--------------- testReadBytesMaxFilter");
        formatter = factory.getFormatter("testReadBytesMaxFilter");
        VariableByteBuffer trama = new VariableByteBuffer(12);
        InternalFormat intFormat;
        InternalFormat intFormatExpected;
        String intFmtRes;
        String intFmtExp;
        
        intFormatExpected = ifs.get("testReadBytesMaxFilter");
        intFmtExp = "\n+>ID=NULL, VALUES=\n" +
                    "+--->ID='01', VALUE=[mmm]\n" +
                    "+--->ID='02', VALUE=[****]\n" +
                    "+--->ID='03', VALUE=[++++++]\n";
        trama.add("mmm1234123456");
        log.debug("Trama[" + trama + "]");
        intFormat = formatter.createInternalFormatFromFrame(trama);
        intFmtRes = SingletonFilters.filter(formatter.getId(), intFormat);
        
        log.info("IF Filter Exp:" + intFmtExp);
        log.info("IF Filter Res:" + intFmtRes);
        
        assertEquals(intFormatExpected, intFormat);
        assertEquals(intFmtExp, intFmtRes);
    }
}
