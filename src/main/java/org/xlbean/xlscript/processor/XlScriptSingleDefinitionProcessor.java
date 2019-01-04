package org.xlbean.xlscript.processor;

import org.xlbean.XlBean;
import org.xlbean.definition.Definition;
import org.xlbean.definition.SingleDefinition;
import org.xlbean.util.Accessors;

public class XlScriptSingleDefinitionProcessor extends AbstractXlScriptProcessor {

    public XlScriptSingleDefinitionProcessor() {}

    public XlScriptSingleDefinitionProcessor(String baseScript) {
        super(baseScript);
    }

    public XlScriptSingleDefinitionProcessor(Object baseInstance) {
        super(baseInstance);
    }

    public void process(Definition definition, XlBean bean) {
        SingleDefinition singleDefinition = (SingleDefinition) definition;
        Object obj = Accessors.getValue(singleDefinition.getName(), bean);
        Object evaluatedValue = evaluateIfScript(obj.toString(), bean, null);
        Accessors.setValue(singleDefinition.getName(), evaluatedValue, bean);
    }

}