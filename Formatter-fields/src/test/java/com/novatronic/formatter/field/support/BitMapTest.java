/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.field.support;

import org.apache.log4j.Logger;
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
public class BitMapTest {
    private static final Logger log = Logger.getLogger(BitMapTest.class);
    
    public BitMapTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
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
     * Test of set method, of class BitMap.
     */
    @Test
    public void testSetGet() {
        log.debug("------------------------ testSetGet ------------------------");
        int index = 7;
        BitMap instance = new BitMap();
        instance.set(index);
        assertEquals(true, instance.get(index));
    }

    /**
     * Test of getByte method, of class BitMap.
     */
    @Test
    public void testGetByte() {
        log.debug("------------------------ testGetByte ------------------------");
        int initialPosition = 0;
        BitMap instance = new BitMap();
        instance.set(2);
        instance.set(4);
        int expResult = 40;
        int result = instance.getByte(initialPosition);
        
        assertEquals(expResult, result);
    }

    /**
     * Test of getNible method, of class BitMap.
     */
    @Test
    public void testGetNible() {
        log.debug("------------------------ testGetNible ------------------------");
        int initialPosition = 0;
        BitMap instance = new BitMap();
        instance.set(2);
        int expResult = 2;
        int result = instance.getNibble(initialPosition);
        
        assertEquals(expResult, result);
    }
    
    /**
     * Test of getNible method, of class BitMap.
     */
    @Test
    public void testPopulateFromByte() {
        log.debug("------------------------ testPopulateFromByte ------------------------");
        int initialPosition = 3;
        BitMap instance = new BitMap();
        byte bite = (byte)0xB2;
        instance.populateFrom(bite, initialPosition);
        
        assertEquals(0x16, instance.getByte(0));
        assertEquals(0xB2, instance.getByte(initialPosition));
        assertEquals(0x90, instance.getByte(6));
    }
    
    /**
     * Test of getNible method, of class BitMap.
     */
    @Test
    public void testPopulateFromChar() {
        log.debug("------------------------ testPopulateFromChar ------------------------");
        int initialPosition = 3;
        BitMap instance = new BitMap();
        char ch = 'A';
        instance.populateFrom(ch, initialPosition);
        
        assertEquals(0x14, instance.getByte(0));
        assertEquals(0xA0, instance.getByte(initialPosition));
        assertEquals(0x00, instance.getByte(6));
    }
}
