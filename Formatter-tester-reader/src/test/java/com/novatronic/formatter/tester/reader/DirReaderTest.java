/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.tester.reader;

import org.apache.log4j.Logger;
import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;

/**
 *
 * @author Omar
 */
public class DirReaderTest {
    private static final Logger log = Logger.getLogger(DirReaderTest.class);
    
    public DirReaderTest() {
    }

    /**
     * Test of readDirectory method, of class DirReader.
     */
    @Test
    public void testReadDirectory() {
        log.info("readDirectory");
        String path = "tests";
        String filter = "format.";
        String[] expResult = new String[]{"format.test.xml"};
        
        String[] result = DirReader.readDirectory(path, filter);
        log.debug(result);
        
        assertArrayEquals(expResult, result);
    }
}
