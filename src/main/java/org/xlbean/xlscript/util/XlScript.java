package org.xlbean.xlscript.util;

import java.util.Map;

import org.codehaus.groovy.control.CompilerConfiguration;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import groovy.util.DelegatingScript;

public class XlScript {

    private String baseScript;
    private Object baseInstance;
    private GroovyShell shell;

    public XlScript() {
        shell = new GroovyShell();
    }

    public XlScript(String baseScript) {
        this.baseScript = baseScript;

        CompilerConfiguration compilerConfig = new CompilerConfiguration();
        compilerConfig.setScriptBaseClass(DelegatingScript.class.getName());
        shell = new GroovyShell(compilerConfig);
    }

    public XlScript(Object baseInstance) {
        this.baseInstance = baseInstance;

        CompilerConfiguration compilerConfig = new CompilerConfiguration();
        compilerConfig.setScriptBaseClass(DelegatingScript.class.getName());
        shell = new GroovyShell(compilerConfig);
    }

    public Object evaluate(String scriptStr) {
        return evaluate(scriptStr, null);
    }

    public Object evaluate(String scriptStr, Map<String, Object> bindings) {
        Script script = getScript(scriptStr);
        if (bindings != null) {
            bindings.forEach((key, value) -> script.setProperty(key, value));
        }
        return script.run();
    }

    private Script getScript(String scriptStr) {
        Script script;
        if (baseScript != null || baseInstance != null) {
            script = shell.parse(scriptStr);
            Object delegate = null;
            if (baseScript != null) {
                GroovyShell baseShell = new GroovyShell();
                delegate = baseShell.parse(baseScript);
            } else if (baseInstance != null) {
                delegate = baseInstance;
            }
            ((DelegatingScript) script).setDelegate(delegate);
        } else {
            script = shell.parse(scriptStr);
        }
        return script;
    }

    public Object getProperty(String key) {
        return shell.getProperty(key);
    }
}
