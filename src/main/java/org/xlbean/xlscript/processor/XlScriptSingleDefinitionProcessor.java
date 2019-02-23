package org.xlbean.xlscript.processor;

import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
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
    public void process(Definition definition,
            Map<String, Object> excel,
            Map<String, Object> optionalMap,
            Map<String, Object> result) {
        SingleDefinition singleDefinition = (SingleDefinition) definition;
        Object obj = Accessors.getValue(singleDefinition.getName(), excel);
        if (obj == null) {
            return;
        }
        String script = obj.toString();
        if (isScript(script)) {
            Map<String, Object> optional = new HashedMap<>();
            if (optionalMap != null) {
                optional.putAll(optionalMap);
            }
            optional.putAll(result);
            Object evaluatedValue = evaluate(script, excel, null, optional);
            Accessors.setValue(singleDefinition.getName(), evaluatedValue, result);
        }
    }

}
