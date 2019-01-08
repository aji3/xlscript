package org.xlbean.xlscript.processor;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.jparsec.internal.util.Objects;
import org.xlbean.XlBean;
import org.xlbean.data.value.table.TableValueLoader.ToBeanOptionProcessor;
import org.xlbean.definition.Definition;
import org.xlbean.definition.TableDefinition;
import org.xlbean.util.Accessors;

/**
 * Evaluate XlBean values loaded by TableDefinition as Groovy Script.
 * 
 * @author tanikawa
 *
 */
public class XlScriptTableDefinitionProcessor extends AbstractXlScriptProcessor {

    public XlScriptTableDefinitionProcessor() {}

    public XlScriptTableDefinitionProcessor(String baseScript) {
        super(baseScript);
    }

    public XlScriptTableDefinitionProcessor(Object baseInstance) {
        super(baseInstance);
    }

    public void process(Definition definition, XlBean excel) {
        TableDefinition tableDefinition = (TableDefinition) definition;
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> dataListForTable = (List<Map<String, Object>>) Accessors.getValue(
            tableDefinition.getName(),
            excel);
        if (dataListForTable == null) {
            return;
        }
        ToBeanOptionProcessor optionProcessor = new ToBeanOptionProcessor(tableDefinition, excel);
        dataListForTable.forEach(elem -> evaluate(tableDefinition, excel, elem, optionProcessor));
    }

    private void evaluate(TableDefinition tableDefinition, XlBean bean, Map<String, Object> element,
            ToBeanOptionProcessor optionProcessor) {
        tableDefinition
            .getAttributes()
            .values()
            .stream()
            .sorted(Comparator.comparing(AbstractXlScriptProcessor::getScriptOrder))
            .forEach(columnDefinition ->
        {
                Object value = Accessors.getValue(columnDefinition.getName(), element);
                if (value == null || !(value instanceof String)) {
                    return;
                }
                Object evaluatedValue = evaluateIfScript((String) value, bean, element);
                if (!Objects.equals(value, evaluatedValue)) {
                    Accessors.setValue(columnDefinition.getName(), evaluatedValue, element);
                }
            });
        optionProcessor.process(element);
    }

}
