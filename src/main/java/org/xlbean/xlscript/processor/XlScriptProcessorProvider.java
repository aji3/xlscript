package org.xlbean.xlscript.processor;

import org.xlbean.definition.Definition;
import org.xlbean.definition.TableDefinition;
import org.xlbean.xlscript.script.XlScriptFactory;

/**
 * Provider for XlScriptProcessor implementations.
 * 
 * @author tanikawa
 *
 */
public class XlScriptProcessorProvider {

    private XlScriptFactory scriptFactory = new XlScriptFactory();
    private XlScriptProcessor singleDefinitionProcessor = new XlScriptSingleDefinitionProcessor(scriptFactory);
    private XlScriptProcessor tableDefinitionProcessor = new XlScriptTableDefinitionProcessor(scriptFactory);

    public XlScriptProcessor getProcessor(Definition definition) {
        if (definition instanceof TableDefinition) {
            return tableDefinitionProcessor;
        } else {
            return singleDefinitionProcessor;
        }
    }

    public void addBaseBinding(String key, Object value) {
        scriptFactory.addBaseBinding(key, value);
    }

    public void setBaseScript(String baseScript) {
        scriptFactory.setBaseScript(baseScript);
    }

    public void setBaseInstance(Object baseInstance) {
        scriptFactory.setBaseInstance(baseInstance);
    }

}
