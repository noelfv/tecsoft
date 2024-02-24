/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.filter;

import com.novatronic.formatter.FormatterFactory;
import org.apache.log4j.Logger;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author ofernandez
 */
public class FilterFactoryTest {

    private static final Logger log = Logger.getLogger(FilterFactoryTest.class);
    private static final String DEFAULT_CONFIG = "formatFilter.xml";

    public FilterFactoryTest() {
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

    @Test
    public void testConfigFilterWithFormatterFactory() {
        log.debug("-------------- testConfigFilterWithFormatterFactory - BEGIN");
        try {
            FormatterFactory factory = new FormatterFactory(DEFAULT_CONFIG);
            assertTrue(true);
        } catch (Exception ex) {
            log.error("error en la configuracion", ex);
            fail();
        }
        log.debug("-------------- testConfigFilterWithFormatterFactory - END");
    }
}
