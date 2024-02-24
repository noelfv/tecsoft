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
public class TokenFieldTest{
    
    private static final Logger log = Logger.getLogger(TokenFieldTest.class);
    private static FormatterFactory factory;
    private static Formatter formatter;
    private static Map<String,InternalFormat> ifs;
    private static final String formatConfig = "formatToken.xml";
    private static final String IFtest = "FIToken.xml";
    
    
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
        
        frameExpected.add("aaaaBbbbBccB");
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
        
        trama.add("mmmBppBqqqqqB");
        log.debug("Trama[" + trama + "]");
        intFormat = formatter.createInternalFormatFromFrame(trama);
        log.info(intFormat);
        
        assertEquals(intFormatExpected, intFormat);
    }
    
    @Test
    public void testReadBytesNoEndToken(){
        log.info("--------------- testReadBytesNoEndToken");
        formatter = factory.getFormatter("testReadBytesNoEndToken");
        VariableByteBuffer trama = new VariableByteBuffer(12);
        InternalFormat intFormat;
        InternalFormat intFormatExpected = ifs.get("testReadBytesNoEndToken");
        
        trama.add("mmmBppBqqqqqBdddd");
        log.debug("Trama[" + trama + "]");
        intFormat = formatter.createInternalFormatFromFrame(trama);
        log.info(intFormat.toString());
        
        assertEquals(intFormatExpected, intFormat);
    }
    
    @Test
    public void testReadBytesNoEndTokenFilter(){
        log.info("--------------- testReadBytesNoEndTokenFilter");
        formatter = factory.getFormatter("testReadBytesNoEndTokenFilter");
        VariableByteBuffer trama = new VariableByteBuffer(12);
        InternalFormat intFormat;
        InternalFormat intFormatExpected;
        String intFmtRes;
        String intFmtExp;
        
        
        intFormatExpected = ifs.get("testReadBytesNoEndTokenFilter");
        intFmtExp = "\n+>ID=NULL, VALUES=\n" +
                    "+--->ID='01', VALUE=[---]\n" +
                    "+--->ID='02', VALUE=[**]\n" +
                    "+--->ID='03', VALUE=[qqqqq]\n" +
                    "+--->ID='04', VALUE=[++++]\n";
        trama.add("mmmBppBqqqqqBdddd");
        log.debug("Trama[" + trama + "]");
        intFormat = formatter.createInternalFormatFromFrame(trama);
        intFmtRes = SingletonFilters.filter(formatter.getId(), intFormat);
        
        log.info("IF Filter Exp:" + intFmtExp);
        log.info("IF Filter Res:" + intFmtRes);
        
        assertEquals(intFormatExpected, intFormat);
        assertEquals(intFmtExp, intFmtRes);
    }
}
