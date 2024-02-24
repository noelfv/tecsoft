/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.filter;

import com.novatronic.formatter.Formatter;
import com.novatronic.formatter.FormatterFactory;
import com.novatronic.formatter.exception.FilterException;
import com.novatronic.formatter.internal.InternalFormat;
import com.novatronic.formatter.test.IntFormatReader;
import com.novatronic.formatter.util.ResourceHelper;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
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
public class FiltersTest {

    private static final Logger log = Logger.getLogger(FiltersTest.class);
    private static Map<String, InternalFormat> ifs;
    private static final String DEFAULT_CONFIG = "formatFilter.xml";
    private static final String IFtest = "FIFilter.xml";
    private static Element filtersConfig;
    private static FormatterFactory factory;

    public FiltersTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        ifs = IntFormatReader.asMap(IFtest);
        URL urlFormat = ResourceHelper.findResource(DEFAULT_CONFIG);
        Document doc = (new SAXBuilder()).build(urlFormat);
        filtersConfig = doc.getRootElement().getChild("filters");
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
     * Test of readConfiguration method, of class Filters.
     */
    @Test
    public void testReadConfiguration() {
        log.debug("-------------- testReadConfiguration - BEGIN");
        try {
            Filters filters = new Filters();
            filters.readConfiguration(filtersConfig);
            assertTrue(true);
        } catch (Exception ex) {
            log.error("error en la configuracion", ex);
            fail();
        }
        log.debug("-------------- testReadConfiguration - END");
    }

    @Test
    public void testFilterConfig() {
        log.debug("-------------- testFilterConfig - BEGIN");
        Filters filters = new Filters();
        filters.readConfiguration(filtersConfig);
        int filtersSize = 3;
        assertEquals(filtersSize, filters.getFilterAsList().size());
        log.debug("-------------- testFilterConfig - END");
    }

    /**
     * Test of addFilter method, of class Filters.
     */
    @Test
    public void testAddFilter() {
        log.debug("-------------- testAddFilter - BEGIN");
        String formatId = "fmt";
        String path = "01.01.01";
        Filters filters = new Filters();
        filters.readConfiguration(filtersConfig);

        int formatterFilterSize = 1;
        Map<String, Filter> formatterFilters;

        filters.addFilter(formatId, path, "HIDDING");

        formatterFilters = filters.getFiltersByFormatterId("fmt");

        assertEquals(formatterFilterSize, formatterFilters.size());
        assertEquals(filters.getFilter("HIDDING"), formatterFilters.get("01.01.01"));
        log.debug("-------------- testAddFilter - END");
    }

    /**
     * Test of addFilter method, of class Filters.
     */
    @Test
    public void testAddFilterUnknown() {
        log.debug("-------------- testAddFilterUnknown - BEGIN");
        String formatId = "fmt";
        String path = "01.01.01";
        Filters filters = new Filters();
        filters.readConfiguration(filtersConfig);

        try {
            filters.addFilter(formatId, path, "HIDDING2");
            fail();
        } catch (FilterException  e) {
            log.error("Error esperado", e);
            assert (true);
        }


        log.debug("-------------- testAddFilterUnknown - END");
    }

    @Test
    public void testFilters() {
        log.debug("-------------- testFilters - BEGIN");
        Filters filters = new Filters();
        filters.readConfiguration(filtersConfig);

        assertEquals("*******", filters.getFilter("HIDDING").filter("prueba1"));
        assertEquals("++++++++", filters.getFilter("HIDDING_PLUS").filter("prueba12"));
        assertEquals("---------", filters.getFilter("HIDDING_REST").filter("prueba123"));
        log.debug("-------------- testFilters - END");
    }

    @Test
    public void testFilter() {
        log.debug("-------------- testFilter - BEGIN");
        Formatter formatter;
        String filterExpected;
        String filterResult;
        InternalFormat intFmtExpected;
        InternalFormat intFmtResult;

        formatter = factory.getFormatter("testFilter");

        intFmtExpected = ifs.get("testFilter");
        intFmtResult = formatter.getInternalFormatFromConfig();

        filterExpected = "\n+>ID=NULL, VALUES=\n"
                + "+--->ID='00', VALUE=[000050]\n"
                + "+--->ID='01', VALUE=[********]\n"
                + "+--->ID='03', VALUE=[++++++++]\n";
        filterResult = factory.getFilters().filter(formatter.getFormatterId(), intFmtResult);

        assertEquals(filterExpected, filterResult);
        assertEquals(intFmtExpected, intFmtResult);

        log.debug("-------------- testFilter - framesResult " + formatter.getFrames(intFmtResult));
        log.debug("-------------- testFilter - END");
    }

