package org.xlbean.xlscript;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xlbean.definition.Definition;
import org.xlbean.reader.XlBeanReaderContext;
import org.xlbean.util.XlBeanFactory;
import org.xlbean.xlscript.processor.AbstractXlScriptProcessor.ScriptOrderOptionProcessor;
import org.xlbean.xlscript.processor.XlScriptProcessorProvider;

public class XlScriptReaderContext extends XlBeanReaderContext {

    private static Logger log = LoggerFactory.getLogger(XlScriptReaderContext.class);

    private XlScriptProcessorProvider scriptProcessorProvider = new XlScriptProcessorProvider();

    private SkipScriptOptionProcessor skipScriptOptionProcessor = new SkipScriptOptionProcessor();
    private ScriptOrderOptionProcessor scriptOrderOptionProcessor = new ScriptOrderOptionProcessor();

    public XlScriptReaderContext(XlScriptProcessorProvider scriptProcessorProvider) {
        this.scriptProcessorProvider = scriptProcessorProvider;
    }

    /**
     * Set given {@code key} and {@code value} to common context which is used for
     * bindings for all {@link eval} method calls.
     * 
     * @param key
     * @param value
     */
    public void addBaseBinding(String key, Object value) {
        scriptProcessorProvider.addBaseBinding(key, value);
    }

    /**
     * Evaluate all values in XlBean defined by the Definitions.
     * 
     * <p>
     * Iterate over all Definitions, first evaluate skipScript option and skip the
     * Definition if true, sort definitions by scriptOrder option then evaluate as
     * Groovy script.
     * </p>
     */
    public void evalAll() {
        getDefinitions()
            .stream()
            .filter(skipScriptOptionProcessor::notSkip)
            .sorted(Comparator.comparing(scriptOrderOptionProcessor::getScriptOrder))
            .forEach(def -> evalInternal(def, null, getXlBean()));
    }

    public Map<String, Object> eval(String name) {
        return eval(name, null);
    }

    /**
     * Evaluate values in XlBean defined by a Definition with {@code name}.
     * 
     * <p>
     * {@code optionalMap} will be registered to Bindings when evaluating Groovy
     * script.
     * </p>
     * 
     * @param name
     * @param optionalMap
     * @return
     */
    public Map<String, Object> eval(String name, Map<String, Object> optionalMap) {
        Definition definition = getDefinitions().toMap().get(name);
        Map<String, Object> result = XlBeanFactory.getInstance().createBean();
        if (definition == null) {
            return result;
        }
        evalInternal(definition, optionalMap, result);
        return result;
    }

    private void evalInternal(Definition definition, Map<String, Object> optionalMap, Map<String, Object> result) {
        if (optionalMap == null) {
            optionalMap = new HashMap<>();
            optionalMap.put("$context", this);
        }
        scriptProcessorProvider.getProcessor(definition).process(definition, getXlBean(), optionalMap, result);
    }

    /**
     * Process skipScript option.
     * 
     * @author tanikawa
     *
     */
    public static class SkipScriptOptionProcessor {

        public boolean notSkip(Definition definition) {
            String skipScript = definition.getOptions().getOption("skipScript");
            if (skipScript != null) {
                boolean isNotSkip = !Boolean.parseBoolean(skipScript);
                if (!isNotSkip) {
                    log.info("Skip evaluation. [{}]", definition.getName());
                }
                return isNotSkip;
            }
            return true;
        }
    }

}
