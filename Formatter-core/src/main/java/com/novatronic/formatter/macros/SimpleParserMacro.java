/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.macros;

import com.novatronic.formatter.exception.ParseException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 *
 * @author ofernandez
 * @version 1.0
 * @since 1.0, 10/12/2010
 */
public class SimpleParserMacro implements ParserMacro {

    private static final Logger log = Logger.getLogger(SimpleParserMacro.class);
    private Map<String, Macro> macros;
    private static final String MACRO_PATTERN = "(\\w+)\\(([\\w, ]*)\\)$";
    private static final Pattern macroPattern = Pattern.compile(MACRO_PATTERN);

    public SimpleParserMacro() {
        macros = new TreeMap<String, Macro>();
    }

    public final void readConfiguration(Element elementConfig) {
        List<Element> macrosList = elementConfig.getChildren(Tag.MACRO);
        for (Element elemConfigMacro : macrosList) {
            Macro macro = MacroFactory.getMacro(elemConfigMacro);
            macros.put(macro.getParseName(), macro);
        }
    }

    /**
     *
     * @param lineToParse
     * @return
     * @throws ParseException
     */
    public String parse(String lineToParse) {
        log.debug("Parseando:" + lineToParse);
        Matcher matcher = macroPattern.matcher(lineToParse);
        if(!matcher.find()){
            throw new ParseException("No hay elementos por parsear");
        }
        String macroName = matcher.group(1);
        String argument = matcher.group(2);
        String[] arguments = argument.split("[, ]+");
        log.debug("Macro name=" + macroName + ", arguments=" + argument);
        Macro macro = macros.get(macroName);
        if(macro == null){
            throw new ParseException("La macro "+ macroName + " desconocida");
        }
        return macro.parse(arguments);
    }

    public Map<String, Macro> getMacros() {
        return macros;
    }

    public void setMacros(Map<String, Macro> macros) {
        this.macros = macros;
    }

    public interface Tag {

        public static final String MACRO = "macro";
    }
}
