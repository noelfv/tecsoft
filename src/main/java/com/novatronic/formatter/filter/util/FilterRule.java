/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.novatronic.formatter.filter.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

/**
 *
 * @author rcastillejo
 */
public class FilterRule {

    private static final Logger log = Logger.getLogger(FilterRule.class);
    private static final Map<String, String> rules;
    private static final Map<String, Pattern> patterns;
    private static final String SEPARATOR_EXP = "\\.";
    private static final String SEPARATOR_SPLIT = "\\.";

    public interface RULE {

        String NUMBER = "#";
    }

    static {
        rules = new HashMap<String, String>();
        rules.put(RULE.NUMBER, "[0-9]*");

        patterns = new HashMap<String, Pattern>();
    }

    private static String searchRuleExp(String rule) {
        if (rule != null && rules.containsKey(rule)) {
            return rules.get(rule);
        }
        return null;
    }

    public static boolean validateRule(String rule, String value) {
        Pattern pattern;

        pattern = patterns.get(searchRuleExp(rule));

        if (pattern != null) {
            Matcher matcher = pattern.matcher(value);
            if (matcher.find()) {
                return false;
            }
            return true;
        }
        return true;
    }
    
    /**
     * 
     * @param path Ej: list.#.01
     * @return Patron con la expresion. Ej: (list)\.([0-9]*)\.(01)
     */
    public static Pattern makePatternByPath(String path) {
        Pattern pattern;
        String ruleId;
        String regExp;

        log.debug("Evaluando path=" + path + " ...");
        pattern = null;
        ruleId = getRuleIdFrom(path);
        if (ruleId != null) {
            regExp = makeRegExp(ruleId, path);
            pattern = Pattern.compile(regExp);
            log.debug("Pattern compilado=" + regExp + "");
        }

        return pattern;
    }

    //TOFIX: Arreglar la evaluacion de reglas
    private static String makeRegExp(String ruleId, String path) {
        String[] ids;
        StringBuilder exps;
        String ruleExp;

        ruleExp = searchRuleExp(ruleId);
        exps = new StringBuilder('^');
        ids = path.split(SEPARATOR_SPLIT);
        for (int i = 0; i < ids.length; i++) {//list.#.01
            exps.append('(');
            if (ids[i].equals(ruleId)) {
                exps.append(ruleExp);
            } else {
                exps.append(ids[i]);
            }
            exps.append(')');
            if (i != ids.length - 1) {
                exps.append(SEPARATOR_EXP);
            }
        }
        exps.append('$');
        return exps.toString();
    }

    private static String getRuleIdFrom(String value) {
        if (value.contains(RULE.NUMBER)) {
            return RULE.NUMBER;
        }

        return null;
    }
}
