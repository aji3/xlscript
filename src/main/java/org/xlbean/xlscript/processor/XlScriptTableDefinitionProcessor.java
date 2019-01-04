package org.xlbean.xlscript.processor;

import java.util.List;
import java.util.Map;

import org.jparsec.internal.util.Objects;
import org.xlbean.XlBean;
import org.xlbean.definition.Definition;
import org.xlbean.definition.SingleDefinition;
import org.xlbean.definition.TableDefinition;
import org.xlbean.util.Accessors;

public class XlScriptTableDefinitionProcessor extends AbstractXlScriptProcessor {

    public XlScriptTableDefinitionProcessor() {}

    public XlScriptTableDefinitionProcessor(String baseScript) {
        super(baseScript);
    }

    public XlScriptTableDefinitionProcessor(Object baseInstance) {
        super(baseInstance);
    }

    public void process(Definition definition, XlBean bean) {
        TableDefinition tableDefinition = (TableDefinition) definition;
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> dataListForTable = (List<Map<String, Object>>) Accessors.getValue(
            tableDefinition.getName(),
            bean);
        dataListForTable.forEach(elem -> {
            evaluate(tableDefinition, bean, elem);

            processListToProp(tableDefinition, bean, dataListForTable);
        });
    }

    private void processListToProp(TableDefinition tableDefinition, XlBean bean, List<Map<String, Object>> list) {
        SingleDefinition listToPropKeyOptionDefinition = null;
        SingleDefinition listToPropValueOptionDefinition = null;
        for (SingleDefinition attr : tableDefinition.getAttributes().values()) {
            if ("key".equals(attr.getOptions().get("listToProp"))) {
                listToPropKeyOptionDefinition = attr;
            } else if ("value".equals(attr.getOptions().get("listToProp"))) {
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

    private void evaluate(TableDefinition tableDefinition, XlBean bean, Map<String, Object> element) {
        tableDefinition.getAttributes().values().forEach(columnDefinition -> {
            if (TableDefinition.START_MARK.equals(columnDefinition.getName())) {
                return;
            }
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

}
