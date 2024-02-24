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
public class CollectGroupFieldTest{
    
    private static final Logger log = Logger.getLogger(CollectGroupFieldTest.class);
    private static FormatterFactory factory;
    private static Formatter formatter;
    private static Map<String,InternalFormat> ifs;
    private static final String formatConfig = "formatCollectGroup.xml";
    private static final String IFtest = "FICollectGroup.xml";
    
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        factory = new FormatterFactory(formatConfig);
        ifs = IntFormatReader.asMap(IFtest);
    }
    
    @Test
    public void testConfig() {
        log.info("--------------- testConfig");
        formatter = factory.getFormatter("test");
        
        assertNotNull(formatter);
    }
    
    /**
     * Test of putBytes method, of class FIXEDField.
     */
    @Test
    public void testPutBytesUp() {
        log.info("--------------- testPutBytesUp");
        formatter = factory.getFormatter("testPutBytesUp");
        VariableByteBuffer frameExpected = new VariableByteBuffer();
        VariableByteBuffer frame;
        InternalFormat intFormat = ifs.get("testPutBytesUp");
        
        frameExpected.add("123aabbbbb12ccc   ");
        frame = formatter.getFrames(intFormat);
        log.info("[" + frame + "]");
        
        assertEquals(frameExpected.toString(), frame.toString());
    }
    
    @Test
    public void testPutBytesDown() {
        log.info("--------------- testPutBytesDown");
        formatter = factory.getFormatter("testPutBytesDown");
        VariableByteBuffer frameExpected = new VariableByteBuffer();
        VariableByteBuffer frame;
        InternalFormat intFormat = ifs.get("testPutBytesDown");
        
        frameExpected.add("123aabbbbb12ccc   ");
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
    public void testPutBytesLimit() {
        log.info("--------------- testPutBytesLimit");
        formatter = factory.getFormatter("testPutBytesLimit");
        VariableByteBuffer frameExpected = new VariableByteBuffer();
        VariableByteBuffer frame;
        InternalFormat intFormat = ifs.get("testPutBytesLimit");
        
        frameExpected.add("wwwaaaa07123456700889012345");
        frame = formatter.getFrames(intFormat);
        log.info("[" + frame + "]");
        
        assertEquals(frameExpected.toString(), frame.toString());
    }
    
    @Test
    public void testPartialDataFromConfig() {
        log.info("--------------- testPartialDataFromConfig");
        InternalFormat intFormatExpected;
        InternalFormat intFormatResult;
        formatter = factory.getFormatter("testPartialDataFromConfig");
        intFormatExpected = ifs.get("testPartialDataFromConfig");
        
        intFormatResult = formatter.getInternalFormatFromConfig();
        log.info("Result:" + intFormatResult);
        
        assertEquals(intFormatExpected, intFormatResult);
    }

    @Test
    public void testReadBytesFilter(){
        log.info("--------------- testReadBytesFilter");
        formatter = factory.getFormatter("testReadBytesFilter");
        VariableByteBuffer trama = new VariableByteBuffer(12);
        InternalFormat intFormat;
        InternalFormat intFormatExpected = ifs.get("testReadBytesFilter");
        String intFmtRes;
        String intFmtExp;
        
        intFmtExp = "\n+>ID=NULL, VALUES=\n" +
                    "+--->ID='P', VALUE=[***]\n" +
                    "+--->ID='Q', VALUE=[++++]\n" +
                    "+--->ID='R', VALUE=[qqqqq]\n" +
                    "+--->ID='S', VALUE=[-]\n" +
                    "+--->ID='T', VALUES=\n" +
                    "+------>ID='a', VALUE=[mmm]\n" +
                    "+------>ID='b', VALUE=[*]\n" +
                    "+------>ID='c', VALUE=[pp]\n" +
                    "+------>ID='d', VALUE=[*]\n";
        trama.add("mmmBppBqqqqqB");
        log.debug("Trama[" + trama + "]");
        intFormat = formatter.createInternalFormatFromFrame(trama);
        intFmtRes = SingletonFilters.filter(formatter.getId(), intFormat);
        
        log.info("IF Filter Exp:" + intFmtExp);
        log.info("IF Filter Res:" + intFmtRes);
        
        assertEquals(intFormatExpected, intFormat);
        assertEquals(intFmtExp, intFmtRes);
    }
    
    @Test
    public void testPartialDataFromConfigFilter() {
        log.info("--------------- testPartialDataFromConfigFilter");
        InternalFormat intFormatExpected;
        InternalFormat intFormatResult;
        String intFmtExp;
        String intFmtRes;
        
        formatter = factory.getFormatter("testPartialDataFromConfigFilter");
        intFormatExpected = ifs.get("testPartialDataFromConfigFilter");
        intFmtExp = "\n+>ID=NULL, VALUES=\n" +
                    "+--->ID='01', VALUES=\n" +
                    "+------>ID='01', VALUE=[**]\n" +
                    "+------>ID='02', VALUE=[02]\n" +
                    "+------>ID='03', VALUE=[**]\n" +
                    "+------>ID='04', VALUE=[123abc123]\n" +
                    "+--->ID='Data_appl', VALUES=\n" +
                    "+------>ID='D1', VALUE=[***]\n" +
                    "+------>ID='D2', VALUE=[D02]\n" +
                    "+------>ID='D3', VALUES=\n" +
                    "+--------->ID='C01', VALUE=[***]\n";
        
        intFormatResult = formatter.getInternalFormatFromConfig();
        intFmtRes = SingletonFilters.filter(formatter.getId(), intFormatResult);
        
        log.info("IF Filter Exp:" + intFmtExp);
        log.info("IF Filter Res:" + intFmtRes);
        
        assertEquals(intFormatExpected, intFormatResult);
        assertEquals(intFmtExp, intFmtRes);
    }
}
