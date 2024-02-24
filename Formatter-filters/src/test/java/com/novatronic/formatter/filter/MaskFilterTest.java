/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.filter;

import com.novatronic.formatter.util.ResourceHelper;
import java.net.URL;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author rcastillejo
 */
public class MaskFilterTest {

    private static final Logger log = Logger.getLogger(MaskFilterTest.class);
    private static final String DEFAULT_CONFIG = "format.xml";
    private static Element filtersConfig;

    public MaskFilterTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        URL urlFormat = ResourceHelper.findResource(DEFAULT_CONFIG);
        Document doc = (new SAXBuilder()).build(urlFormat);
        filtersConfig = doc.getRootElement().getChild("filters");
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of readCustomConfiguration method, of class MaskFilter.
     */
    @Test
    public void testReadCustomConfiguration() {
        try {
            Filters filters = new Filters();
            filters.readConfiguration(filtersConfig);
            assertTrue(true);
        } catch (Exception ex) {
            log.error("error en la configuracion", ex);
            fail();
        }
    }

    /**
     * Test of filter method, of class MaskFilter.
     */
    @Test
    public void testFilter() {
        Filters filters = new Filters();
        filters.readConfiguration(filtersConfig);

        assertEquals("*******", filters.getFilter("HIDDING").filter("prueba1"));
        assertEquals("********", filters.getFilter("HIDDING").filter("prueba12"));
        assertEquals("*********", filters.getFilter("HIDDING").filter("prueba123"));

        assertEquals("***", filters.getFilter("TRUNK_FIRST").filter("pru"));
        assertEquals("****", filters.getFilter("TRUNK_FIRST").filter("prue"));
        assertEquals("prue*", filters.getFilter("TRUNK_FIRST").filter("prueb"));
        assertEquals("prue**", filters.getFilter("TRUNK_FIRST").filter("prueba"));
        assertEquals("prue***", filters.getFilter("TRUNK_FIRST").filter("prueba1"));

        assertEquals("+++", filters.getFilter("TRUNK_LAST").filter("pru"));
        assertEquals("++++", filters.getFilter("TRUNK_LAST").filter("prue"));
        assertEquals("+rueb", filters.getFilter("TRUNK_LAST").filter("prueb"));
        assertEquals("++ueba", filters.getFilter("TRUNK_LAST").filter("prueba"));
        assertEquals("+++eba1", filters.getFilter("TRUNK_LAST").filter("prueba1"));

        assertEquals("-------", filters.getFilter("MASKING").filter("prueba1"));
        assertEquals("--------", filters.getFilter("MASKING").filter("prueba12"));
        assertEquals("---------", filters.getFilter("MASKING").filter("prueba123"));
        assertEquals("----------", filters.getFilter("MASKING").filter("prueba1234"));
        assertEquals("prueba-2345", filters.getFilter("MASKING").filter("prueba12345"));

    }

    //@Test
    public void testFilterEstres() {

        Filters filters = new Filters();
        filters.readConfiguration(filtersConfig);
        
        int iterations = 10000;
        long init = System.currentTimeMillis();

        log.debug("testFilterEstres ---------- BEGIN");
        for (int i = 0; i < iterations; i++) {
            filters.getFilter("MASKING").filter("prueba12345");
        }
        log.debug("testFilterEstres ---------- END :" + (System.currentTimeMillis() - init));
    }
}
