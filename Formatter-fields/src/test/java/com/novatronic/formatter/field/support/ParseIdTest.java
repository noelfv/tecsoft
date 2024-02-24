/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.field.support;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author ofernandez
 */
public class ParseIdTest {
    private static final Logger log = Logger.getLogger(ParseIdTest.class);
    public ParseIdTest() {
    }

    /**
     * Test of getIds method, of class ParseId.
     */
    @Test
    public void testGetIds() {
        String value = "1:90, 23:20 , 34-2:1450 ,4:10, dia_hoy:0, bmp.dia.ademas:2, h:05";
        List<FieldStorage> expected = new ArrayList<FieldStorage>();
        List<FieldStorage> result;
        
        expected.add(new FieldStorage("1", 90));
        expected.add(new FieldStorage("23",20));
        expected.add(new FieldStorage("34-2", 1450));
        expected.add(new FieldStorage("4", 10));
        expected.add(new FieldStorage("dia_hoy", 0));
        expected.add(new FieldStorage("bmp.dia.ademas", 2));
        expected.add(new FieldStorage("h", 5));
        result = ParseId.getIds(value);
        assertEquals(expected, result);
    }
}
