/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.filter;

import com.novatronic.formatter.FormatterFactory;
import com.novatronic.formatter.internal.InternalFormat;
import com.novatronic.formatter.test.IntFormatReader;
import java.util.Map;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author rcastillejo
 */
public class SingletonFiltersTest {

    private static final Logger log = Logger.getLogger(SingletonFiltersTest.class);
    private static Map<String, InternalFormat> ifs;
    private static final String DEFAULT_CONFIG = "formatSingletonFilters.xml";
    private static final String IFtest = "FISingletonFilters.xml";
    private static FormatterFactory factory;

    public SingletonFiltersTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        ifs = IntFormatReader.asMap(IFtest);
        factory = new FormatterFactory(DEFAULT_CONFIG);
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
     * Test of readFormatterFactory method, of class SingletonFilters.
     */
    @Test
    public void testConfigureFiltersError() {
        log.debug("-------------- testConfigureFiltersError - BEGIN");
        FormatterFactory factory;
        try {
            factory = new FormatterFactory("formatWithFiltersReferenceError.xml");            
            fail("Debio lanzar error");
        } catch (Exception e) {
            log.warn("Error esperado", e);
            assert (true);
        }
        log.debug("-------------- testConfigureFiltersError - END");
    }

    /**
     * Test of filter method, of class SingletonFilters.
     */
    @Test
    public void testSingletonFilter() {
        log.debug("-------------- testSingletonFilter - BEGIN");
        InternalFormat intFmt;
        String expResult;
        String result;
        
        intFmt = ifs.get("testSingletonFilter");
        expResult = "\n+>ID=NULL, VALUES=\n"
                + "+--->ID='00', VALUE=[0200]\n"
                + "+--->ID='01', VALUE=[****************]\n"
                + "+--->ID='02', VALUE=[234]\n"
                + "+--->ID='03', VALUE=[234]\n"
                + "+--->ID='04', VALUE=[5421]\n"
                + "+--->ID='05', VALUE=[+++]\n"
                + "+--->ID='06', VALUES=\n"
                + "+------>ID='01', VALUE=[qwerty]\n"
                + "+------>ID='02', VALUE=[--]\n"
                + "+------>ID='03', VALUE=[++++]\n"
                + "+--->ID='07', VALUES=\n"
                + "+------>ID='01', VALUE=[qwerty]\n"
                + "+------>ID='02', VALUE=[12]\n"
                + "+------>ID='03', VALUE=[1115]\n";

        result = SingletonFilters.filter("testSingletonFilter", intFmt);

        assertEquals(expResult, result);
        log.debug("-------------- testSingletonFilter - END");
    }
        
    /**
     * Test of filter method, of class SingletonFilters.
     */
    @Test
    public void testSingletonFilterAnonymous() {
        log.debug("-------------- testSingletonFilterAnonymous - BEGIN");
        InternalFormat intFmt;
        String expResult;
        String result;

        intFmt = ifs.get("testSingletonFilterAnonymous");
        expResult = "\n+>ID=NULL, VALUES=\n"
                + "+--->ID='00', VALUE=[0200]\n"
                + "+--->ID='01', VALUE=[****************]\n"
                + "+--->ID='02', VALUE=[234]\n"
                + "+--->ID='03', VALUE=[234]\n"
                + "+--->ID='04', VALUE=[5421]\n"
                + "+--->ID='05', VALUE=[+++]\n"
                + "+--->ID='06', VALUE=[123456]\n"
                + "+--->ID='07', VALUES=\n"
                + "+------>ID='01', VALUE=[******]\n"
                + "+------>ID='02', VALUE=[**]\n"
                + "+------>ID='03', VALUE=[****]\n";

        result = SingletonFilters.filter(intFmt);
        assertEquals(expResult, result);
        
        log.debug("-------------- testSingletonFilterAnonymous - END");
    }
        
    /**
     * Test of filter method, of class SingletonFilters.
     */
    @Test
    public void testNoFiltersReference() {
        log.debug("-------------- testNoFiltersReference - BEGIN");
        InternalFormat intFmt;
        String expResult;
        String result;
        FormatterFactory factory;
        
        factory = new FormatterFactory("formatEmptyFilters.xml");
        intFmt = ifs.get("testSingletonFilterAnonymous");
        expResult = "\n+>ID=NULL, VALUES=\n"
                + "+--->ID='00', VALUE=[0200]\n"
                + "+--->ID='01', VALUE=[FABCC28188E0D008]\n"
                + "+--->ID='02', VALUE=[234]\n"
                + "+--->ID='03', VALUE=[234]\n"
                + "+--->ID='04', VALUE=[5421]\n"
                + "+--->ID='05', VALUE=[159]\n"
                + "+--->ID='06', VALUE=[123456]\n"
                + "+--->ID='07', VALUES=\n"
                + "+------>ID='01', VALUE=[qwerty]\n"
                + "+------>ID='02', VALUE=[12]\n"
                + "+------>ID='03', VALUE=[1115]\n";

        result = SingletonFilters.filter("testNoFiltersReference", intFmt);
        assertEquals(expResult, result);
        
        log.debug("-------------- testNoFiltersReference - END");
    }
}
