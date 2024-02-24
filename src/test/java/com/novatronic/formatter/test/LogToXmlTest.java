/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.test;

import org.apache.log4j.Logger;
import org.junit.*;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Omar
 */
public class LogToXmlTest {

    private static final Logger log = Logger.getLogger(LogToXmlTest.class);

    public LogToXmlTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
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

    /**
     * este test se usaba para el formato anterior de salida del formato interno
     */
    //@Test
    public void testToXml() {
        log.info("-------------------------- testToXml");
        String logTest = test();
        String expResult = expected();
        String result = LogToXml.toXml(logTest);
        log.debug("Result:\n" + result);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testToXml2() {
        log.info("-------------------------- testToXml2");
        String logTest = test2();
        String expResult = expected();
        String result = LogToXml.toXml(logTest);
        log.debug("Result:\n" + result);
        assertEquals(expResult, result);
    }
    
    private String test2() {
        String test;

        test = "+>ID=NULL, VALUES=\n"
                + "+--->ID='TT', VALUE=[200]\n"
                + "+--->ID='bmp', VALUES=\n"
                + "+------>ID='2', VALUE=[005002]\n"
                + "+------>ID='3', VALUE=[01FF00]\n"
                + "+------>ID='4', VALUE=[500]\n"
                + "+------>ID='11', VALUE=[005001]\n"
                + "+--->ID='QQ', VALUE=[0050]\n"
                + "+--->ID='VV', VALUE=[0051]\n"
                + "+--->ID='BB', VALUE=[0052]\n";
        
        return test;
    }

    private String test() {
        String test;

        test = "+>ID=, VALUES=\n"
                + "+--->ID=TT, VALUE=[200]\n"
                + "+--->ID=bmp, VALUES=\n"
                + "+------>ID=2, VALUE=[005002]\n"
                + "+------>ID=3, VALUE=[01FF00]\n"
                + "+------>ID=4, VALUE=[500]\n"
                + "+------>ID=11, VALUE=[005001]\n"
                + "+--->ID=QQ, VALUE=[0050]\n"
                + "+--->ID=VV, VALUE=[0051]\n"
                + "+--->ID=BB, VALUE=[0052]\n";
        
        return test;
    }

    private String expected() {
        String result;
        result = "<intformat>\n"
                + "   <field id=\"TT\">200</field>\n"
                + "   <field id=\"bmp\">\n"
                + "      <field id=\"2\">005002</field>\n"
                + "      <field id=\"3\">01FF00</field>\n"
                + "      <field id=\"4\">500</field>\n"
                + "      <field id=\"11\">005001</field>\n"
                + "   </field>\n"
                + "   <field id=\"QQ\">0050</field>\n"
                + "   <field id=\"VV\">0051</field>\n"
                + "   <field id=\"BB\">0052</field>\n"
                + "</intformat>";
        return result;
    }
}
