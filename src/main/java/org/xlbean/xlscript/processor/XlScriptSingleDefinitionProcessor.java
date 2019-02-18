package org.xlbean.xlscript.processor;

import java.util.Map;

import org.xlbean.definition.Definition;
import org.xlbean.definition.SingleDefinition;
import org.xlbean.util.Accessors;

/**
 * Implementation of AbstractXlScriptProcessor for SingleDefinition.
 * 
 * @author tanikawa
 *
 */
public class XlScriptSingleDefinitionProcessor extends AbstractXlScriptProcessor {

    public XlScriptSingleDefinitionProcessor() {}

    public XlScriptSingleDefinitionProcessor(String baseScript) {
        super(baseScript);
    }

    public XlScriptSingleDefinitionProcessor(Object baseInstance) {
        super(baseInstance);
    }

    @Override
    public void process(Definition definition, Map<String, Object> excel, Map<String, Object> result) {
        SingleDefinition singleDefinition = (SingleDefinition) definition;
        Object obj = Accessors.getValue(singleDefinition.getName(), excel);
        if (obj == null) {
            return;
        }
        Object evaluatedValue = evaluateIfScript(obj.toString(), excel, null, result);
        Accessors.setValue(singleDefinition.getName(), evaluatedValue, result);
    }

}
