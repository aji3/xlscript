package org.xlbean.xlscript.processor;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.jparsec.internal.util.Objects;
import org.xlbean.XlBean;
import org.xlbean.data.value.table.TableValueLoader;
import org.xlbean.definition.Definition;
import org.xlbean.definition.SingleDefinition;
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
        dataListForTable.forEach(elem -> evaluate(tableDefinition, excel, elem));

        processToBean(tableDefinition, excel, dataListForTable);
    }

    private void evaluate(TableDefinition tableDefinition, XlBean bean, Map<String, Object> element) {
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
    }

    private void processToBean(TableDefinition tableDefinition, XlBean bean, List<Map<String, Object>> list) {
        SingleDefinition listToPropKeyOptionDefinition = null;
        SingleDefinition listToPropValueOptionDefinition = null;
        for (SingleDefinition attr : tableDefinition.getAttributes().values()) {
            if (TableValueLoader.OPTION_TOBEAN_KEY.equals(attr.getOptions().get(TableValueLoader.OPTION_TOBEAN))) {
                listToPropKeyOptionDefinition = attr;
            } else if (TableValueLoader.OPTION_TOBEAN_VALUE.equals(
                attr.getOptions().get(TableValueLoader.OPTION_TOBEAN))) {
                listToPropValueOptionDefinition = attr;
            }
        }
        if (listToPropValueOptionDefinition != null && listToPropValueOptionDefinition != null) {
            for (Map<String, Object> row : list) {
                String keyObj = Accessors.getValue(listToPropKeyOptionDefinition.getName(), row);
                Object valueObj = Accessors.getValue(listToPropValueOptionDefinition.getName(), row);
                Accessors.setValue(keyObj, valueObj, bean);
            }
        }
    }

}
