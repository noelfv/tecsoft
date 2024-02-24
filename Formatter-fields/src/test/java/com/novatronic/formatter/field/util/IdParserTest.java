/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.field.util;

import java.util.Arrays;
import org.apache.log4j.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Omar
 */
public class IdParserTest {
    private static final Logger log = Logger.getLogger(IdParserTest.class);
    /**
     * Test of parseIds method, of class IdParser.
     */
    public IdParserTest(){
        
    }
    
    @Test
    public void testParseIds() {
        log.info("--------------- testParseIds");
        String claves = "01.mo.a,02,05.b";
        IdItem[] expResult = null;
        IdItem[] result = null;
        
        expResult = getIdItemsTest();
        result = IdParser.parseIds(claves);
        log.debug("Array=" + Arrays.asList(result));
        
        assertArrayEquals(expResult, result);
    }
    
    private IdItem[] getIdItemsTest(){
        IdItem[] ids = new IdItem[3];
        IdItem id;
        
        id = new IdItem("01", new IdItem("mo", new IdItem("a")));
        ids[0] = id;
        id = new IdItem("02");
        ids[1] = id;
        id = new IdItem("05", new IdItem("b"));
        ids[2] = id;
        
        return ids;
    }
}
