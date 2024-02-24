/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.internal;

import com.novatronic.formatter.exception.InvalidFieldTypeException;
import com.novatronic.formatter.test.IntFormatReader;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Omar
 */
public class InternalFormatTest {

    private static final Logger log = Logger.getLogger(InternalFormatTest.class);
    private static Map<String, InternalFormat> ifs;
    private static final String IFtest = "FIformat.xml";
    private InternalFormat intFormat;

    public InternalFormatTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        ifs = IntFormatReader.asMap(IFtest);
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
    public void testConstructorInternalFormatFromMap() {
        log.info("----------------- testConstrucInternalFormatFromMap");
        intFormat = new InternalFormat(getMapForTestConstructor());
        InternalFormat ifExpected = ifs.get("testConstrucInternalFormatFromMap");
        log.debug(intFormat);
        assertEquals(ifExpected, intFormat);
    }

    private Map<String, Object> getMapForTestConstructor() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("01", "1111");
        map.put("02", "22");

        Map<String, Object> submap = new LinkedHashMap<String, Object>();
        submap.put("a", "aaaaaaa");
        submap.put("b", "bbbbb");
        map.put("03", submap);

        Map<String, Object> subsubmap = new LinkedHashMap<String, Object>();
        subsubmap.put("A", "AAAA");
        subsubmap.put("B", "BB");
        submap.put("c", subsubmap);

        submap.put("d", "ddd");

        subsubmap = new LinkedHashMap<String, Object>();
        subsubmap.put("A", "AAAAAA");
        subsubmap.put("B", "BBBBBBBBB");
        submap.put("e", subsubmap);

        map.put("04", "4444");

