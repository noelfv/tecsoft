package com.novatronic.formatter.field;

import com.novatronic.formatter.exception.FieldValueException;
import com.novatronic.formatter.internal.InternalFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 * @author Omar Fernandez
 * @version 1.0
 * @since 1.0 @date 05 Abr. 2012
 *
 */
public class NumberFormatDecoField extends FieldDecorator {

    private static final Logger log = Logger.getLogger(NumberFormatDecoField.class);
    private String numFormat;
    private Locale locale;
    private int numDecimal;
    private boolean allowNull;
    private boolean allowBlank;
    
    private final String LOCALE_SEPARATOR = "_";
    private final int IDX_LANGUAGE = 0;
    private final int IDX_COUNTRY = 1;

    public interface Attr {

        public static final String NUM_FORMAT = "numFormat";
        public static final String LOCALE = "locale";
        public static final String N_DEC = "dec";
        public static final String ALLOW_NULL = "allowNull";
        public static final String ALLOW_BLANK = "allowBlank";
    }
    
    public interface Const {
        public static final String ZERO = "0";
    }

    /**
     * {@inheritDoc }<br><br> Este decorador debe retirar el signo decimal "." del numeral
     * manteniendo dos decimales (configurable). El resultado es redondeado de la forma
     * normal.
     */
    @Override
    protected void readDecoratorConfig(Element element) {
        numDecimal = Integer.parseInt(element.getAttributeValue(Attr.N_DEC));
        numFormat = element.getAttributeValue(Attr.NUM_FORMAT);
        locale = getLocale(element.getAttributeValue(Attr.LOCALE));
        allowNull = Boolean.parseBoolean(element.getAttributeValue(Attr.ALLOW_NULL));
        allowBlank = Boolean.parseBoolean(element.getAttributeValue(Attr.ALLOW_BLANK));
        
        log.debug("numberFormat={Locale=" + locale + ", allowNull= " + allowNull 
                + ", allowBlank=" + allowBlank +", numDecimal=" + numDecimal + 
                ", numFormat="+numFormat+"}");
    }

    private Locale getLocale(String pattern) {
        Locale local;
        if (pattern != null) {
            if (pattern.contains(LOCALE_SEPARATOR)) {
                String[] parts = pattern.split(LOCALE_SEPARATOR);
                local = new Locale(parts[IDX_LANGUAGE], parts[IDX_COUNTRY]);
                return local;
            } else {
                local = new Locale(pattern);
                return local;
            }
        } else {
            return Locale.getDefault();
        }
    }

    /**
     * {@inheritDoc }<br><br> Se extrae el valor del FI, se le retira el decimal, se le
     * redondea y finalmente se vuelve a colocar en el FI
     */
    @Override
    protected void toField(InternalFormat internalFormat) {
        Double number = null;
        Long numberWithoutDecimals;
        String value = null;
        NumberFormat numberFormat;

        try{
            value = internalFormat.getValue(getId());
            log.debug("Valor leido=" + value);
            
            if(allowNull && value == null){
                value = Const.ZERO;
            }
            
            numberFormat = getNumberFormat();
            number = ((DecimalFormat)numberFormat )
                    .parse(value)
                    .doubleValue();
            log.debug("Number obtenido=" + number);
            numberWithoutDecimals = Math.round(number * Math.pow(10, numDecimal));
            value = numberWithoutDecimals.toString();
            log.debug("Valor calculado=" + value);
            internalFormat.add(getId(), value);
        }catch(Exception ex){
            throw new FieldValueException("[" + getId() + "]-Deco. No fue posible parsear"
                    + " el formato recibido. value=" + value + ", number=" + number, ex);
        }
    }
    
    private NumberFormat getNumberFormat(){
        NumberFormat numberFormat;
        
        numberFormat = NumberFormat.getInstance(locale);
        if(numFormat != null){
            ((DecimalFormat) numberFormat).applyPattern(numFormat);
        }
        
        return numberFormat;
    }

    /**
     * Se requiere que el numero vuelva a estar en su formato original
     *
     * @param internalFormat
     */
    @Override
    protected void fromField(InternalFormat internalFormat) {
        Double number;
        String value;
        NumberFormat numberFormat;

        numberFormat = getNumberFormat();
        value = internalFormat.getValue(getId());
        LogMF.debug(log, "[id.Deco={0}]Recibido:{1}", getId(),value);
        
        if(allowBlank && value.trim().length() == 0){
            value = Const.ZERO;
        }
        
        number = Double.parseDouble(value);
        number /= Math.pow(10, numDecimal);
        value = numberFormat.format(number);
        LogMF.debug(log, "[id.Deco={0}]Generado:{1}", getId(),value);
        
        internalFormat.add(getId(), value);
    }
}
