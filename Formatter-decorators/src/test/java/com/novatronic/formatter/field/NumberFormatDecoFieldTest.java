package com.novatronic.formatter.field;

import com.novatronic.formatter.Formatter;
import com.novatronic.formatter.FormatterFactory;
import com.novatronic.formatter.internal.InternalField;
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
public class NumberFormatDecoFieldTest {
    private static final Logger log = Logger.getLogger(NumberFormatDecoFieldTest.class);
    private static FormatterFactory factory;
    private static Formatter formatter;
    private static Map<String,InternalFormat> ifs;
    private static final String formatConfig = "formatNumberFormatDeco.xml";
    private static final String IFtest = "FINumberFormatDeco.xml";
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        factory = new FormatterFactory(formatConfig);
        ifs = IntFormatReader.asMap(IFtest);
    }
    
    //@Test
    public void testConfig() {
        log.info("--------------- testConfig");
        formatter = factory.getFormatter("T01");
        
        assertNotNull(formatter);
    }
    
    /**
     * Test of putBytes method, of class FIXEDField.
     */
    //@Test
    public void testToField() {
        log.info("--------------- testToField");
        formatter = factory.getFormatter("T01");
        VariableByteBuffer frameExpected = new VariableByteBuffer();
        VariableByteBuffer frame;
        InternalFormat intFormat = ifs.get("TEST-TF");
        
        frameExpected.add("123545145846004500");
        frame = formatter.getFrames(intFormat);
        log.info("[" + frame + "]");
        
        assertEquals(frameExpected.toString(), frame.toString());
    }

    //@Test
    public void testFromField(){
        log.info("--------------- testFromField");
        formatter = factory.getFormatter("T01");
        VariableByteBuffer trama = new VariableByteBuffer(12);
        InternalFormat intFormat;
        InternalFormat intFormatExpected = ifs.get("TEST-FF");
        
        trama.add("023100078643000100");
        log.debug("Trama[" + trama + "]");
        intFormat = formatter.createInternalFormatFromFrame(trama);
        log.info(intFormat);
        
        assertEquals(intFormatExpected, intFormat);
    }
    
    //@Test
    public void testToFieldAllowNull(){
        log.info("--------------- testToFieldAllowNull");
        formatter = factory.getFormatter("testToFieldAllowNull");
        VariableByteBuffer frameExpected = new VariableByteBuffer();
        VariableByteBuffer frame;
        InternalField field;
        InternalFormat intFormat = ifs.get("testToFieldAllowNull");
        field = new InternalField("01", null);
        intFormat.addInternalField(field);
        
        frameExpected.add("000000145846004500");
        frame = formatter.getFrames(intFormat);
        log.info("[" + frame + "]");
        
        assertEquals(frameExpected.toString(), frame.toString());
    }
    
    //@Test
    public void testFromFieldAllowBlank(){
        log.info("--------------- testFromFieldAllowBlank");
        formatter = factory.getFormatter("testFromFieldAllowBlank");
        VariableByteBuffer trama = new VariableByteBuffer(12);
        InternalFormat intFormat;
        InternalFormat intFormatExpected = ifs.get("testFromFieldAllowBlank");
        
        trama.add("      078643000100");
        log.debug("Trama[" + trama + "]");
        intFormat = formatter.createInternalFormatFromFrame(trama);
        log.info(intFormat);
        
        assertEquals(intFormatExpected, intFormat);
    }
    
    @Test
    public void testToFieldNumFormat(){
        log.info("--------------- testToFieldNumFormat");
        formatter = factory.getFormatter("testToFieldNumFormat");
        VariableByteBuffer trama = new VariableByteBuffer(12);
        VariableByteBuffer tramaExpected;
        InternalFormat intFormat = ifs.get("testToFieldNumFormat");
        
        trama.add("000050");
        log.debug("Trama[" + trama + "]");
        tramaExpected = formatter.getFrameFromInternalFormat(intFormat);
        log.info(tramaExpected);
        
        assertEquals(tramaExpected, trama);
    }
}