        return map;
    }

    //TODO: Mejorar y assert para el propsito del TEST :D
    @Test
    public void testIteratorFromMap() {
        log.info("----------------- testIteratorFromMap");
        Map map = new InternalFormat(getMapForTestConstructor());
        log.debug(map);

        for (Iterator it = map.values().iterator(); it.hasNext();) {
            Object object = it.next();
            log.debug(object);
        }
        assertEquals(this, this);
    }

    /**
     * Test of addIFieldByPathFromRootFI method, of class IFUtil.
     */
    @Test
    public void testAddIFieldByPathFromRootFI() {
        log.debug("------------------------- testAddIFieldByPathFromRootFI");
        InternalFormat testIF = ifs.get("testAddIFieldByPathFromRootFI");
        InternalFormat expectedIF = ifs.get("testAddIFieldByPathFromRootFI-Exp");
        String idPath = "/05.ahora.test";
        String value = "123456";

        testIF.add(idPath, value);
        assertEquals(expectedIF, testIF);
    }

    /**
     * Se agrega campo nuevo a partir de un sub mapa interno.
     */
    @Test
    public void testAddIFieldByPathFromActualFI() {
        log.debug("------------------------- testAddIFieldByPathFromActualFI");
        InternalFormat rootIF = ifs.get("testAddIFieldByPathFromActualFI");
        InternalFormat testIF;
        InternalFormat expectedIF = ifs.get("testAddIFieldByPathFromActualFI-Exp");
        String idPath = "05.ahora.test";
        String value = "123456";

        testIF = rootIF.getIFmt("04");
        testIF.add(idPath, value);
        assertEquals(expectedIF, rootIF);
    }

    /**
     * Test of addIFieldByPathFromActualFI method, of class IFUtil.
     */
    @Test
    public void testAddIFieldByPathFromRootFI_Exist() {
        log.debug("------------------------- testAddIFieldByPathFromRootFI_Exist");
        InternalFormat rootIF = ifs.get("testAddIFieldByPathFromRootFI_Exist");
        InternalFormat expectedIF = ifs.get("testAddIFieldByPathFromRootFI_Exist-Exp");
        String idPath = "04.ahora.test";
        String value = "123456";

        rootIF.add(idPath, value);
        assertEquals(expectedIF, rootIF);
    }

    @Test
    public void testgetValue() {
        log.debug("------------------------- testgetValue");
        InternalFormat rootIF = ifs.get("testgetValue");
        String idPath;
        String expectedValue;
        String value;

        idPath = "04.03.02";
        expectedValue = "456";
        value = rootIF.getValue(idPath);
        assertEquals(expectedValue, value);
    }

    @Test
    public void testgetRootValue() {
        log.debug("------------------------- testgetRootValue");
        InternalFormat rootIF = ifs.get("testgetValue");
        String idPath;
        String expectedValue;
        String value;

        idPath = "/04.03.02";
        expectedValue = "456";
        value = rootIF.getIFmt("04").getValue(idPath);
        assertEquals(expectedValue, value);
    }

    @Test
    public void testgetRootValueNotFound() {
        log.debug("------------------------- testgetRootValueNotFound");
        InternalFormat rootIF = ifs.get("testgetValue");
        String idPath;
        String value;

        idPath = "/05.03.02";
        value = rootIF.getValue(idPath);
        assertNull(value);
    }

    @Test
    public void testgetValueException() {
        log.debug("------------------------- testgetValueException");
        InternalFormat rootIF = ifs.get("testgetValue");
        String idPath;

        idPath = "04.03";
        try {
            rootIF.getValue(idPath);
            fail("No se obtuvo la excepcion esperada");
        } catch (InvalidFieldTypeException ex) {
            log.debug(ex.getMessage());
            assertTrue(true);
        }

        idPath = "04.01.01";
        try {
            rootIF.getValue(idPath);
            fail("No se obtuvo la excepcion esperada");
        } catch (InvalidFieldTypeException ex) {
            log.debug(ex.getMessage());
            assertTrue(true);
        }
    }

    @Test
    public void testGetIFSingle() {
        log.debug("------------------------- testGetIFSingle");
        InternalFormat rootIF = ifs.get("testGetIF");
        String idPath;
        InternalFormat expectedValue;
        InternalFormat value;

        idPath = "04";
        expectedValue = ifs.get("testGetIF-exp1");
        value = rootIF.getIFmt(idPath);
        assertEquals(expectedValue, value);
    }

    @Test
    public void testGetIFPathSingleRoot() {
        log.debug("------------------------- testGetIFPathSingleRoot");
        InternalFormat rootIF = ifs.get("testGetIF");
        String idPath;
        InternalFormat expectedValue;
        InternalFormat value;

        idPath = "/04";
        expectedValue = ifs.get("testGetIF-exp1");
        value = rootIF.getIFmt("04").getIFmt(idPath);
        assertEquals(expectedValue, value);
    }

    @Test
    public void testGetIFPath() {
        log.debug("------------------------- testGetIFPath");
        InternalFormat rootIF = ifs.get("testGetIF");
        String idPath;
        InternalFormat expectedValue;
        InternalFormat value;

        idPath = "04.03.01";
        expectedValue = ifs.get("testGetIF-exp2");
        value = rootIF.getIFmt(idPath);
        assertEquals(expectedValue, value);
    }

    @Test
    public void testGetIFRootPath() {
        log.debug("------------------------- testGetIFRootPath");
        InternalFormat rootIF = ifs.get("testGetIF");
        String idPath;
        InternalFormat expectedValue;
        InternalFormat value;

        idPath = "/04.03.01";
        expectedValue = ifs.get("testGetIF-exp2");
        value = rootIF.getIFmt("04").getIFmt(idPath);
        assertEquals(expectedValue, value);
    }

    @Test
    public void testGetIFRootNotFound() {
        log.debug("------------------------- testGetIFRootNotFound");
        InternalFormat rootIF = ifs.get("testGetIF");
        String idPath;
        InternalFormat value;

        idPath = "/05.03.02";
        value = rootIF.getIFmt(idPath);
        assertNull(value);
    }

    @Test
    public void testGetIFException() {
        log.debug("------------------------- testGetIFException");
        InternalFormat rootIF = ifs.get("testGetIF");
        String idPath;

        idPath = "04.02";
        try {
            rootIF.getIFmt(idPath);
            fail("No se obtuvo la excepcion esperada");
        } catch (InvalidFieldTypeException ex) {
            log.debug(ex.getMessage());
            assertTrue(true);
        }

        idPath = "04.01.01";
        try {
            rootIF.getIFmt(idPath);
            fail("No se obtuvo la excepcion esperada");
        } catch (InvalidFieldTypeException ex) {
            log.debug(ex.getMessage());
            assertTrue(true);
        }
    }

    @Test
    public void testEntrySet() {
        log.debug("------------------------- testEntrySet");
        InternalFormat rootIF;
        InternalFormat lista;

        try {
            rootIF = ifs.get("testEntrySet");
            lista = rootIF.getIFmt("lista_cuenta");
            for (Map.Entry<String, Object> entry : lista.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                log.debug("key=[" + key + "], value=[" + value + "]");
            }
            assertTrue(true);
        } catch (Exception ex) {
            log.error(":D", ex);
            fail("Nos fallo el foreach...");
        }
    }
    
    @Test
    public void testGetPath(){
        log.debug("------------------------- testGetPath");
        InternalFormat intFmtBase;
        InternalFormat intFmtTest;
        String pathExpected;
        String pathResult;
        
        intFmtBase = ifs.get("testGetPath");
        log.debug("testGetPath.Base=" + intFmtBase);
        pathExpected = "";
        pathResult = intFmtBase.getPath();
        assertEquals(pathExpected, pathResult);
        
        pathExpected = "path01.path02";
        intFmtTest = intFmtBase.getIFmt("path01.path02");
        pathResult = intFmtTest.getPath();
        assertEquals(pathExpected, pathResult);
        
        pathExpected = "path01.path02.path03.path04.path05";
        intFmtTest = intFmtBase.getIFmt("path01.path02.path03.path04.path05");
        pathResult = intFmtTest.getPath();
        assertEquals(pathExpected, pathResult);
    }
    
    @Test
    public void testMakePath(){
        log.debug("------------------------- testMakePath");
        InternalFormat intFmtBase;
        InternalFormat intFmtTest;
        String pathExpected;
        String pathResult;
        
        intFmtBase = ifs.get("testMakePath");
        log.debug("testGetPath.Base=" + intFmtBase);
        pathExpected = "idtest";
        pathResult = intFmtBase.makePath("idtest");
        assertEquals(pathExpected, pathResult);
        
        pathExpected = "path01.path02.idtest";
        intFmtTest = intFmtBase.getIFmt("path01.path02");
        pathResult = intFmtTest.makePath("idtest");
        assertEquals(pathExpected, pathResult);
    }
}
