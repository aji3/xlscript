package org.xlbean.xlscript.processor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xlbean.data.value.table.TableValueLoader.ToMapOptionProcessor;
import org.xlbean.definition.Definition;
import org.xlbean.definition.TableDefinition;
import org.xlbean.util.Accessors;
import org.xlbean.util.XlBeanFactory;
import org.xlbean.xlscript.script.XlScriptFactory;

/**
 * Evaluate XlBean values loaded by TableDefinition as Groovy Script.
 * 
 * @author tanikawa
 *
 */
public class XlScriptTableDefinitionProcessor extends AbstractXlScriptProcessor {

    public static final String CONTEXT_KEY_LIST_CURRENT_OBJECT = "$it";
    public static final String CONTEXT_KEY_LIST_CURRENT_INDEX = "$index";

    private ScriptOrderOptionProcessor scriptOrderOptionProcessor = new ScriptOrderOptionProcessor();

    public XlScriptTableDefinitionProcessor(XlScriptFactory scriptProvider) {
        super(scriptProvider);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xlbean.xlscript.processor.XlScriptProcessor#process(org.xlbean.definition
     * .Definition, java.util.Map, java.util.Map, java.util.Map)
     */
    @Override
    @SuppressWarnings("unchecked")
    public void process(Definition definition,
            Map<String, Object> excel,
            Map<String, Object> optionalMap,
            Map<String, Object> result) {
        TableDefinition tableDefinition = (TableDefinition) definition;
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) Accessors.getValue(
            tableDefinition.getName(),
            excel);
        if (dataList == null) {
            return;
        }
        List<Map<String, Object>> resultDataList = (List<Map<String, Object>>) Accessors.getValue(
            tableDefinition.getName(),
            result);
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
            optionalMap.put(CONTEXT_KEY_LIST_CURRENT_OBJECT, elem);
            optionalMap.put(CONTEXT_KEY_LIST_CURRENT_INDEX, i);
            evalInternal(tableDefinition, excel, elem, result, resultElem, optionalMap, optionProcessor);
        }
    }

    private void evalInternal(
            TableDefinition tableDefinition,
            Map<String, Object> originalBean,
            Map<String, Object> originalElement,
            Map<String, Object> resultBean,
            Map<String, Object> resultElement,
            Map<String, Object> optionalMap,
            ToMapOptionProcessor optionProcessor) {
        tableDefinition
            .getAttributes()
            .values()
            .stream()
            .sorted(Comparator.comparing(scriptOrderOptionProcessor::getScriptOrder))
            .forEach(columnDefinition ->
        {
                Object value = Accessors.getValue(columnDefinition.getName(), originalElement);
                if (value == null || !(value instanceof String)) {
                    return;
                }
                if (isScript((String) value)) {
                    Map<String, Object> tmpOptionalMap = new HashMap<>();
                    if (optionalMap != null) {
                        tmpOptionalMap.putAll(optionalMap);
                    }
                    tmpOptionalMap.putAll(resultBean);
                    tmpOptionalMap.putAll(resultElement);
                    Object evaluatedValue = evaluate((String) value, originalBean, tmpOptionalMap);
                    if (!value.equals(evaluatedValue)) {
                        Accessors.setValue(columnDefinition.getName(), evaluatedValue, resultElement);
                    }
                }
            });
        optionProcessor.process(resultElement);
    }

}
