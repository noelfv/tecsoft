/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter;

import com.novatronic.formatter.internal.InternalFormat;
import com.novatronic.formatter.util.ResourceHelper;
import com.novatronic.formatter.util.VariableByteBuffer;
import java.net.URL;
import java.util.List;
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
 * @author ofernandez
 */
public class SimpleFormatterTest {
    private static final Logger log = Logger.getLogger(SimpleFormatterTest.class);
    private static final String CONFIGURATION_PATH = "format.xml";
    private static List<Element> formatters;

    public SimpleFormatterTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        URL url;
        
        SAXBuilder builder = new SAXBuilder();
        url = ResourceHelper.findResource(CONFIGURATION_PATH);
        Document doc = builder.build(url);
        log.debug("Leido " + doc);
        log.debug("Childs " + doc.getRootElement().getChildren());
        log.debug("Formatters " + doc.getRootElement().getChild(FormatterFactory.Tag.FORMATTERS).getChildren());
        formatters = doc.getRootElement().getChild(FormatterFactory.Tag.FORMATTERS).getChildren(FormatterFactory.Tag.FORMATTER);
        log.debug("Formatters cargados:" + formatters);
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
     * El formato usado es el "formatter id=001" ubicado en el archivo format.xml
     */
    @Test
    public void testReadConfiguration() {
        log.info("-------------------------------- testReadConfiguration");
        SimpleFormatter formatter = new SimpleFormatter();
        int posFormatter = 0;
        int fieldsExpected = 7;

        log.debug("Leyendo :" + formatters.get(posFormatter));
        formatter.readConfiguration(formatters.get(posFormatter));
        //Se valida la cantidad de campos del formateador.
        assertEquals(fieldsExpected, formatter.getSize());
    }

    @Test
    public void testGetInternalFormatFromConfig(){
        log.info("-------------------------------- testGetInternalFormatFromConfig");
        SimpleFormatter formatter = new SimpleFormatter();
        int posFormatter = 1;
        int FieldsSizeExpected = 3;
        
        log.debug("Leyendo :" + formatters.get(posFormatter));
        formatter.readConfiguration(formatters.get(posFormatter));
        InternalFormat intFormat = formatter.getInternalFormatFromConfig();        
        log.debug(intFormat.getIdsAsList());        
        log.debug(formatter.getFrames(intFormat));
        assertEquals(FieldsSizeExpected, intFormat.size());
    }

    /**
     * 
     */
    @Test
    public void testGetFrames() {
        log.info("-------------------------------- testGetFrames");
        SimpleFormatter formatter = new SimpleFormatter();
        int posFormatter = 3;
        log.debug("Leyendo :" + formatters.get(posFormatter));
        formatter.readConfiguration(formatters.get(posFormatter));
        VariableByteBuffer bb = formatter.getFrames();
        log.debug("bytes obtenidos:" + bb);
        VariableByteBuffer bbExp = new VariableByteBuffer();
        bbExp.add("12345678000050FIX03   ");
        assertEquals(bbExp, bb);
    }

}