    @Test
    public void testFilterWithGroup() {
        log.debug("-------------- testFilterWithGroup - BEGIN");
        Formatter formatter;
        String filterExpected;
        String filterResult;
        InternalFormat intFmtExpected;
        InternalFormat intFmtResult;

        formatter = factory.getFormatter("testFilterWithGroup");

        intFmtExpected = ifs.get("testFilterWithGroup");
        intFmtResult = formatter.getInternalFormatFromConfig();

        filterExpected = "\n+>ID=NULL, VALUES=\n"
                + "+--->ID='00', VALUE=[0200]\n"
                + "+--->ID='01', VALUE=[****************]\n"
                + "+--->ID='02', VALUE=[234]\n"
                + "+--->ID='04', VALUE=[5421]\n"
                + "+--->ID='06', VALUES=\n"
                + "+------>ID='01', VALUE=[qwerty]\n"
                + "+------>ID='02', VALUE=[--]\n";
        filterResult = factory.getFilters().filter(formatter.getFormatterId(), intFmtResult);

        assertEquals(filterExpected, filterResult);
        assertEquals(intFmtExpected, intFmtResult);
        
        log.debug("-------------- testFilterWithGroup - framesResult " + formatter.getFrames(intFmtResult));
        log.debug("-------------- testFilterWithGroup - END");
    }

    @Test
    public void testFilterWithUnknownFmtId() {
        log.debug("-------------- testFilterWithUnknownFmtId - BEGIN");
        Formatter formatter;
        String filterExpected;
        String filterResult;
        InternalFormat intFmtExpected;
        InternalFormat intFmtResult;

        factory = new FormatterFactory(DEFAULT_CONFIG);
        formatter = factory.getFormatter("testFilter");

        intFmtExpected = ifs.get("testFilter");
        intFmtResult = formatter.getInternalFormatFromConfig();

        filterExpected = "\n+>ID=NULL, VALUES=\n"
                + "+--->ID='00', VALUE=[000050]\n"
                + "+--->ID='01', VALUE=[FIX03   ]\n"
                + "+--->ID='03', VALUE=[12345678]\n";
        filterResult = factory.getFilters().filter("formateadorDesconocido", intFmtResult);

        assertEquals(filterExpected, filterResult);
        assertEquals(intFmtExpected, intFmtResult);

        log.debug("-------------- testFilterWithUnknownFmtId - framesResult " + formatter.getFrames(intFmtResult));
        log.debug("-------------- testFilterWithUnknownFmtId - END");
    }

    @Test
    public void testFilterWithOutFmtId() {
        log.debug("-------------- testFilterWithOutFmtId - BEGIN");
        String filterExpected;
        String filterResult;
        InternalFormat intFmtToFilter;

        intFmtToFilter = ifs.get("testFilterWithOutFmtId");

        filterExpected = "\n+>ID=NULL, VALUES=\n"
                + "+--->ID='00', VALUE=[no aplica filtro]\n"
                + "+--->ID='05', VALUE=[++++++++++++++++]\n"
                + "+--->ID='06', VALUES=\n"
                + "+------>ID='02', VALUE=[-------------------------------]\n"
                + "+--->ID='08', VALUE=[no aplica filtro]\n";
        filterResult = factory.getFilters().filter(intFmtToFilter);

        assertEquals(filterExpected, filterResult);
        log.debug("-------------- testFilterWithOutFmtId - END");
    }
    
