package org.xlbean.xlscript.processor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jparsec.internal.util.Objects;
import org.xlbean.data.value.table.TableValueLoader.ToMapOptionProcessor;
import org.xlbean.definition.Definition;
import org.xlbean.definition.TableDefinition;
import org.xlbean.util.Accessors;
import org.xlbean.util.XlBeanFactory;

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

    @Override
    @SuppressWarnings("unchecked")
    public void process(Definition definition, Map<String, Object> excel, Map<String, Object> result) {
        TableDefinition tableDefinition = (TableDefinition) definition;
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) Accessors.getValue(
            tableDefinition.getName(),
            excel);
        List<Map<String, Object>> resultDataList = (List<Map<String, Object>>) Accessors.getValue(
            tableDefinition.getName(),
            result);
        if (dataList == null) {
            return;
        }
        if (resultDataList == null) {
            resultDataList = new ArrayList<>();
            Accessors.setValue(tableDefinition.getName(), resultDataList, result, true, true, false);
        }
        ToMapOptionProcessor optionProcessor = new ToMapOptionProcessor(tableDefinition, excel);
        optionProcessor.setTargetBean(result);
        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> elem = dataList.get(i);
            Map<String, Object> resultElem = null;
            if (resultDataList.size() <= i) {
                if (elem != null) {
                    resultElem = XlBeanFactory.getInstance().createBean();
                    resultElem.putAll(elem);
                }
                resultDataList.add(resultElem);
            } else {
                resultElem = resultDataList.get(i);
            }
            eval(tableDefinition, excel, elem, result, resultElem, optionProcessor);
        }
    }

    private void eval(
            TableDefinition tableDefinition,
            Map<String, Object> originalBean,
            Map<String, Object> originalElement,
            Map<String, Object> resultBean,
            Map<String, Object> resultElement,
            ToMapOptionProcessor optionProcessor) {
        tableDefinition
            .getAttributes()
            .values()
            .stream()
            .sorted(Comparator.comparing(AbstractXlScriptProcessor::getScriptOrder))
            .forEach(columnDefinition ->
        {
                Object value = Accessors.getValue(columnDefinition.getName(), originalElement);
                if (value == null || !(value instanceof String)) {
                    return;
                }
                if (isScript((String) value)) {
                    Map<String, Object> optionalMap = new HashMap<>();
                    optionalMap.putAll(resultBean);
                    optionalMap.putAll(resultElement);
                    Object evaluatedValue = evaluate((String) value, originalBean, originalElement, optionalMap);
                    if (!Objects.equals(value, evaluatedValue)) {
                        Accessors.setValue(columnDefinition.getName(), evaluatedValue, resultElement);
                    }
                }
            });
        optionProcessor.process(resultElement);
    }

}
