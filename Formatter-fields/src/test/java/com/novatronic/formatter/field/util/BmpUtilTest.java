/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.field.util;

import com.novatronic.formatter.field.support.BitMap;
import com.novatronic.formatter.util.VariableByteBuffer;
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
public class BmpUtilTest {
    private static final Logger log  = Logger.getLogger(BmpUtilTest.class);
    public BmpUtilTest() {
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
     * Test of readBitmap method, of class BmpUtil.
     */
    @Test
    public void testReadBitmap() {
        System.out.println("readBitmap");
        BitMap bitmap;
        VariableByteBuffer frame;
        int posicion;
        boolean compress;
        
        try {
            bitmap = new BitMap(64);
            posicion = 0;
            compress = false;
            frame = new VariableByteBuffer();
            frame.add("7238848106A08000");
            BmpUtil.readBitmap(bitmap, frame, posicion, compress);
            assertEquals("{2, 3, 4, 7, 11, 12, 13, 17, 22, 25, 32, 38, 39, 41, 43, 49}", bitmap.toString());
            
        } catch (Exception ex) {
            log.error("y ahoraaaa??", ex);
        }
    }
}
