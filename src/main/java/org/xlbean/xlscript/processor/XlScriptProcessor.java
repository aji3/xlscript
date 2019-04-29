package org.xlbean.xlscript.processor;

import java.util.Map;

import org.xlbean.definition.Definition;

public interface XlScriptProcessor {

    /**
     * Get value from {@code excel} based on {@code definition}, evaluate the value
     * and set the result to {@code result}.
     * 
     * <p>
     * By default, {@link #evaluate(String, Map, Map, Map)} is used for evaluation.
     * </p>
     * 
     * <p>
     * Key-Values in {@code optionalMap} will be added to bindings on evaluation.
     * </p>
     * 
     * @param definition
     * @param excel
     * @param optionalMap
     * @param result
     */
    void process(
            Definition definition,
            Map<String, Object> excel,
            Map<String, Object> optionalMap,
            Map<String, Object> result);
}
