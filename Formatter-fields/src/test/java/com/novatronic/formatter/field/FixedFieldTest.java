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
public class FixedFieldTest {
    private static Logger log = Logger.getLogger(FixedFieldTest.class);

    private static FormatterFactory factory;
    private static Map<String,InternalFormat> ifs;
    private static final String formatConfig = "formatFixed.xml";
    private static final String IFtest = "FIFixed.xml";
    private static Formatter formatter;

    @BeforeClass
    public static void setUpClass() throws Exception {
        factory = new FormatterFactory(formatConfig);
        ifs = IntFormatReader.asMap(IFtest);
    }
    
    @Test
    public void testConfig() {
        log.info("----------- testConfig");
        assertEquals(6, factory.getNumberOfFormatters());
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
        
        frameExpected.add(new byte[]{0x02,0x00});
        frameExpected.add("algo   0023a4      ");
        frame = formatter.getFrames(intFormat);
        log.info("[" + frame + "]");
        
        assertEquals(frameExpected, frame);
    }

    @Test
    public void testReadBytes(){
        log.info("--------------- testReadBytes");
        formatter = factory.getFormatter("testReadBytes");
        VariableByteBuffer trama = new VariableByteBuffer(40);
        InternalFormat intFormat;
        InternalFormat intFormatExpected;
        
        intFormatExpected = ifs.get("testReadBytes");
        trama.add(new byte[]{0x02,0x00});
        trama.add("TEST 17890120501");
        log.debug("Trama[" + trama + "]");
        intFormat = formatter.createInternalFormatFromFrame(trama);
        log.info(intFormat);
        
        assertEquals(intFormatExpected, intFormat);
    }
    
    @Test
    public void testReadBytesCompress(){
        log.info("--------------- testReadBytesCompress");
        formatter = factory.getFormatter("testReadBytesCompress");
        VariableByteBuffer trama = new VariableByteBuffer(40);
        InternalFormat intFormat;
        InternalFormat intFormatExpected = ifs.get("testReadBytesCompress");
        
        trama.add(new byte[]{0x01,0x23,0x02,0x03,0x00,0x00});
        log.debug("Trama[" + trama + "]");
        intFormat = formatter.createInternalFormatFromFrame(trama);
        log.info(intFormat);
        
        assertEquals(intFormatExpected, intFormat);
    }

     @Test
    public void testPutBytesFilter() {
        log.info("--------------- testPutBytesFilter");
        formatter = factory.getFormatter("testPutBytesFilter");
        VariableByteBuffer frameExpected = new VariableByteBuffer();
        VariableByteBuffer frame;
        InternalFormat intFormat = ifs.get("testPutBytesFilter");
        String intFmtRes;
        String intFmtExp;
        
        intFmtExp = "\n+>ID=NULL, VALUES=\n" +
                    "+--->ID='01', VALUE=[0200]\n" +
                    "+--->ID='02', VALUE=[****]\n" +
                    "+--->ID='03', VALUE=[23]\n"+
                    "+--->ID='04', VALUE=[**]\n";
        
        frameExpected.add(new byte[]{0x02,0x00});
        frameExpected.add("algo   0023a4      ");
        frame = formatter.getFrames(intFormat);
        intFmtRes = SingletonFilters.filter(formatter.getId(), intFormat);
        log.info("[" + frame + "]");
        
        assertEquals(frameExpected, frame);
        assertEquals(intFmtExp, intFmtRes);
    }
     
    @Test
    public void testReadBytesFilter(){
        log.info("--------------- testReadBytesFilter");
        formatter = factory.getFormatter("testReadBytesFilter");
        VariableByteBuffer trama = new VariableByteBuffer(40);
        InternalFormat intFormat;
        InternalFormat intFormatExpected;
        String intFmtRes;
        String intFmtExp;
        
        intFormatExpected = ifs.get("testReadBytesFilter");
        intFmtExp = "\n+>ID=NULL, VALUES=\n" +
                    "+--->ID='01', VALUE=[***]\n" +
                    "+--->ID='02', VALUE=[TEST 1789012]\n" +
                    "+--->ID='03', VALUE=[***]\n";
        
        trama.add(new byte[]{0x02,0x00});
        trama.add("TEST 17890120501");
        log.debug("Trama[" + trama + "]");
        intFormat = formatter.createInternalFormatFromFrame(trama);
        intFmtRes = SingletonFilters.filter(formatter.getId(), intFormat);
        
        log.info("IF Filter Exp:" + intFmtExp);
        log.info("IF Filter Res:" + intFmtRes);
        
        assertEquals(intFormatExpected, intFormat);
        assertEquals(intFmtExp, intFmtRes);
    }
    
    @Test
    public void testReadBytesCompressFilter(){
        log.info("--------------- testReadBytesCompressFilter");
        formatter = factory.getFormatter("testReadBytesCompressFilter");
        VariableByteBuffer trama = new VariableByteBuffer(40);
        InternalFormat intFormat;
        InternalFormat intFormatExpected;
        String intFmtRes;
        String intFmtExp;
        
        intFormatExpected = ifs.get("testReadBytesCompressFilter");
        trama.add(new byte[]{0x01,0x23,0x02,0x03,0x00,0x00});        
        intFmtExp = "\n+>ID=NULL, VALUES=\n" +
                    "+--->ID='01', VALUE=[***]\n";
        
        log.debug("Trama[" + trama + "]");
        intFormat = formatter.createInternalFormatFromFrame(trama);
        intFmtRes = SingletonFilters.filter(formatter.getId(), intFormat);
        
        log.info("IF Filter Exp:" + intFmtExp);
        log.info("IF Filter Res:" + intFmtRes);
        
        assertEquals(intFormatExpected, intFormat);
        assertEquals(intFmtExp, intFmtRes);
    }
}