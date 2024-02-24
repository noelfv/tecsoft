/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.formatter.field;

import com.novatronic.formatter.Formatter;
import com.novatronic.formatter.FormatterFactory;
import com.novatronic.formatter.filter.SingletonFilters;
import com.novatronic.formatter.internal.InternalFormat;
import com.novatronic.formatter.util.VariableByteBuffer;
import org.apache.log4j.Logger;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author nteruya
 */
public class NVarFieldTest {
    private static Logger log = Logger.getLogger(NVarFieldTest.class);
    
    private static FormatterFactory factory;
    private static final String formatConfig = "formatNVar.xml";
    private static Formatter formatter;

    @BeforeClass
    public static void setUpClass() throws Exception {
        factory = new FormatterFactory(formatConfig);
    }
    
    @Test
    public void testConfig() {
        log.info("----------- testConfig");
        formatter = factory.getFormatter("fmt");
    }
    
    @Test
    public void testPutBytes() {
        log.info("--------------- testPutBytes");
        formatter = factory.getFormatter("fmt");
        VariableByteBuffer frameExpected = new VariableByteBuffer();
        VariableByteBuffer frame;
        InternalFormat intFormat;
        
        intFormat = createIntFormatPutBytes();
        frameExpected.add(new byte[]{0x00,0x06});
        frameExpected.add("as.dr5");
        frameExpected.add(new byte[]{0x05});
        frameExpected.add(new byte[]{0x01,0x23,0x45});
        frameExpected.add("0024testeando lo configurado");
        
        frame = formatter.getFrames(intFormat);
        log.info("[" + frame + "]");
        
        assertEquals(frameExpected.toString(), frame.toString());
    }
    
    private InternalFormat createIntFormatPutBytes(){
        InternalFormat intFormat = new InternalFormat();

        intFormat.add("01", "as.dr5");
        intFormat.add("02", "12345");
        intFormat.add("03", "testeando lo configurado");

        return intFormat;
    }
    
    @Test
    public void testReadBytes(){
        log.info("--------------- testReadBytes");
        formatter = factory.getFormatter("fmt");
        VariableByteBuffer trama = new VariableByteBuffer(40);
        InternalFormat intFormat;
        
        trama.add(new byte[]{0x00,0x06});
        trama.add("as.dr5");
        trama.add(new byte[]{0x05,0x12,0x34,0x50});
        trama.add("0024testeando lo configurado");
        log.debug("Trama[" + trama.toHexaString() + "]");
        
        intFormat = formatter.createInternalFormatFromFrame(trama);
        log.info(intFormat);
        
        assertEquals(createIntFormatPutBytes(), intFormat);
    }
    
    @Test
    public void testReadBytesFilter(){
        log.info("--------------- testReadBytesFilter");
        formatter = factory.getFormatter("fmtFilter");
        VariableByteBuffer trama = new VariableByteBuffer(40);
        InternalFormat intFormat;
        String intFmtRes;
        String intFmtExp;
        
        intFmtExp = "\n+>ID=NULL, VALUES=\n" +
                    "+--->ID='01', VALUE=[******]\n" +
                    "+--->ID='02', VALUE=[12345]\n" +
                    "+--->ID='03', VALUE=[************************]\n";
        
        trama.add(new byte[]{0x00,0x06});
        trama.add("as.dr5");
        trama.add(new byte[]{0x05,0x12,0x34,0x50});
        trama.add("0024testeando lo configurado");
        log.debug("Trama[" + trama.toHexaString() + "]");
        
        intFormat = formatter.createInternalFormatFromFrame(trama);
        intFmtRes = SingletonFilters.filter(formatter.getId(), intFormat);
        
        log.info("IF Filter Exp:" + intFmtExp);
        log.info("IF Filter Res:" + intFmtRes);
        
        assertEquals(intFmtExp, intFmtRes);
    }

}