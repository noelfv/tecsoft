/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.dataacces.util;

import java.io.FileNotFoundException;
import junit.framework.TestCase;

/**
 *
 * @author nteruya
 */
public class ReadFileTest extends TestCase {
    
    public ReadFileTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    
    public void testReadFile() throws FileNotFoundException {
        ReadFile reader = new ReadFile("500010T2.txt");
        reader.processLineByLine();
    }

}
