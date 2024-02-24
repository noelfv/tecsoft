/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.formatter.macros;

import com.novatronic.formatter.Formatter;
import com.novatronic.formatter.FormatterFactory;
import com.novatronic.formatter.util.VariableByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import org.apache.log4j.Logger;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author ofernandez
 */
public class SimpleParserMacroTest {
    private static final Logger log = Logger.getLogger(SimpleParserMacroTest.class);
    private static Map<String, Macro> macros;
    
    public SimpleParserMacroTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        macros = new TreeMap<String, Macro>();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testConfigParse() {
        log.debug("-------------- testParse - BEGIN");
        try{
            FormatterFactory factory = new FormatterFactory();
            assertTrue(true);
        } catch(Exception ex){
            log.error("error en la configuracion",ex);
            fail();
        }
        log.debug("-------------- testParse - END");
    }

    @Test
    public void testParse() {
        log.debug("-------------- testParse - BEGIN");
        FormatterFactory factory = new FormatterFactory();
        SimpleDateFormat formato = new SimpleDateFormat("yyyyMMdd");
        String fechaHoy = formato.format(new Date());
        
        Formatter formatter = factory.getFormatter("003");
        VariableByteBuffer bufferExpected = new VariableByteBuffer();
        bufferExpected.add(fechaHoy + "000050FIX03   ");
        VariableByteBuffer bufferResult = formatter.getFrames();
        assertEquals(bufferExpected, bufferResult);
        log.debug("-------------- testParse - END");
    }
    
    @Test
    public void testParse_MasDeUnArg() {
        log.debug("-------------- testParse_MasDeUnArg");
        SimpleParserMacro parser = new SimpleParserMacro();
        macros.put("MIMACRO", new MacroMock());
        parser.setMacros(macros);
        String lineToParse = "MIMACRO(campo1, campo2, campo3 )";
        String expected = "1=[campo1],2=[campo2],3=[campo3]";
        String result;
        
        result = parser.parse(lineToParse);
        
        assertEquals(expected, result);
        
    }

}