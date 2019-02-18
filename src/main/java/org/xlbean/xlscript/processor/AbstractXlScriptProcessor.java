package org.xlbean.xlscript.processor;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xlbean.definition.Definition;
import org.xlbean.xlscript.util.XlScript;

/**
 * Abstract class for XlScriptProcessors which executes XlBean values as Groovy
 * script.
 * 
 * @author tanikawa
 *
 */
public abstract class AbstractXlScriptProcessor {

    public static final String OPTION_SCRIPTORDER = "scriptOrder";
    public static final int DEFAULT_SCRIPTORDER = 1000;

    /**
     * Get a value from {@code excel} based on {@code definition}, evaluate the
     * value and set the result to {@code result}.
     * 
     * <p>
     * By default, {@link #evaluateIfScript(String, Map, Map)} is used for
     * evaluation.
     * </p>
     * 
     * @param definition
     * @param excel
     * @param result
     */
    abstract public void process(Definition definition, Map<String, Object> excel, Map<String, Object> result);

    public static final String CONTEXT_KEY_EXCEL = "$excel";
    public static final String CONTEXT_KEY_LIST_CURRENT_OBJECT = "$it";

    private static Logger log = LoggerFactory.getLogger(AbstractXlScriptProcessor.class);

    private static final Pattern MATCHER = Pattern.compile("`[^`]*`");

    private String baseScript;
    private Object baseInstance;

    public AbstractXlScriptProcessor() {}

    /**
     * Constructor to set base script which will be accessible from all scripts.
     * 
     * <p>
     * For instance, if {@code baseScript} is "def testMethod(String str) {'hello '
     * + str}", then any script can call "testMethod('test') // it will return
     * 'hello test'"
     * </p>
     * 
     * @param baseScript
     */
    public AbstractXlScriptProcessor(String baseScript) {
        this.baseScript = baseScript;
    }

    /**
     * Constructor to set base instance which will be accessible from all scripts.
     * 
     * <p>
     * For instance, if the class of the instance has "public String
     * testMethod(String str)", then scripts can call testMethod('some value').
     * </p>
     * 
     * @param baseInstance
     */
    public AbstractXlScriptProcessor(Object baseInstance) {
        this.baseInstance = baseInstance;
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
     * </ul>
     * </p>
     * 
     * @param markedScript
     * @param excel
     * @param listElement
     * @return
     */
    protected Object evaluateIfScript(String markedScript, Map<String, Object> excel, Map<String, Object> listElement,
            Map<String, Object> optional) {
        if (!MATCHER.matcher(markedScript).matches()) {
            return markedScript;
        }
        String script = markedScript.substring(1, markedScript.length() - 1);
        log.debug("Execute script: {}", script);
        Map<String, Object> map = new XlScriptBindingsBuilder().excel(excel).it(listElement).putAll(optional).build();
        Object evaluatedValue = null;
        if (baseInstance != null) {
            evaluatedValue = new XlScript(baseInstance).evaluate(script, map);
        } else if (baseScript != null) {
            evaluatedValue = new XlScript(baseScript).evaluate(script, map);
        } else {
            evaluatedValue = new XlScript().evaluate(script, map);
        }
        log.debug("Script result: {}", evaluatedValue);
        return evaluatedValue;
    }

    /**
     * Get script order from given {@code definition}.
     * 
     * If {@code definition} don't have "scriptOrder" option, then return 1000 as
     * the default value.
     * 
     * @param definition
     * @return
     */
    public static int getScriptOrder(Definition definition) {
        String sortOrder = definition.getOptions().get(OPTION_SCRIPTORDER);
        if (sortOrder == null) {
            return DEFAULT_SCRIPTORDER;
        } else {
            try {
                return Integer.parseInt(sortOrder);
            } catch (NumberFormatException e) {
                return DEFAULT_SCRIPTORDER;
            }
        }
    }

    public static Map<String, Object> createBindingsMap(Map<String, Object> excel) {
        Map<String, Object> map = new HashMap<>();
        map.putAll(excel);
        map.put(CONTEXT_KEY_EXCEL, excel);
        return map;
    }

    public static Map<String, Object> createBindingsMap(Map<String, Object> excel, Map<String, Object> it) {
        Map<String, Object> map = new HashMap<>();
        map.putAll(excel);
        map.put(CONTEXT_KEY_EXCEL, excel);
        if (it != null) {
            map.putAll(it);
            map.put(CONTEXT_KEY_LIST_CURRENT_OBJECT, it);
        }
        return map;
    }

    public static class XlScriptBindingsBuilder {
        private Map<String, Object> bindingsMap = new HashMap<>();

        public XlScriptBindingsBuilder excel(Map<String, Object> excel) {
            putAndPutAll(CONTEXT_KEY_EXCEL, excel);
            return this;
        }

        public XlScriptBindingsBuilder it(Map<String, Object> it) {
            putAndPutAll(CONTEXT_KEY_LIST_CURRENT_OBJECT, it);
            return this;
        }

        public XlScriptBindingsBuilder put(String key, Map<String, Object> map) {
            if (key == null || map == null) {
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

        private XlScriptBindingsBuilder putAndPutAll(String key, Map<String, Object> map) {
            if (key == null || map == null) {
                return this;
            }
            bindingsMap.putAll(map);
            bindingsMap.put(key, map);
            return this;
        }

        public Map<String, Object> build() {
            return bindingsMap;
        }
    }
}
