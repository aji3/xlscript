package org.xlbean.xlscript.script;

import java.util.Map;

import groovy.lang.Script;

/**
 * Wrapper for GroovyShell.
 * 
 * @author tanikawa
 *
 */
public class XlScript {

    private Script script;
    private Map<String, Object> baseBindings;

    public XlScript(Script script, Map<String, Object> baseBindings) {
        this.script = script;
        this.baseBindings = baseBindings;
    }

    /**
     * Execute wrapped Script instance.
     * 
     * @param scriptStr
     * @param bindings
     * @return
     */
    public Object execute() {
        return execute(null);
    }

    /**
     * Execute wrapped Script instance with {@code bindings}.
     * 
     * @param scriptStr
     * @param bindings
     * @return
     */
    public Object execute(Map<String, Object> bindings) {
        baseBindings.forEach((key, value) -> script.setProperty(key, value));
        if (bindings != null) {
            bindings.forEach((key, value) -> script.setProperty(key, value));
        }
        return script.run();
    }

}
