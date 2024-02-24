/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.filter;

import com.novatronic.formatter.exception.FilterConfigurationException;
import java.util.Arrays;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 * @author rcastillejo
 */
public class MaskFilter extends Filter {

    private static final Logger log = Logger.getLogger(MaskFilter.class);
    private int snfp;
    private int snlp;
    private int showNTotal;

    private interface Attr {

        String SHOW_N_FIRST_POSITION = "showNFirstPos";
        String SHOW_N_LAST_POSITION = "showNLastPos";
    }

    private interface Alias {

        String SHOW_N_FIRST_POSITION = "SNFP";
        String SHOW_N_LAST_POSITION = "SNLP";
    }

    private interface Const {

        int DEFAULT_INIT = 0;
        int DEFAULT_END = 0;
    }

    /**
     * {@inheritDoc  }
     */ 
    @Override
    protected void readCustomConfiguration(Element elementConfig) {
        snfp = getAttributeValue(elementConfig, Attr.SHOW_N_FIRST_POSITION,
                Alias.SHOW_N_FIRST_POSITION, Const.DEFAULT_INIT);
        snlp = getAttributeValue(elementConfig, Attr.SHOW_N_LAST_POSITION,
                Alias.SHOW_N_LAST_POSITION, Const.DEFAULT_END);

        showNTotal = snfp + snlp;
    }

    private int getAttributeValue(Element elementConfig, String attr, String alias, int def) {
        int value;
        String element;
        String elementAlias;

        element = elementConfig.getAttributeValue(attr);
        elementAlias = elementConfig.getAttributeValue(alias);

        element = element == null ? elementAlias : element;

        if (element == null || element.isEmpty()) {
            value = def;
        } else {
            try {
                value = Integer.parseInt(element);
            } catch (Exception e) {
                throw new FilterConfigurationException("El parametro "
                        + attr + " tiene un valor invalido", e);
            }
        }
        return value;
    }

    /**
     * {@inheritDoc  }
     */
    @Override
    public String applyFilter(String value) {
        int valueLength;
        String result;

        valueLength = value.length();
        if (valueLength > showNTotal) {
            result = format(value);
        } else {
            result = generateMask(valueLength);
        }
        log.debug("Mascara result=" + result);

        return result;
    }

    private String format(String value) {
        StringBuilder sbValue;
        String valueFiltered;

        sbValue = new StringBuilder(value);
        valueFiltered = generateMask(value.length() - showNTotal);

        return sbValue.replace(snfp, value.length() - snlp, valueFiltered).toString();
    }

    private String generateMask(int maskLength) {
        char[] mask;
        
        mask = new char[maskLength];
        Arrays.fill(mask, this.getSymbol());
        return new String(mask);
    }

}
