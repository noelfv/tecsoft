/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.novatronic.tester.validate;

import com.novatronic.formatter.Formatter;
import com.novatronic.formatter.FormatterFactory;
import com.novatronic.formatter.internal.InternalFormat;
import com.novatronic.tester.validate.exception.ValidatorConfigurationException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 * @author ofernandez
 * @version 1.0
 * @since 1.0, 29/12/2010
 */
public class RuleValidator {
    private static final Logger log = Logger.getLogger(RuleValidator.class);
    private String id;
    private String formatterInId;
    private String formatterOutId;
    private Formatter formatterIn;
    private Formatter formatterOut;
    private List<FieldValidator> fields;
    private String result;

    public interface Attr{
        public static final String ID = "id";
        public static final String FORMATTERIN_ID = "idFormatterIn";
        public static final String FORMATTEROUT_ID = "idFormatterOut";
    }

    public interface Tag{
        public static final String RULE = "rule";
        public static final String FIELD = "field";
    }

    /**
     *
     * @param element
     * @param factory
     * @throws ValidatorConfigurationException
     */
    public RuleValidator(Element element, FormatterFactory factory){
        fields = new ArrayList<FieldValidator>();
        id = element.getAttributeValue(Attr.ID);
        formatterInId = element.getAttributeValue(Attr.FORMATTERIN_ID);
        formatterOutId = element.getAttributeValue(Attr.FORMATTEROUT_ID);
        log.debug("Leimos: id=" + id +", formatterInID=" + formatterInId
                + ",formatterOutId=" + formatterOutId);

        formatterIn = factory.getFormatter(formatterInId);
        formatterOut = factory.getFormatter(formatterOutId);
        if((formatterIn == null) || (formatterOut == null)){
            throw new ValidatorConfigurationException("No pueden ser ubicados"
                    + " los fromateadores: IN=" + formatterInId + ",OUT="
                    + formatterOutId);
        }
        log.debug("Formateadores asignados, leyendo elementos");
        readFields(element.getChildren(Tag.FIELD));
    }

    private void readFields(List<Element> listOfFields){
        log.debug("Leyendo total campos=" + listOfFields.size());
        for(Element element : listOfFields){
            FieldValidator fieldValidator = new FieldValidator();
            fieldValidator.configurate(element);
            fields.add(fieldValidator);
            log.debug("Agregando fieldValidator:" + fieldValidator);
        }
    }

    public boolean applyRule(String frameIn, String frameOut){
        log.debug("aplicando regla=" + id);
        InternalFormat intFormatIn = formatterIn
                                        .createInternalFormatFromFrame(frameIn);
        InternalFormat intFormatOut = formatterIn
                                        .createInternalFormatFromFrame(frameOut);
        return compareInternalFormats(intFormatIn,intFormatOut);
    }

    private boolean compareInternalFormats(InternalFormat intFormatIn,
            InternalFormat intFormatOut){
        log.debug("Comparando campos. Total a comparar=" + fields.size());
        boolean compareResult = true;
        result= "";
        for(FieldValidator fieldValidator : fields){
            log.debug("Obteniendo campos por id=" + fieldValidator.getId());
            compareResult &= CompareField(
                    intFormatIn.getValue(fieldValidator.getId()),
                    intFormatOut.getValue(fieldValidator.getId()),
                    fieldValidator);
        }
        log.debug("Comparacion:" + compareResult + ", result=" + result);
        return compareResult;

    }

    private boolean CompareField(String valueIn, String valueOut,
            FieldValidator fieldValidator){
        log.debug("comparando valueIn=" + valueIn + ",valueOut=" + valueOut);
        if ((valueIn == null) ^ (valueOut == null)){
            result += "{id=" + fieldValidator.getId() + ",expected=[" + valueIn
                    + "],received=[" + valueOut + "]}";
            return false;
        } else if ((valueIn == null) && (valueOut == null)){
            return true;
        } else if(!valueIn.equals(valueOut)){
            result += "{id=" + fieldValidator.getId() + ",expected=[" + valueIn
                    + "],received=[" + valueOut + "]}";
            return false;
        } else{
            return true;
        }
    }

    public String getId() {
        return id;
    }

    public String getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "RuleValidator{" + "id=" + id + ",formatterInId="
                + formatterInId + ",formatterOutId=" + formatterOutId
                + ",formatterIn=" + formatterIn + ",formatterOut="
                + formatterOut + ",fields=" + fields + '}';
    }

    
}
