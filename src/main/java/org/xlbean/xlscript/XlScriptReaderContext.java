package org.xlbean.xlscript;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xlbean.definition.Definition;
import org.xlbean.definition.TableDefinition;
import org.xlbean.reader.XlBeanReaderContext;
import org.xlbean.util.XlBeanFactory;
import org.xlbean.xlscript.processor.AbstractXlScriptProcessor;
import org.xlbean.xlscript.processor.XlScriptSingleDefinitionProcessor;
import org.xlbean.xlscript.processor.XlScriptTableDefinitionProcessor;

public class XlScriptReaderContext extends XlBeanReaderContext {

    private static Logger log = LoggerFactory.getLogger(XlScriptReaderContext.class);

    private XlScriptSingleDefinitionProcessor singleDefinitionProcessor;
    private XlScriptTableDefinitionProcessor tableDefinitionProcessor;

    private Map<String, Object> baseBindings = new HashMap<>();

    public XlScriptReaderContext(XlScriptSingleDefinitionProcessor singleDefinitionProcessor,
            XlScriptTableDefinitionProcessor tableDefinitionProcessor, Map<String, Object> baseBindings) {
        this.singleDefinitionProcessor = singleDefinitionProcessor;
        this.tableDefinitionProcessor = tableDefinitionProcessor;
        this.baseBindings = baseBindings;
    }

    /**
     * Set given {@code key} and {@code value} to common context which is used for
     * bindings for all {@link eval} method calls.
     * 
     * @param key
     * @param value
     */
    public void addBaseBinding(String key, Object value) {
        baseBindings.put(key, value);
    }

    public void evalAll() {
        SkipScriptOptionProcessor skipScriptOptionProcessor = new SkipScriptOptionProcessor();
        getDefinitions()
            .stream()
            .filter(skipScriptOptionProcessor::notSkip)
            .sorted(Comparator.comparing(AbstractXlScriptProcessor::getScriptOrder))
            .forEach(def -> evalInternal(def, null, getXlBean()));
    }

    public Map<String, Object> eval(String name) {
        return eval(name, null);
    }

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
        optionalMap.putAll(baseBindings);
        getProcessor(definition).process(definition, getXlBean(), optionalMap, result);
    }

    private AbstractXlScriptProcessor getProcessor(Definition definition) {
        if (definition instanceof TableDefinition) {
            return tableDefinitionProcessor;
        } else {
            return singleDefinitionProcessor;
        }
    }

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
