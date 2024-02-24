/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.field;

import com.novatronic.formatter.Formatter;
import com.novatronic.formatter.FormatterFactory;
import com.novatronic.formatter.filter.SingletonFilters;
import com.novatronic.formatter.internal.InternalFormat;
import com.novatronic.formatter.test.IntFormatReader;
import com.novatronic.formatter.util.VariableByteBuffer;
import java.util.Map;
import org.apache.log4j.Logger;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Omar
 */
public class BmpFieldTest {
    private static Logger log = Logger.getLogger(BmpFieldTest.class);

    private static FormatterFactory factory;
    private static Formatter formatter;
    private static final String formatConfig = "formatBmp.xml";
    private static final String IFtest = "FIBmp.xml";
    private static Map<String,InternalFormat> ifs;

    public BmpFieldTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        factory = new FormatterFactory(formatConfig);
        ifs = IntFormatReader.asMap(IFtest);
    }

    @Test
    public void testConfig() {
        log.info("----------- testConfig");
        assertEquals(3, factory.getNumberOfFormatters());
    }

    /**
     * Test of putBytes method, of class BMPField.
     */
    @Test
    public void testPutBytes() {
        log.debug("-------------------- testPutBytes");
        formatter = factory.getFormatter("testPutBytes");
        VariableByteBuffer frameExp = new VariableByteBuffer(10);
        VariableByteBuffer frame;
        InternalFormat intFormat;

        intFormat = ifs.get("testPutBytes");
        log.debug("IF:" + intFormat);
        frameExp.add(new byte[]{(byte)0xE1,0x42});
        frameExp.add(new byte[6]);
        frameExp.add(new byte[]{(byte)0x80});
        frameExp.add(new byte[7]);
        frameExp.add("101234567890123   12345678123456789012345612345678901234561");

        frame = formatter.getFrames(intFormat);
        log.debug("Trama(" + frame.getLength() + ")[" + frame.toString() + "]");
        log.debug("Trama(" + frameExp.getLength() + ")[" + frameExp.toString() + "]");

        assertEquals(frameExp,frame);
    }

    /**
     * Test of readBytes method, of class BMPField.
     */
    //@Ignore
    @Test
    public void testReadBytes() {
        log.debug("-------------------- testReadBytes");
        formatter = factory.getFormatter("testReadBytes");
        VariableByteBuffer frame = new VariableByteBuffer(70);
        InternalFormat intFormatExp;
        InternalFormat intFormatRes;

        intFormatExp = ifs.get("testReadBytes");
        log.debug("IF:" + intFormatExp);
        frame.add(new byte[]{(byte)0xE0});
        frame.add(new byte[7]);
        frame.add(new byte[]{(byte)0x80});
        frame.add(new byte[7]);
        frame.add("1012345678901....61");
        intFormatRes = formatter.createInternalFormatFromFrame(frame);

        log.debug(intFormatExp);
        assertEquals(intFormatExp, intFormatRes);
    }
    
    /**
     * Test of readBytes method, of class BMPField.
     */
    @Test
    public void testReadBytesFilter() {
        log.debug("-------------------- testReadBytesFilter");
        formatter = factory.getFormatter("testReadBytesFilter");
        VariableByteBuffer frame = new VariableByteBuffer(70);
        InternalFormat intFormatExp;
        InternalFormat intFormatRes;
        String intFmtFilteredExp;
        String intFmtFilteredRes;

        intFormatExp = ifs.get("testReadBytesFilter");
        intFmtFilteredExp = "\n+>ID=NULL, VALUES=\n"
                + "+--->ID='isobmp', VALUES=\n"
                + "+------>ID='2', VALUE=[**********]\n"
                + "+------>ID='3', VALUE=[++++++]\n"
                + "+------>ID='65', VALUE=[-]\n";
        log.debug("IF:" + intFormatExp);
        log.debug("IF Filtered:" + intFormatExp);
        frame.add(new byte[]{(byte) 0xE0});
        frame.add(new byte[7]);
        frame.add(new byte[]{(byte) 0x80});
        frame.add(new byte[7]);
        frame.add("1012345678901....61");
        intFormatRes = formatter.createInternalFormatFromFrame(frame);

        intFmtFilteredRes = SingletonFilters.filter(formatter.getId(), intFormatRes);

        log.debug("IF Filtrado Esperado:" + intFmtFilteredExp);
        log.debug("IF Filtrado Resultado:" + intFmtFilteredRes);
        assertEquals(intFormatExp, intFormatRes);
        assertEquals(intFmtFilteredRes, intFmtFilteredExp);
    }
}
