/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.macros;

import com.novatronic.formatter.exception.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 * @author ofernandez
 */
public class DummyMacro extends Macro{
    private static final Logger log = Logger.getLogger(DummyMacro.class);
    private SimpleDateFormat sdf;
    
    @Override
    protected void readCustomConfiguration(Element elementConfig) {
        /*No presenta configuracion especial*/
    }

    @Override
    public String parse(String... args) {
        log.debug("Parseando Fecha con formato:" + args[0]);
        if(args.length != 1){
            throw new ParseException("Argumentos invalidos");
        }
        sdf = new SimpleDateFormat(args[0]);
        return sdf.format(new Date());
    }
    
}
