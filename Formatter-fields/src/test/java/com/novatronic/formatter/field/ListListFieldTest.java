/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.formatter.field;

import com.novatronic.formatter.Formatter;
import com.novatronic.formatter.FormatterFactory;
import com.novatronic.formatter.exception.CreateFrameException;
import com.novatronic.formatter.filter.SingletonFilters;
import com.novatronic.formatter.internal.InternalFormat;
import com.novatronic.formatter.test.IntFormatReader;
import com.novatronic.formatter.util.VariableByteBuffer;
import java.util.Map;
import org.apache.log4j.Logger;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ofernandez
 */
public class ListListFieldTest {
    private static Logger log = Logger.getLogger(ListListFieldTest.class);
    private static FormatterFactory factory;
    private static Formatter formatter;
    private static Map<String,InternalFormat> ifs;
    private static final String formatConfig = "formatListList.xml";
    private static final String IFtest = "FIListList.xml";

    public ListListFieldTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    	factory = new FormatterFactory(formatConfig);
        ifs = IntFormatReader.asMap(IFtest);
    }

    /**
     * Test of putBytes method, of class GROUPField.
     */
    @Test
    public void testPutBytes() {
        log.debug("--------------- testPutBytes");
        VariableByteBuffer result;
        VariableByteBuffer expResult = new VariableByteBuffer(20);
    	InternalFormat intFormat;
    	
        intFormat = generateIntFormatPutBytes();
        log.debug(intFormat);
        expResult.add("01a  aaa ");
    	formatter = factory.getFormatter("fmtListList");
        result = formatter.getFrames(intFormat);
        log.debug("[" + result + "]");
        
        assertEquals(expResult, result);
    }
    
    private InternalFormat generateIntFormatPutBytes(){
        InternalFormat intFormat = new InternalFormat();
        
        intFormat.add("01", "01");
        InternalFormat innerFormat = new InternalFormat("02");
            InternalFormat innerInFormat = new InternalFormat("0");
                innerInFormat.add("01", "a");
                innerInFormat.add("02", "aaa");
            innerFormat.addInternalField(innerInFormat);
    	intFormat.addInternalField(innerFormat);
        
        return intFormat;
    }

    /**
     * Test of readBytes method, of class GROUPField.
     */
    @Test
    public void testReadBytes() {
    	log.debug("--------------- testReadBytes");
        String trama = "03AAAaaaaBBBbbbbCCCcccc";
        formatter = factory.getFormatter("fmtListList");
        InternalFormat intFormat = formatter.createInternalFormatFromFrame(trama);
        log.debug("Leido:" + intFormat);
        log.debug("Campo:"+intFormat.getIFmt("02.2"));
    }
    
    @Test
    public void testCtes(){
        log.debug("--------------- testCtes");
        formatter = factory.getFormatter("testCtes");
        InternalFormat intFormat = ifs.get("testCtes");
        String frameResult;
        String frameExpected;
        
        log.debug("Pasando el FI:" + intFormat);
        frameExpected = "02HHHA   BBCCCHHHA   BBCCC";
        frameResult = formatter.getFrameFromInternalFormat(intFormat).toString();
        log.debug("FI, luego de procesar la tramas:" + intFormat);
        log.debug("Trama.Result:[" + frameResult + "]");
        
        assertEquals(frameExpected, frameResult);
    }
    
    @Test
    public void testCtesException(){
        log.debug("--------------- testCtesException");
        formatter = factory.getFormatter("testCtesException");
        InternalFormat intFormat = ifs.get("testCtesException");
        VariableByteBuffer frame;
        
        try{
            frame = formatter.getFrameFromInternalFormat(intFormat);
            assertTrue(false);
        }catch(CreateFrameException ex){
            log.debug("Una buena excepción:" + ex.getIntFormat());
            assertTrue(true);
        }
    }
    
    @Test
    public void testListEmptyOrNull(){
        log.debug("--------------- testListEmptyOrNull");
        VariableByteBuffer result;
        VariableByteBuffer expResult = new VariableByteBuffer(20);
    	InternalFormat intFormat;
        
        formatter = factory.getFormatter("testListEmptyOrNull");
        intFormat = ifs.get("testListEmptyOrNull");
        
        try{
            expResult.add("00");
            result = formatter.getFrameFromInternalFormat(intFormat);
            log.debug("[" + result + "]");
            assertEquals(expResult, result);
        }catch(CreateFrameException ex){
            log.debug("Una buena excepción:" + ex.getIntFormat());
            assertTrue(true);
        }
    }

    @Test
    public void testFmtListListFilter() {
    	log.debug("--------------- testFmtListListFilter");
        InternalFormat intFormat;
        String intFmtRes;
        String intFmtExp;
        
        String trama = "03AAAaaaa1AAAaaaaBBBbbbb1BBBbbbbCCCcccc1CCCcccc";        
        intFmtExp = "\n+>ID=NULL, VALUES=\n" +
                    "+--->ID='1', VALUE=[**]\n" +
                    "+--->ID='2', VALUES=\n" +
                    "+------>ID='0', VALUES=\n" +
                    "+--------->ID='1', VALUE=[AAA]\n" +
                    "+--------->ID='2', VALUE=[****]\n" +      
                    "+--------->ID='3', VALUE=[*]\n" +    
                    "+--------->ID='4', VALUES=\n" +   
                    "+------------>ID='0', VALUES=\n" +
                    "+--------------->ID='1', VALUE=[+++]\n" +
                    "+--------------->ID='2', VALUE=[aaaa]\n" +
                    "+------>ID='1', VALUES=\n" +
                    "+--------->ID='1', VALUE=[BBB]\n" +
                    "+--------->ID='2', VALUE=[****]\n" +
                    "+--------->ID='3', VALUE=[*]\n" +    
                    "+--------->ID='4', VALUES=\n" +   
                    "+------------>ID='0', VALUES=\n" +
                    "+--------------->ID='1', VALUE=[+++]\n" +
                    "+--------------->ID='2', VALUE=[bbbb]\n" +
                    "+------>ID='2', VALUES=\n" +
                    "+--------->ID='1', VALUE=[CCC]\n" +
                    "+--------->ID='2', VALUE=[****]\n" +        
                    "+--------->ID='3', VALUE=[*]\n" +     
                    "+--------->ID='4', VALUES=\n" +   
                    "+------------>ID='0', VALUES=\n" +
                    "+--------------->ID='1', VALUE=[+++]\n" +
                    "+--------------->ID='2', VALUE=[cccc]\n";
        formatter = factory.getFormatter("fmtListListFilter");
        intFormat = formatter.createInternalFormatFromFrame(trama);
        intFmtRes = SingletonFilters.filter(formatter.getId(), intFormat);
        log.info("IF Filter Exp:" + intFmtExp);
        log.info("IF Filter Res:" + intFmtRes);
        assertEquals(intFmtExp, intFmtRes);
    }
}