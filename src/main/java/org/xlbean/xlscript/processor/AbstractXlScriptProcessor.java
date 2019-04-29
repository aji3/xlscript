package org.xlbean.xlscript.processor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xlbean.definition.Definition;
import org.xlbean.xlscript.script.XlScriptFactory;

/**
 * Abstract class for XlScriptProcessors which executes XlBean values as Groovy
 * script.
 * 
 * @author tanikawa
 *
 */
public abstract class AbstractXlScriptProcessor implements XlScriptProcessor {

    public static final String CONTEXT_KEY_EXCEL = "$excel";

    private static Logger log = LoggerFactory.getLogger(AbstractXlScriptProcessor.class);
    private static final Pattern MATCHER = Pattern.compile("^`[^`]*`$");
    private XlScriptFactory scriptProvider;

    public AbstractXlScriptProcessor(XlScriptFactory scriptProvider) {
        this.scriptProvider = scriptProvider;
    }

    protected boolean isScript(String markedScript) {
        return trimIfScript(markedScript) != null;
    }

    /**
     * Returns actual script with "`" trimmed.
     * 
     * <p>
     * If given {@code markedScript} doesn't match script format, then it returns
     * null.
     * </p>
     * 
     * @param markedScript
     * @return
     */
    protected String trimIfScript(String markedScript) {
        if (MATCHER.matcher(markedScript).matches()) {
            return markedScript.substring(1, markedScript.length() - 1);
        } else {
            return null;
        }
    }

    /**
     * Execute {@code markedScript} as Groovy script if it is surrounded by
     * backslash. (e.g. `some script here`)
     * 
     * <p>
     * This method will set following beans to binding context of the script.
     * <ul>
     * <li>$excel: {@code excel} object given as parameter</li>
     * <li>keys in $excel: For instance, if $excel contains "testList", then it is
     * accessible from script by either "testList" or "$excel.testList"</li>
     * <li>$it: {@code listElement} object</li>
     * <li>keys in {@code listElement}</li>
     * <li>keys in {@code optional}</li>
     * </ul>
     * </p>
     * 
     * @param markedScript
     * @param excel
     * @param listElement
     * @return
     */
    protected Object evaluate(String markedScript, Map<String, Object> excel, Map<String, Object> optional) {
        String script = trimIfScript(markedScript);
        log.debug("- Script: {}", script);
        Map<String, Object> map = new XlScriptBindingsBuilder().excel(excel).putAll(optional).build();
        log.debug("-- Script context: {}", map);
        Object evaluatedValue = null;
        try {
            evaluatedValue = scriptProvider.getXlScript(script).execute(map);
        } catch (Exception e) {
            log.error("----Error occured during evaluating script");
            log.error(script);
            StringWriter writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            e.printStackTrace(printWriter);
            log.error("----EXCEPTION FOR SCRIPT----START-----------------------------");
            log.error(writer.toString());
            log.error("----EXCEPTION FOR SCRIPT----END  -----------------------------");
        }
        log.debug("Script result: {}", evaluatedValue);
        return evaluatedValue;
    }

    public static class ScriptOrderOptionProcessor {

        public static final String OPTION_SCRIPTORDER = "scriptOrder";
        public static final int DEFAULT_SCRIPTORDER = 1000;

        /**
         * Get script order from given {@code definition}.
         * 
         * If {@code definition} don't have "scriptOrder" option, then return 1000 as
         * the default value.
         * 
         * @param definition
         * @return
         */
        public int getScriptOrder(Definition definition) {
            String scriptOrder = definition.getOptions().getOption(OPTION_SCRIPTORDER);
            if (scriptOrder == null) {
                return DEFAULT_SCRIPTORDER;
            } else {
                try {
                    return Integer.parseInt(scriptOrder);
                } catch (NumberFormatException e) {
                    // ignore exception and return default value
                    log.warn(
                        "Invalid scriptOrder value. Must be integer value. [{}:{}]",
                        definition.getName(),
                        scriptOrder);
                    return DEFAULT_SCRIPTORDER;
                }
            }
        }
    }

    public static class XlScriptBindingsBuilder {
        private Map<String, Object> bindingsMap = new HashMap<>();

        public XlScriptBindingsBuilder excel(Map<String, Object> excel) {
            bindingsMap.putAll(excel);
            bindingsMap.put(CONTEXT_KEY_EXCEL, excel);
            return this;
        }

        public XlScriptBindingsBuilder put(String key, Map<String, Object> map) {
            if (key == null) {
                return this;
            }
            bindingsMap.put(key, map);
            return this;
        }

        public XlScriptBindingsBuilder putAll(Map<String, Object> map) {
            if (map == null) {
                return this;
            }
            bindingsMap.putAll(map);
            return this;
        }

        public Map<String, Object> build() {
            return bindingsMap;
        }
    }
}
