/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.formatter;

import java.net.URISyntaxException;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ofernandez
 */
public class FormatterFactoryTest{
    private static final Logger log = Logger.getLogger(FormatterFactoryTest.class);
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        
    }

    /**
     * Test of readConfiguration method, of class FormatterFactory.
     */
    @Test
    public void testReadConfiguration_sinArgumentos() throws URISyntaxException {
        log.debug("------------------ testReadConfiguration_sinArgumentos");
        int formattersSize = 4;
        FormatterFactory factory = new FormatterFactory();
        assertEquals(formattersSize, factory.getNumberOfFormatters());
    }

    @Test
    public void testReadConfiguration_conArgumentos() throws URISyntaxException {
        log.debug("------------------ testReadConfiguration_conArgumentos");
        String configurationPath = "format.xml";
        int size = 4;
        FormatterFactory factory = new FormatterFactory(configurationPath);
        
        assertEquals(size, factory.getNumberOfFormatters());

    }

    /**
     * Test of getFormatter method, of class FormatterFactory.
     */
    @Test
    public void testGetFormatter() throws URISyntaxException {
        log.debug("------------------ testGetFormatter");
        String configurationPath = "format.xml";
        String id = "001";
        FormatterFactory instance = new FormatterFactory(configurationPath);
        Formatter result = instance.getFormatter(id);
        
        assertNotNull(result);

        id = "008";
        result = instance.getFormatter(id);
        assertNull(result);

        id = null;
        result = instance.getFormatter(id);
        assertNull(result);
    }    

}
