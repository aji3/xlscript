package org.xlbean.xlscript;

import java.util.Comparator;
import java.util.Map;

import org.xlbean.definition.Definition;
import org.xlbean.definition.TableDefinition;
import org.xlbean.reader.XlBeanReaderContext;
import org.xlbean.util.XlBeanFactory;
import org.xlbean.xlscript.processor.AbstractXlScriptProcessor;
import org.xlbean.xlscript.processor.XlScriptSingleDefinitionProcessor;
import org.xlbean.xlscript.processor.XlScriptTableDefinitionProcessor;

public class XlScriptReaderContext extends XlBeanReaderContext {

    private XlScriptSingleDefinitionProcessor singleDefinitionProcessor;
    private XlScriptTableDefinitionProcessor tableDefinitionProcessor;

    public XlScriptReaderContext(XlScriptSingleDefinitionProcessor singleDefinitionProcessor,
            XlScriptTableDefinitionProcessor tableDefinitionProcessor) {
        this.singleDefinitionProcessor = singleDefinitionProcessor;
        this.tableDefinitionProcessor = tableDefinitionProcessor;
    }

    public void processAll() {
        SkipScriptOptionProcessor skipScriptOptionProcessor = new SkipScriptOptionProcessor();
        getDefinitions()
            .stream()
            .filter(skipScriptOptionProcessor::notSkip)
            .sorted(Comparator.comparing(AbstractXlScriptProcessor::getScriptOrder))
            .forEach(def -> process(def, getXlBean()));
    }

    public Map<String, Object> process(String name) {
        Definition definition = getDefinitions().toMap().get(name);
        Map<String, Object> result = XlBeanFactory.getInstance().createBean();
        if (definition == null) {
            return result;
        }
        process(definition, result);
        return result;
    }

    public void process(Definition definition, Map<String, Object> result) {
        getProcessor(definition).process(definition, getXlBean(), result);
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
                return !Boolean.parseBoolean(skipScript);
            }
            return true;
        }
    }

}
