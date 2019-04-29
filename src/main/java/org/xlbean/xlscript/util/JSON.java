package org.xlbean.xlscript.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.xlbean.converter.ValueConverters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import groovy.lang.GString;

public class JSON {

    private static List<Class<?>> WRAPPERCLASSES = Arrays
        .asList(
            Boolean.class,
            Byte.class,
            Character.class,
            Double.class,
            Float.class,
            Integer.class,
            Long.class,
            Short.class);

    public static String stringify(Object data) {
        Object d = convertToStringifyableObject(data, new LinkedList<>());
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false);
        mapper.configure(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS, false);
        try {
            return mapper.writeValueAsString(d);
        } catch (JsonProcessingException e) {
            if ("Infinite recursion (StackOverflowError)".equals(e.getOriginalMessage())) {
                return "Error on stringify due to infinite recursion.";
            }
            throw new IllegalArgumentException("data cannot be converted to JSON string.", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Object convertToStringifyableObject(Object data, Deque<Object> stack) {
        if (data == null) {
            return null;
        }
        if (stack.contains(data)) {
            return "DROPPED DUE TO RECURSIVE OBJECT";
        }
        if (!(data instanceof String)) {
            stack.push(data);
        }
        Object ret = null;
        if (data.getClass().isPrimitive() || WRAPPERCLASSES.contains(data.getClass())) {
            ret = data;
        } else if (data instanceof Map) {
            Map<Object, Object> map = createMap(data);
            ((Map<Object, Object>) data)
                .entrySet()
                .forEach(
                    entry -> map
                        .put(
                            convertToStringifyableObject(entry.getKey(), stack),
                            convertToStringifyableObject(entry.getValue(), stack)));
            ret = map;
        } else if (data instanceof List) {
            List<Object> list = new ArrayList<>();
            ((List<Object>) data).forEach(elem -> list.add(convertToStringifyableObject(elem, stack)));
            ret = list;
        } else if (data instanceof GString) {
            ret = data.toString();
        } else if (ValueConverters.canConvert(data.getClass())) {
            ret = ValueConverters.getValueConverter(data.getClass()).toString(data);
        } else {
            ret = "DROPPED DUE TO INCONVERTABLE OBJECT: " + data.getClass();
        }
        if (!(data instanceof String)) {
            stack.pop();
        }
        return ret;
    }
    
    private static Map<Object, Object> createMap(Object map) {
        if (map instanceof LinkedHashMap) {
            return new LinkedHashMap<>();
        } else {
            return new HashMap<>();
        }
    }

    public static Object parse(String jsonStr) {
        if (jsonStr == null) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            if (jsonStr.startsWith("{")) {
                return mapper.readValue(jsonStr, Map.class);
            } else if (jsonStr.startsWith("[")) {
                return mapper.readValue(jsonStr, List.class);
            } else {
                return "Illegal JSON string: " + jsonStr;
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Illegal json string", e);
        }
    }

}
