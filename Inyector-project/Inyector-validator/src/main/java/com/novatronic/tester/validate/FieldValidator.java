/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.tester.validate;

import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;

/**
 *
 * @author ofernandez
 * @version 1.0
 * @since 1.0, 29/12/2010
 */
public class FieldValidator {

    private String id;
    private List<FieldValidator> fields;

    public interface Attr {

        public static final String ID = "id";
    }

    public interface Tag {

        public static final String FIELD = "field";
    }

    public void configurate(Element element) {
        id = element.getAttributeValue(Attr.ID);
        List<Element> listOfFields = element.getChildren(Tag.FIELD);
        if (listOfFields != null) {
            fields = new ArrayList<FieldValidator>();
            for (Element subElement : listOfFields) {
                FieldValidator fieldValidator = new FieldValidator();
                fieldValidator.configurate(subElement);
                fields.add(fieldValidator);
            }
        }
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "FieldValidator{" + "id=" + id + "fields=" + fields + '}';
    }

    
}
