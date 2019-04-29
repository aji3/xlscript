package org.xlbean.xlscript.script;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.control.CompilerConfiguration;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import groovy.util.DelegatingScript;

/**
 * Factory for XlScript.
 * 
 * <p>
 * Concept of XlScript is a simple wrapper of Groovy Script instance. Since
 * Script instance contains logics as well as contexts, xlscript by default
 * creates XlScript instance for every execution of scripts even if scriptText
 * is exactly the same. This behavior can be changed by {@code cacheScript}
 * flag.
 * </p>
 * 
 * <p>
 * On creating XlScript instance, by default this factory uses plain GroovyShell
 * instance to parse @{@code scriptText}. But if {@code baseScript} or
 * {@code baseInstance} is set, then set them as DelegatingScript of GroovyShell
 * so that {@code scriptText} can access these base logics from the script.
 * Since only one delegate can be configured, this class expects either of the
 * base is set. If both are set, baseScript will be used.
 * </p>
 * 
 * @author tanikawa
 *
 */
public class XlScriptFactory {

    private String baseScript;
    private Object baseInstance;
    private Map<String, Object> baseBindings = new HashMap<>();

    private Map<String, GroovyShell> shellCache = new HashMap<>();
    private Map<String, XlScript> scriptCache = new HashMap<>();

    /**
     * Parse {@code scriptText} to create Script instance, wrap the script by
     * XlScript and return.
     * 
     * <p>
     * This method is calling {@link #getXlScript(String, String, boolean)} with
     * {@code sharedContextName} is null and {@code cacheScript} is false.
     * </p>
     * 
     * @param scriptText
     * @return
     */
    public XlScript getXlScript(String scriptText) {
        return getXlScript(scriptText, null, false);
    }

    /**
     * Parse {@code scriptText} to create Script instance, wrap the script by
     * XlScript and return.
     * 
     * <p>
     * This method is calling {@link #getXlScript(String, String, boolean)} with
     * given {@code sharedContextName} and {@code cacheScript} false.
     * </p>
     * 
     * @param scriptText
     * @return
     */
    public XlScript getXlScript(String scriptText, String sharedContextName) {
        return getXlScript(scriptText, sharedContextName, false);
    }

    /**
     * Parse {@code scriptText} to create Script instance, wrap the script by
     * XlScript and return.
     * 
     * <p>
     * This method is calling {@link #getXlScript(String, String, boolean)} with
     * {@code sharedContextName} null and given {@code cacheScript}.
     * </p>
     * 
     * @param scriptText
     * @return
     */
    public XlScript getXlScript(String scriptText, boolean cacheScript) {
        return getXlScript(scriptText, null, cacheScript);
    }

    /**
     * Parse {@code scriptText} to create Script instance, wrap the script by
     * XlScript and return.
     * 
     * <p>
     * {@code sharedContextName} is used to share GroovyShell instance between
     * {@code scriptText}. For the same {@code sharedContextName}, the same
     * GroovyShell instance to parse {@code scriptText}. Let's assume you have 2
     * different scripts "def val = 1" and "println val", and you want to execute
     * the second script in the same memory space as the first one. In that case,
     * invoke this method with the same {@code sharedContextName} for the different
     * scripts then the 2 XlScript instances will share the memory space so that the
     * second script will write "1" to stdout.
     * </p>
     * 
     * <p>
     * When {@code cacheScript} is true, XlScript instance for scriptText will be
     * cached and reused. You can expect around 100 times better performance by
     * caching XlScript instance, however using the same XlScript instance means
     * that subsequent execution can be impacted by the previous one so be careful
     * when enabling caching.
     * </p>
     * 
     * @param scriptText
     * @param sharedContextName
     * @param cacheScript
     * @return
     */
    public XlScript getXlScript(String scriptText, String sharedContextName, boolean cacheScript) {
        if (cacheScript) {
            XlScript script = scriptCache.get(scriptText);
            if (script != null) {
                return script;
            }
        }
        GroovyShell shell;
        if (sharedContextName != null) {
            shell = shellCache.get(sharedContextName);
            if (shell == null) {
                shell = createShell();
                shellCache.put(sharedContextName, shell);
            }
        } else {
            shell = createShell();
        }
        Script script = createScript(shell, scriptText);
        XlScript xlScript = new XlScript(script, baseBindings);
        if (cacheScript) {
            scriptCache.put(scriptText, xlScript);
        }
        return xlScript;
    }

    /**
     * Add key-value to baseBindings.
     * 
     * <p>
     * {@code baseBindings} is added to Bindings of Script instance everytime it is
     * created.
     * </p>
     * 
     * @param key
     * @param value
     */
    public void addBaseBinding(String key, Object value) {
        baseBindings.put(key, value);
    }

    public void setBaseScript(String baseScript) {
        this.baseScript = baseScript;
    }

    public void setBaseInstance(Object baseInstance) {
        this.baseInstance = baseInstance;
    }

    private GroovyShell createShell() {
        GroovyShell shell;
        if (baseInstance != null || baseScript != null) {
            CompilerConfiguration compilerConfig = new CompilerConfiguration();
            compilerConfig.setScriptBaseClass(DelegatingScript.class.getName());
            shell = new GroovyShell(compilerConfig);
        } else {
            shell = new GroovyShell();
        }
        return shell;
    }

    private Script createScript(GroovyShell shell, String scriptStr) {
        Script script;
        if (baseScript != null || baseInstance != null) {
            Object delegate = null;
            if (baseScript != null) {
                GroovyShell baseShell = new GroovyShell();
                delegate = baseShell.parse(baseScript);
            } else if (baseInstance != null) {
                delegate = baseInstance;
            }
            script = shell.parse(scriptStr);
            ((DelegatingScript) script).setDelegate(delegate);
        } else {
            script = shell.parse(scriptStr);
        }
        return script;
    }

}
