package org.xlbean.xlscript.processor;

import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.collections4.map.HashedMap;
import org.xlbean.XlBean;
import org.xlbean.definition.Definition;
import org.xlbean.xlscript.util.XlScript;

public abstract class AbstractXlScriptProcessor {

    abstract public void process(Definition definition, XlBean bean);

    public static final String CONTEXT_KEY_EXCEL = "$excel";
    public static final String CONTEXT_KEY_LIST_CURRENT_OBJECT = "$it";
    private static final Pattern MATCHER = Pattern.compile("`[^`]*`");

    private String baseScript;
    private Object baseInstance;

    public AbstractXlScriptProcessor() {}

    public AbstractXlScriptProcessor(String baseScript) {
        this.baseScript = baseScript;
    }

    public AbstractXlScriptProcessor(Object baseInstance) {
        this.baseInstance = baseInstance;
    }

    /**
     * Execute {@code markedScript} as Groovy script if it is surrounded by
     * backslash. (e.g. `some script here`)
     * 
     * @param markedScript
     * @param excel
     * @param listElement
     * @return
     */
    protected Object evaluateIfScript(String markedScript, XlBean excel, Map<String, Object> listElement) {
        if (!MATCHER.matcher(markedScript).matches()) {
            return markedScript;
        }
        String script = markedScript.substring(1, markedScript.length() - 1);
        System.out.println("####SCRIPT####");
        System.out.println(script);
        System.out.println(listElement);
        Map<String, Object> map = new HashedMap<>();
        map.putAll(excel);
        map.put(CONTEXT_KEY_EXCEL, excel);
        if (listElement != null) {
            map.putAll(listElement);
            map.put(CONTEXT_KEY_LIST_CURRENT_OBJECT, listElement);
        }
        Object evaluatedValue = null;
        if (baseInstance != null) {
            evaluatedValue = new XlScript(baseInstance).evaluate(script, map);
        } else if (baseScript != null) {
            evaluatedValue = new XlScript(baseScript).evaluate(script, map);
        } else {
            evaluatedValue = new XlScript().evaluate(script, map);
        }
        System.out.println("####EVALUATED####");
        System.out.println(evaluatedValue);
        return evaluatedValue;
    }
}
