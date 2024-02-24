/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.tester.reader;

import com.novatronic.formatter.internal.InternalField;
import com.novatronic.formatter.internal.InternalFormat;
import com.novatronic.formatter.test.IntFormatReader;
import com.novatronic.formatter.tester.beans.FormatterTest;
import com.novatronic.formatter.util.VariableByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author Omar
 */
public class Reader {
    private static final Logger log = Logger.getLogger(Reader.class);
    
    public static interface Attr{
        public static final String ID = "id";
        public static final String FORMATID = "formatid";
        public static final String DESCRIPTION = "desc";
    }
    public static interface Tag{
        public static final String TEST = "test";
        public static final String FRAME_TEST = "frame.test";
        public static final String FRAME_EXP = "frame.exp";
        public static final String INTFORMAT_TEST = "intformat.test";
        public static final String INTFORMAT_EXP = "intformat.exp";
        public static final String FIELD = "field";
    }
            
    public static List<FormatterTest> readTests(String path){
        Element root;
        
        try {
            Document doc = (new SAXBuilder()).build(path);
            log.debug("Documento leido, obteniendo elemento raiz");
            root = doc.getRootElement();
            
            return readTests(root);
            
        } catch (Exception ex) {
            log.error("Error al leer el xml", ex);
            return null;
        }
    }
    
    public static List<FormatterTest> readTests(Element root){
        FormatterTest test;
        String temp;
        VariableByteBuffer frameTemp;
        InternalFormat intFormatTemp;
        Element elemTemp;
        List<Element> domTests;
        List<FormatterTest> tests = new ArrayList<FormatterTest>();
        
        try {
            domTests = root.getChildren(Tag.TEST);
            for (Element elemTest : domTests) {
                test = new FormatterTest();
                temp = elemTest.getAttributeValue(Attr.ID);
                test.setId(temp);
                temp = elemTest.getAttributeValue(Attr.FORMATID);
                test.setFormatId(temp);
                temp = elemTest.getAttributeValue(Attr.DESCRIPTION);
                test.setDescription(temp);
                elemTemp = elemTest.getChild(Tag.FRAME_TEST);
                if(elemTemp != null){
                    frameTemp = ParseValue.parse(elemTemp.getText());
                    test.setFrameTest(frameTemp);
                }else{
                    log.warn("Tag no encontrado:" + Tag.FRAME_TEST + ", se omite");
                }
                elemTemp = elemTest.getChild(Tag.FRAME_EXP);
                if(elemTemp != null){
                    frameTemp = ParseValue.parse(elemTemp.getText());
                    test.setFrameExp(frameTemp);
                }else{
                    log.warn("Tag no encontrado:" + Tag.FRAME_EXP + ", se omite");
                }
                elemTemp = elemTest.getChild(Tag.INTFORMAT_TEST);
                if(elemTemp != null){
                    intFormatTemp = IntFormatReader.fromDom(elemTemp);
                    test.setIntFormatTest(intFormatTemp);
                }else{
                    log.warn("Tag no encontrado:" + Tag.INTFORMAT_TEST + ", se omite");
                }
                elemTemp = elemTest.getChild(Tag.INTFORMAT_EXP);
                if(elemTemp != null){
                    intFormatTemp = IntFormatReader.fromDom(elemTemp);
                    test.setIntFormatExp(intFormatTemp);
                }else{
                    log.warn("Tag no encontrado:" + Tag.INTFORMAT_EXP + ", se omite");
                }
                
                tests.add(test);
                log.debug("Test AGREGADO;" + test);
            }
            
            return tests;
            
        } catch (Exception ex) {
            log.error("Error", ex);
            return null;
        }
    }
}
