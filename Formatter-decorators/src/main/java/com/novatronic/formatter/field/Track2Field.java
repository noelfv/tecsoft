/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.field;

import com.novatronic.formatter.internal.InternalFormat;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 * @author ofernandez
 */
public class Track2Field extends FieldDecorator{
    private static final Logger log = Logger.getLogger(Track2Field.class);
    private boolean compress;
    private static final String SEP_REPL_TARGET = "=";
    private static final String SEP_REPL_REPLAC = "D";
    
    public static interface Attr{
        public static final String COMPRESS = "compress";
    }
    @Override
    protected void readDecoratorConfig(Element element) {
        compress = Boolean.parseBoolean(element.getAttributeValue(Attr.COMPRESS));
        log.debug("leido configuracion PamDeco, compress=" + compress);
    }

    @Override
    protected void toField(InternalFormat internalFormat) {
        String value;
        if(compress){
            value = internalFormat.getValue(this.getId());
            value = value.replace(SEP_REPL_TARGET, SEP_REPL_REPLAC);
            log.debug("new TRACK2=[" + value + "]");
            internalFormat.add(getId(), value);
        }
    }

    @Override
    protected void fromField(InternalFormat internalFormat) {
        String value;
        
        value = internalFormat.getValue(this.getId());
        value = value.replace(SEP_REPL_REPLAC, SEP_REPL_TARGET);
        log.debug("new TRACK2=[" + value + "]");
        internalFormat.add(getId(), value);
    }

   
   
}