    @Test
    public void testFilterWithFmtIdBasePath() {
        log.debug("-------------- testFilterWithFmtIdBasePath - BEGIN");
        Formatter formatter;
        String filterExpected;
        String filterResult;
        InternalFormat intFmtExpected;
        InternalFormat intFmtResult;

        SimpleDateFormat formato = new SimpleDateFormat("yyyyMMdd");
        String fechaHoy = formato.format(new Date());
        
        formatter = factory.getFormatter("testFilterWithMacroDeepField");

        intFmtExpected = ifs.get("testFilterWithFmtIdBasePath");
        intFmtExpected.setId("06");
        intFmtExpected.add("03", fechaHoy);
        
        intFmtResult = formatter.getInternalFormatFromConfig().getIFmt("06");
       
        filterExpected = "\n+>ID='06', VALUES=\n"
                + "+--->ID='01', VALUE=[qwerty]\n"
                + "+--->ID='02', VALUE=[--]\n"
                + "+--->ID='03', VALUE=[++++++++]\n";
        filterResult = factory.getFilters().filter(formatter.getFormatterId(), intFmtResult.getPath(), intFmtResult);

        assertEquals(filterExpected, filterResult);
        assertEquals(intFmtExpected, intFmtResult);

        log.debug("-------------- testFilterWithFmtIdBasePath - framesResult " + formatter.getFrames(intFmtResult));
        log.debug("-------------- testFilterWithFmtIdBasePath - END");
    }
    

    @Test
    public void testFilterWithMacro() {
        log.debug("-------------- testFilterWithMacro - BEGIN");
        Formatter formatter;
        String filterExpected;
        String filterResult;
        InternalFormat intFmtExpected;
        InternalFormat intFmtResult;

        SimpleDateFormat formato = new SimpleDateFormat("yyyyMMdd");
        String fechaHoy = formato.format(new Date());

        formatter = factory.getFormatter("testFilterWithMacro");
        intFmtExpected = ifs.get("testFilterWithMacro");
        intFmtExpected.add("03", fechaHoy);

        intFmtResult = formatter.getInternalFormatFromConfig();

        filterExpected = "\n+>ID=NULL, VALUES=\n"
                + "+--->ID='00', VALUE=[000050]\n"
                + "+--->ID='01', VALUE=[********]\n"
                + "+--->ID='03', VALUE=[++++++++]\n";
        filterResult = factory.getFilters().filter(formatter.getFormatterId(), intFmtResult);

        assertEquals(filterExpected, filterResult);
        assertEquals(intFmtExpected, intFmtResult);
        log.debug("-------------- testFilterWithMacro - framesResult " + formatter.getFrames(intFmtResult));
        log.debug("-------------- testFilterWithMacro - END");
    }

    @Test
    public void testFilterWithMacroDeepField() {
        log.debug("-------------- testFilterWithMacroDeepField - BEGIN");
        Formatter formatter;
        String filterExpected;
        String filterResult;
        InternalFormat intFmtExpected;
        InternalFormat intFmtResult;

        formatter = factory.getFormatter("testFilterWithMacroDeepField");

        SimpleDateFormat formato = new SimpleDateFormat("yyyyMMdd");
        String fechaHoy = formato.format(new Date());

        intFmtExpected = ifs.get("testFilterWithMacroDeepField");
        intFmtExpected.add("05", fechaHoy);
        intFmtExpected.add("06.03", fechaHoy);

        intFmtResult = formatter.getInternalFormatFromConfig();

        filterExpected = "\n+>ID=NULL, VALUES=\n"
                + "+--->ID='00', VALUE=[0200]\n"
                + "+--->ID='01', VALUE=[****************]\n"
                + "+--->ID='02', VALUE=[234]\n"
                + "+--->ID='04', VALUE=[5421]\n"
                + "+--->ID='05', VALUE=[++++++++]\n"
                + "+--->ID='06', VALUES=\n"
                + "+------>ID='01', VALUE=[qwerty]\n"
                + "+------>ID='02', VALUE=[--]\n"
                + "+------>ID='03', VALUE=[++++++++]\n";

        filterResult = factory.getFilters().filter(formatter.getFormatterId(), intFmtResult);

        assertEquals(filterExpected, filterResult);
        assertEquals(intFmtExpected, intFmtResult);

        log.debug("-------------- testFilterWithMacroDeepField - framesResult " + formatter.getFrames(intFmtResult));
        log.debug("-------------- testFilterWithMacroDeepField - END");
    }
}
