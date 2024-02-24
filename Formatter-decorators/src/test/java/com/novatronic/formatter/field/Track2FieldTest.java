/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.field;

import com.novatronic.formatter.Formatter;
import com.novatronic.formatter.FormatterFactory;
import com.novatronic.formatter.field.util.Converter;
import com.novatronic.formatter.util.VariableByteBuffer;
import org.apache.log4j.Logger;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Omar
 */
public class Track2FieldTest {
    private static Logger log = Logger.getLogger(Track2FieldTest.class);

    private static FormatterFactory factory;
    private static final String formatConfig = "formatTrack2.xml";
    private static Formatter formatter;
    
    public Track2FieldTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        factory = new FormatterFactory(formatConfig);
    }

    //@Ignore
    @Test
    public void testReadDecoratorConfig() {
        log.info("----------- testReadDecoratorConfig");
        formatter = factory.getFormatter("FMTPAMCOM");
        assertNotNull(formatter);
    }
    
    @Ignore
    @Test
    public void testToFieldCompress() {
        log.info("----------- testToFieldCompress");
        formatter = factory.getFormatter("FMTPAMCOM");
        VariableByteBuffer resultExp = new VariableByteBuffer();
        VariableByteBuffer result;
        
        resultExp.add(new byte[]{0x15});
        resultExp.add(new byte[]{0x12,0x34,0x56,0x7D,0x12,0x34,0x56,0x7F});
        log.debug(Converter.toHexaString(resultExp.getByteArray()));
        result = formatter.getFrames();
        log.debug(Converter.toHexaString(result.getByteArray()));
        assertEquals(resultExp, result);
    }
    
    @Ignore
    @Test
    public void testToFieldNoCompress() {
        log.info("----------- testToFieldNoCompress");
        formatter = factory.getFormatter("FMTPAM");
        VariableByteBuffer resultExp = new VariableByteBuffer();
        VariableByteBuffer result;
        
        resultExp.add("1234567=1234567");
        log.debug("resultExp[" + resultExp.toString() + "]");
        result = formatter.getFrames();
        log.debug("result[" + result.toString() + "]");
        assertEquals(resultExp, result);
    }
}
