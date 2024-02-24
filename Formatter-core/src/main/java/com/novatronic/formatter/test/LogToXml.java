/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.test;

import java.util.Stack;
import org.apache.log4j.Logger;

/**
 *
 * @author Omar
 */
public class LogToXml {

    private static final Logger log = Logger.getLogger(LogToXml.class);
    
    private static final char END_MARK = '>'; 
    private static final String END_LINE = "\n";
    private static final String ID_BEGIN = "='";
    private static final String ID_END = "',";
    private static final String VALUE_BEGIN = "[";
    private static final String VALUE_END = "]";
    
    private static final int IS_FIELD = 0;
    private static final int IS_BEGIN_INTFORMAT = 1;
    private static final int IS_END_INTFORMAT = 2;
    
    private static final String TAB = "   ";
    private static final String TAG_INTFORMAT_BEGIN = "<intformat>\n";
    private static final String TAG_INTFORMAT_END = "</intformat>";
    private static final String TAG_FIELD_BEGIN = "<field ";
    private static final String PROP_ID_BEGIN = "id=\"";
    private static final String PROP_ID_END = "\">";
    private static final String TAG_FIELD_END = "</field>";
    private static final String EMPTY = "";

    public static String toXml(String logIntFormat) {
        String xml;
        String[] list;
        Stack<String> lines;
        
        log.trace("Se recibe=[" + logIntFormat + "]");
        if(logIntFormat.equals("")){
            return EMPTY;
        }
        
        list = logIntFormat.split(END_LINE);
        lines = new Stack<String>();
        populateStack(lines, list);
        
        xml = TAG_INTFORMAT_BEGIN + readFields(lines,TAB)+ TAG_INTFORMAT_END;
        
        return xml;
    }
    
    private static void populateStack(Stack<String> lines, String[] list){
        for (int i = list.length - 1; i > 0; i--) {
            lines.add(list[i]);
        }
    }
    
    private static String readFields(Stack<String> lines, String space){
        String line = "";
        int mark;
        int markQuestion;
        
        mark = lines.peek().indexOf(END_MARK);
        while(lines.size() > 0){
            markQuestion = isField(lines.peek(), mark);
            if(markQuestion == IS_BEGIN_INTFORMAT){
                line += readIntFormat(lines, space);
            }else if (markQuestion == IS_END_INTFORMAT){
                break;
            }else{
                line += readField(lines.peek(), space) + END_LINE;
                lines.pop();
            }
        }
        return line;
    }
    
    private static int isField(String line, int mark){
        int result;
        
        result = line.indexOf(END_MARK);
        result = (result == mark) ? IS_FIELD:
                (result > mark) ? IS_BEGIN_INTFORMAT : IS_END_INTFORMAT;
        
        return result;
    }
    
    private static String readIntFormat(Stack<String> lines, String space){
        String xml = "";
        xml += readFields(lines, space + TAB) + space + TAG_FIELD_END + END_LINE;
        return xml;
    }
    
    private static String readField(String line, String space){
        String field;
        int posBegin;
        int posEnd;
        
        posBegin = line.indexOf(ID_BEGIN) + 1;
        posEnd = line.lastIndexOf(ID_END);
        field = space 
                + TAG_FIELD_BEGIN + PROP_ID_BEGIN 
                + line.substring(posBegin + 1, posEnd)
                + PROP_ID_END;
        
        posBegin = line.indexOf(VALUE_BEGIN);
        if(posBegin != -1){
            posEnd = line.lastIndexOf(VALUE_END);
            field += line.substring(posBegin + 1, posEnd) + TAG_FIELD_END;
        }
        
        return field;
    }
}
