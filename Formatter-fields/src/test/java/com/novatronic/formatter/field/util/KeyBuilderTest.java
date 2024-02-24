/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.field.util;

import com.novatronic.formatter.internal.InternalFormat;
import org.apache.log4j.Logger;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Omar
 */
public class KeyBuilderTest {
    private static final Logger log = Logger.getLogger(KeyBuilderTest.class);
    
    public KeyBuilderTest() {
    }

    /**
     * Test of getKeyValue method, of class KeyBuilder.
     */
    @Test
    public void testGenerateKeyCase() {
        log.info("-------------- testGenerateKeyCase");
        InternalFormat intFormat;
        IdItem[] ids = new IdItem[3];
        String expResult;
        String result;
        
        intFormat = getIntFormatTestGetKeyValue();
        log.debug(intFormat);
        ids[0] = new IdItem("01",new IdItem("mo",new IdItem("a")));
        ids[1] = new IdItem("02");
        ids[2] = new IdItem("05",new IdItem("b"));
        expResult = "AAA|22222|BB";
        result = KeyBuilder.generateKeyCase(intFormat,ids);
        log.debug("Key obtenido=" + result);
        assertEquals(expResult, result);
    }
    
    private InternalFormat getIntFormatTestGetKeyValue(){
        InternalFormat intFormat;
        InternalFormat innerN1;
        InternalFormat innerN2;
        
        intFormat = new InternalFormat();
        //Item 1
        innerN2 = new InternalFormat();
        innerN2.setId("mo");
        innerN2.add("a", "AAA");
        innerN1 = new InternalFormat();
        innerN1.addInternalField(innerN2);
        innerN1.setId("01");
        intFormat.addInternalField(innerN1);
        //Item 2
        intFormat.add("02", "22222");
        //Item 3
        innerN1 = new InternalFormat();
        innerN1.setId("05");
        innerN1.add("b", "BB");
        intFormat.addInternalField(innerN1);
        
        return intFormat;
    }
}
