/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.test;

import com.novatronic.formatter.util.VariableByteBuffer;
import org.apache.log4j.Logger;
import static org.junit.Assert.assertEquals;
import org.junit.*;

/**
 *
 * @author Omar
 */
public class CompareFrameTest {
    private static final Logger log = Logger.getLogger(CompareFrameTest.class);
    
    public CompareFrameTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
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
     * Test of compareFrames method, of class CompareFrame.
     */
    @Test
    public void testCompareFrames_success() {
        log.debug("------------------------------ testCompareFrames_success");
        CompareFrameResult compResultExpected = new CompareFrameResult();
        CompareFrameResult compResultTested;
        VariableByteBuffer frameTested = new VariableByteBuffer();
        VariableByteBuffer frameExpected = new VariableByteBuffer();
        
        frameTested.add("cadena de prueba");
        frameExpected.add("cadena de prueba");
        compResultExpected.setSuccess(true);
        compResultExpected.setExpectedSize(frameExpected.getLength());
        compResultExpected.setTestedSize(frameTested.getLength());
        compResultExpected.setReason(CompareFrameResult.EQUALS);
        
        compResultTested = CompareFrame.compareFrames(frameTested, frameExpected);
        assertEquals(compResultExpected, compResultTested);
    }
    
    @Test
    public void testCompareFrames_fail_DifferentsPart() {
        log.debug("------------------------------ testCompareFrames_fail_DifferentsPart");
        CompareFrameResult compResultExpected = new CompareFrameResult();
        CompareFrameResult compResultTested;
        VariableByteBuffer frameTested = new VariableByteBuffer();
        VariableByteBuffer frameExpected = new VariableByteBuffer();
        
        frameTested.add("cadena de Xprueba");
        frameExpected.add("cadena de prueba");
        compResultExpected.setSuccess(false);
        compResultExpected.setExpectedSize(frameExpected.getLength());
        compResultExpected.setTestedSize(frameTested.getLength());
        compResultExpected.setReason(CompareFrameResult.DIFFERENTS_PART);
        compResultExpected.setPosition(10);
        
        compResultTested = CompareFrame.compareFrames(frameTested, frameExpected);
        assertEquals(compResultExpected, compResultTested);
    }
    
    @Test
    public void testCompareFrames_fail_DifferentsSize() {
        log.debug("------------------------------ testCompareFrames_fail_DifferentsSize");
        CompareFrameResult compResultExpected = new CompareFrameResult();
        CompareFrameResult compResultTested;
        VariableByteBuffer frameTested = new VariableByteBuffer();
        VariableByteBuffer frameExpected = new VariableByteBuffer();
        
        frameTested.add("cadena de prueba");
        frameTested.add(0);
        frameExpected.add("cadena de prueba");
        compResultExpected.setSuccess(false);
        compResultExpected.setExpectedSize(frameExpected.getLength());
        compResultExpected.setTestedSize(frameTested.getLength());
        compResultExpected.setReason(CompareFrameResult.EXPECTED_INCLUDE_IN_TESTED);
        compResultExpected.setPosition(0);
        
        compResultTested = CompareFrame.compareFrames(frameTested, frameExpected);
        assertEquals(compResultExpected, compResultTested);
    }
}
