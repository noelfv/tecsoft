package com.novatronic.formatter.field;

import com.novatronic.formatter.Formatter;
import com.novatronic.formatter.FormatterFactory;
import com.novatronic.formatter.filter.SingletonFilters;
import com.novatronic.formatter.internal.InternalFormat;
import com.novatronic.formatter.test.IntFormatReader;
import java.util.Map;
import org.apache.log4j.Logger;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class CaseFieldTest {

    private static Logger log = Logger.getLogger(CaseFieldTest.class);
    private static FormatterFactory factory;
    private static Formatter formatter;
    private static Map<String,InternalFormat> ifs;
    private static final String formatConfig = "formatCase.xml";
    private static final String IFtest = "FICase.xml";

    @BeforeClass
    public static void setUpClass() throws Exception {
        factory = new FormatterFactory(formatConfig);
        ifs = IntFormatReader.asMap(IFtest);
    }

    @Test
    public void testPutBytes() {
        log.info("----------- testPutBytes");
        formatter = factory.getFormatter("T01");
        InternalFormat intFormat;
        String tramaExpected;
        String tramaResult;

        intFormat = ifs.get("TEST-PutBytes");
        tramaExpected = "AAAABBBCC66666655555999999999";
        tramaResult = formatter.getFrames(intFormat).toString();
        log.debug(tramaResult);

        assertEquals(tramaExpected, tramaResult);
    }
    
    @Test
    public void testReadBytesCaseExist() {
        log.info("----------- testReadBytesCaseExist");
        formatter = factory.getFormatter("T01");
        InternalFormat intFormat;
        InternalFormat intFormatExpected;
        String trama;

        intFormatExpected = ifs.get("TEST-ReadBytesCaseExist");
        trama = "AAAABBBEE41141141241413413413";
        intFormat = formatter.createInternalFormatFromFrame(trama);
        log.debug(intFormat);

        assertEquals(intFormatExpected, intFormat);
    }
    
    @Test
    public void testReadBytesCaseDefault() {
        log.info("----------- testReadBytesCaseDefault");
        formatter = factory.getFormatter("T01");
        InternalFormat intFormat;
        InternalFormat intFormatExpected;
        String trama;

        intFormatExpected = ifs.get("TEST-ReadBytesCaseDefault");
        trama = "AAAAFFFEE431";
        intFormat = formatter.createInternalFormatFromFrame(trama);
        log.debug(intFormat);

        assertEquals(intFormatExpected, intFormat);
    }
    
    @Test
    public void testReadBytesCaseEmptyDefault() {
        log.info("----------- testReadBytesCaseEmptyDefault");
        formatter = factory.getFormatter("testReadBytesCaseEmptyDefault");
        InternalFormat intFormat;
        InternalFormat intFormatExpected;
        String trama;

        intFormatExpected = ifs.get("TEST-ReadBytesCaseEmptyDefault");
        trama = "CCCCFFFEE431";
        intFormat = formatter.createInternalFormatFromFrame(trama);
        log.debug("InternalFormat obtenido:"+ intFormat);

        assertEquals(intFormatExpected, intFormat);
    }
    
    @Test
    public void testReadBytesRefUp() {
        log.info("----------- testReadBytesRefUp");
        formatter = factory.getFormatter("testReadBytesRefUp");
        InternalFormat intFormat;
        InternalFormat intFormatExpected;
        String trama;

        intFormatExpected = ifs.get("TEST-ReadBytesRefUp");
        trama = "CCCCFFF11hh444422333";
        intFormat = formatter.createInternalFormatFromFrame(trama);
        log.debug("InternalFormat obtenido:"+ intFormat);

        assertEquals(intFormatExpected, intFormat);
    }
    
    @Test
    public void testReadBytesRefUpMoreThanSize() {
        log.info("----------- testReadBytesRefUpMoreThanSize");
        formatter = factory.getFormatter("testReadBytesRefUpMoreThanSize");
        InternalFormat intFormat;
        InternalFormat intFormatExpected;
        String trama;

        intFormatExpected = ifs.get("TEST-ReadBytesRefUp");
        trama = "CCCCFFF11hh444422333";
        intFormat = formatter.createInternalFormatFromFrame(trama);
        log.debug("InternalFormat obtenido:"+ intFormat);

        assertEquals(intFormatExpected, intFormat);
    }

    @Test
    public void testReadBytesException() {
        log.info("----------- testReadBytesException");
        formatter = factory.getFormatter("testReadBytesException");
        InternalFormat intFormat;
        String trama;
        
        trama = "AAAAFFFEE431";
        try {
            intFormat = formatter.createInternalFormatFromFrame(trama);
            assertTrue(false);
        } catch (Exception ex) {
            log.debug(ex.getMessage());
            assertTrue(true);
        }
    }
    
    @Test
    public void testFromConfigNoParam() {
        log.info("----------- testFromConfigNoParam");
        formatter = factory.getFormatter("testFromConfigNoParam");
        InternalFormat intFmtExpected;
        InternalFormat intFmtResult;

        intFmtExpected = ifs.get("testFromConfigNoParam");
        intFmtResult = formatter.getInternalFormatFromConfig();

        assertEquals(intFmtExpected, intFmtResult);
    }
    
    @Test
    public void testFromConfigWithParam() {
        log.info("----------- testFromConfigWithParam");
        formatter = factory.getFormatter("testFromConfigWithParam");
        InternalFormat intFmtExpected;
        InternalFormat intFmtResult;

        intFmtExpected = ifs.get("testFromConfigWithParam");
        intFmtResult = formatter.getInternalFormatFromConfig("04","AAAA|BBB|CC");

        assertEquals(intFmtExpected, intFmtResult);
    }
    
    @Test
    public void testFromConfigWithCustomParam() {
        log.info("----------- testFromConfigWithCustomParam");
        formatter = factory.getFormatter("testFromConfigWithCustomParam");
        InternalFormat intFmtExpected;
        InternalFormat intFmtResult;

        intFmtExpected = ifs.get("testFromConfigWithParam");
        intFmtResult = formatter.getInternalFormatFromConfig("caseKey","AAAA|BBB|CC");

        assertEquals(intFmtExpected, intFmtResult);
    }
    
    @Test
    public void testFromConfigWithCustomParamDefaultCase() {
        log.info("----------- testFromConfigWithCustomParamDefaultCase");
        formatter = factory.getFormatter("testFromConfigWithCustomParamDefaultCase");
        InternalFormat intFmtExpected;
        InternalFormat intFmtResult;

        intFmtExpected = ifs.get("testFromConfigWithCustomParamDefaultCase");
        intFmtResult = formatter.getInternalFormatFromConfig("caseKey","AAAA");

        assertEquals(intFmtExpected, intFmtResult);
    }
    
    
    @Test
    public void testReadBytesCaseExistFilter() {
        log.info("----------- testReadBytesCaseExistFilter");
        formatter = factory.getFormatter("T01Filter");
        InternalFormat intFormat;
        InternalFormat intFormatExpected;
        String intFmtFilteredRes;
        String intFmtFilteredExp;
        String trama;

        intFormatExpected = ifs.get("TEST-ReadBytesCaseExistFilter");
        intFmtFilteredExp = "\n+>ID=NULL, VALUES=\n"
                + "+--->ID='01', VALUE=[****]\n"
                + "+--->ID='02', VALUE=[BBB]\n"
                + "+--->ID='03', VALUE=[++]\n"
                + "+--->ID='04', VALUES=\n"
                + "+------>ID='04-1-1', VALUE=[411411]\n"
                + "+------>ID='04-1-2', VALUE=[-----]\n"
                + "+------>ID='04-1-3', VALUE=[413413413]\n";
        trama = "AAAABBBEE41141141241413413413";
        intFormat = formatter.createInternalFormatFromFrame(trama);
        intFmtFilteredRes = SingletonFilters.filter(formatter.getId(), intFormat);
        
        log.debug("IF Filtrado Esperado:" + intFmtFilteredExp);
        log.debug("IF Filtrado Resultado:" + intFmtFilteredRes);

        assertEquals(intFormatExpected, intFormat);
        assertEquals(intFmtFilteredExp, intFmtFilteredRes);
    }
    
    @Test
    public void testReadBytesCaseDefaultFilter() {
        log.info("----------- testReadBytesCaseDefaultFilter");
        formatter = factory.getFormatter("T01Filter");
        InternalFormat intFormat;
        InternalFormat intFormatExpected;
        String intFmtFilteredRes;
        String intFmtFilteredExp;
        String trama;

        intFormatExpected = ifs.get("TEST-ReadBytesCaseDefaultFilter");
        intFmtFilteredExp = "\n+>ID=NULL, VALUES=\n"
                + "+--->ID='01', VALUE=[****]\n"
                + "+--->ID='02', VALUE=[FFF]\n"
                + "+--->ID='03', VALUE=[++]\n"
                + "+--->ID='04', VALUES=\n"
                + "+------>ID='04-3-1', VALUE=[---]\n";
        trama = "AAAAFFFEE431";
        intFormat = formatter.createInternalFormatFromFrame(trama);
        intFmtFilteredRes = SingletonFilters.filter(formatter.getId(), intFormat);
        
        log.debug("IF Filtrado Esperado:" + intFmtFilteredExp);
        log.debug("IF Filtrado Resultado:" + intFmtFilteredRes);

        assertEquals(intFormatExpected, intFormat);
        assertEquals(intFmtFilteredExp, intFmtFilteredRes);
    }
    
}
