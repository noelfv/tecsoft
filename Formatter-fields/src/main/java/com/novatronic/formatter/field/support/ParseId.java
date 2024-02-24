/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.field.support;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

/**
 *
 * @author ofernandez
 */
public class ParseId {
    private static final Logger log  = Logger.getLogger(ParseId.class);
    private static final int KEY_GROUP_REG = 1;
    private static final int SIZE_GROUP_REG = 2;
    private static final String PATTERN_STRING = "([a-zA-Z0-9_\\-\\.]+):([0-9]+)[, ]*";
    private static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);
    
    public static List<FieldStorage> getIds(String expression) {
        List<FieldStorage> fieldsData = new ArrayList<FieldStorage>();
        Matcher matcher;
        FieldStorage data;
        String size;
        String key;
        
        log.trace("expression=[" + expression + "]");
        matcher = PATTERN.matcher(expression);
        while (matcher.find()) {
            key = matcher.group(KEY_GROUP_REG);
            size = matcher.group(SIZE_GROUP_REG);
            log.trace("Grupos.size=" + matcher.groupCount()
                    + ",g1:clave=[" + key + "]"
                    + ",g2:size=[" + size + "]");
            if(size == null || size.equals("")){
                data = new FieldStorage(key, FieldStorage.FULL);
            }else{
                data = new FieldStorage(key, Integer.parseInt(size));
            }

            fieldsData.add(data);
        }

        return fieldsData;
    }
}
