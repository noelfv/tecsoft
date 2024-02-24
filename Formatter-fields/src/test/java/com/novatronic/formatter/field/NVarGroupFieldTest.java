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
public class NVarGroupFieldTest {
    private static final Logger log = Logger.getLogger(NVarGroupFieldTest.class);
    
    private static FormatterFactory factory;
    private static final String formatConfig = "formatNVarGroupField.xml";
    private static Formatter formatter;

    @BeforeClass
    public static void setUpClass() throws Exception {
        factory = new FormatterFactory(formatConfig);
    }
    
    @Test
    public void testConfig() {
        log.info("----------- testConfig");
        formatter = factory.getFormatter("fmt1");
    }
    
    @Test
    public void testPutBytes() {
        log.info("--------------- testPutBytes");
        formatter = factory.getFormatter("fmt1");
        VariableByteBuffer frameExpected = new VariableByteBuffer(10);
        VariableByteBuffer frame;
        InternalFormat intFormat;
        
        intFormat = createIntFormatPutBytes();
        frameExpected.add(new byte[]{0x07});
        frameExpected.add("AH 0045");
        
        frame = formatter.getFrames(intFormat);
        log.info("[" + frame + "]");
        
        assertEquals(frameExpected.toString(), frame.toString());
    }
    
    private InternalFormat createIntFormatPutBytes(){
        InternalFormat intFormat = new InternalFormat();
        InternalFormat innerIntFormat = new InternalFormat();
        
        innerIntFormat.add("01", "AH");
        innerIntFormat.add("02", "45");
        innerIntFormat.setId("01");
        intFormat.addInternalField(innerIntFormat);

        return intFormat;
    }
    
    @Test
    public void testReadBytes(){
        log.info("--------------- testReadBytes");
        formatter = factory.getFormatter("fmt2");
        VariableByteBuffer trama = new VariableByteBuffer(40);
        InternalFormat intFormat;
        InternalFormat intFormatExpected;
        
        trama.add(new byte[]{0x19});
        trama.add("Hola a todos   0003");
        intFormatExpected = createIntFormatReadBytes();
        
        intFormat = formatter.createInternalFormatFromFrame(trama);
        log.info(intFormat);
        assertEquals(intFormatExpected, intFormat);
    }
    
    private InternalFormat createIntFormatReadBytes(){
        InternalFormat intFormat = new InternalFormat();
        InternalFormat innerIntFormat = new InternalFormat();
        
        innerIntFormat.add("01", "Hola a todos");
        innerIntFormat.add("02", "3");
        innerIntFormat.setId("01");
        intFormat.addInternalField(innerIntFormat);

        return intFormat;
    }
    
    /**
     * El campo group no deberia ser filtrado ya que es un campo contenedor de 
     * otros campos. Se realiza la prueba para verificar que no afecte el 
     * filtrado de sus campos contenidos.
     */
    @Test
    public void testReadBytesFilter(){
        log.info("--------------- testReadBytesFilter");
        formatter = factory.getFormatter("testReadBytesFilter");
        VariableByteBuffer trama = new VariableByteBuffer(40);
        InternalFormat intFormat;
        InternalFormat intFormatExpected;
        String intFmtRes;
        String intFmtExp;
        
        intFmtExp = "\n+>ID=NULL, VALUES=\n" +
                    "+--->ID='01', VALUES=\n" +
                    "+------>ID='01', VALUE=[************]\n" +
                    "+------>ID='02', VALUE=[3]\n";
        trama.add(new byte[]{0x19});
        trama.add("Hola a todos   0003");
        intFormatExpected = createIntFormatReadBytes();
        
        intFormat = formatter.createInternalFormatFromFrame(trama);
        intFmtRes = SingletonFilters.filter(formatter.getId(), intFormat);
        
        log.info("IF Filter Exp:" + intFmtExp);
        log.info("IF Filter Res:" + intFmtRes);
        
        assertEquals(intFormatExpected, intFormat);
        assertEquals(intFmtExp, intFmtRes);
    }
}