package org.xlbean.xlscript;

import java.util.Comparator;
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

    public XlScriptReaderContext(XlScriptSingleDefinitionProcessor singleDefinitionProcessor,
            XlScriptTableDefinitionProcessor tableDefinitionProcessor) {
        this.singleDefinitionProcessor = singleDefinitionProcessor;
        this.tableDefinitionProcessor = tableDefinitionProcessor;
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

    public void evalInternal(Definition definition, Map<String, Object> optionalMap, Map<String, Object> result) {
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
            String skipScript = definition.getOptions().get("skipScript");
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
