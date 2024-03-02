/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.generator;

import com.novatronic.formatter.FormatterFactory;
import org.junit.Ignore;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ofernandez
 */
public class SimpleFrameGeneratorTest {
    private static final Logger log = Logger.getLogger(SimpleFrameGeneratorTest.class);

    public SimpleFrameGeneratorTest() {
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

    @Ignore
    @Test
    public void testCreateSimpleFrameGenerator() {
        log.debug("------------ testCreateSimpleFrameGenerator");
        FrameGenerator generator = FrameGeneratorFactory.createFrameGenerator();
        int expected = 2;

        assertEquals(expected, generator.getNumberOfClients());
    }
    
    //@Ignore
    @Test
    public void testGenerarTransacciones(){
        log.debug("------------ testCreateSimpleFrameGenerator");

        FrameGenerator generator = FrameGeneratorFactory.createFrameGenerator();
        FormatterFactory factory = new FormatterFactory();
        generator.setFormatterFactory(factory);
        String path = generator.generateFrames();

        log.debug("Se genero las transaciones en la ruta:" + path);
    }



}