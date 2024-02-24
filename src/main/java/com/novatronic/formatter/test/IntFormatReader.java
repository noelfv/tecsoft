/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.test;

import com.novatronic.formatter.internal.InternalField;
import com.novatronic.formatter.internal.InternalFormat;
import com.novatronic.formatter.util.ResourceHelper;
import com.novatronic.formatter.util.VariableByteBuffer;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author Omar
 */
public class IntFormatReader {
    private static final Logger log = Logger.getLogger(IntFormatReader.class);
    
    public static interface Attr{
        public static final String ID = "id";
        public static final String MAP_ID = "mapId";
        public static final String IS_IF = "isIf";
    }
    public static interface Tag{
        public static final String INTFORMAT = "intformat";
        public static final String FIELD = "field";
    }
    
    /**
     * 
     * @param path
     * @return 
     */
    public static Map<String, InternalFormat> asMap(String path){
        URL UrlResource;
        
        try {
            UrlResource = ResourceHelper.findResource(path);
            Document doc = (new SAXBuilder()).build(UrlResource);
            log.debug("Documento leido, obteniendo elemento raiz");
            return getMapFromDoc(doc);
            
        } catch (Exception ex) {
            log.error("Error al leer el xml", ex);
            return null;
        }
    }
    
    private static Map<String, InternalFormat> getMapFromDoc(Document doc){
        Map<String, InternalFormat> map = new HashMap<String, InternalFormat>();
        
        try {
            Element root = doc.getRootElement();
            
            List<Element> elemIntFormat = root.getChildren(Tag.INTFORMAT);
            for (Element elemTest : elemIntFormat) {
                InternalFormat intFormat = fromDom(elemTest);
                intFormat.setId(elemTest.getAttributeValue(Attr.ID));
                map.put(elemTest.getAttributeValue(Attr.MAP_ID),intFormat);
            }
            return map;
            
        } catch (Exception ex) {
            log.error("Error al leer el xml", ex);
            return null;
        }
    }
    
    public static List<InternalFormat> asList(String path){
        URL UrlResource;
        
        try {
            UrlResource = ResourceHelper.findResource(path);
            Document doc = (new SAXBuilder()).build(UrlResource);
            log.debug("Documento leido, obteniendo elemento raiz");
            return getListFromDoc(doc);
            
        } catch (Exception ex) {
            log.error("Error al leer el xml", ex);
            return null;
        }
    }
    
    private static List<InternalFormat> getListFromDoc(Document doc){
        List<InternalFormat> list = new ArrayList<InternalFormat>();
        try {
            Element root = doc.getRootElement();
            
            List<Element> elemIntFormat = root.getChildren(Tag.INTFORMAT);
            for (Element elemTest : elemIntFormat) {
                InternalFormat intFormat = fromDom(elemTest);
                list.add(intFormat);
            }
            return list;
            
        } catch (Exception ex) {
            log.error("Error al leer el xml", ex);
            return null;
        }
    }
    
    /**
     * Este metodo recibe un objeto jdom el cual apunta al detalle de un Formato Interno 
     * en XML bajo la siguiente estructura:<br/>
     * <pre>
     * 
     * </pre>
     * @param element
     * @return 
     */
    public static InternalFormat fromDom(Element element){
        InternalField field;
        String id;
        boolean isIntFormat;
        VariableByteBuffer text;
        InternalFormat intFormat = new InternalFormat();
        
        id = element.getAttributeValue(Attr.ID);
        if (id != null){
            intFormat.setId(id);
        }
        List<Element> elemFields = element.getChildren(Tag.FIELD);
        for (Element elemField : elemFields) {
            id = elemField.getAttributeValue(Attr.ID);
            isIntFormat = Boolean.parseBoolean(elemField.getAttributeValue(Attr.IS_IF));
            if(!elemField.getChildren(Tag.FIELD).isEmpty()){
                log.debug("Procesando sub campos...");
                 field = fromDom(elemField);
            }else if(isIntFormat){
                log.debug("Campo es un IF, se crea y agrega");
                field = new InternalFormat(id);
            }else{
                log.debug("Se tiene un campo, se crea y agrega");
                text = ParseValue.parse(elemField.getText());
                field = new InternalField(id, text.toString());
            }
            intFormat.addInternalField(field);
        }
        
        return intFormat;
    }
}
