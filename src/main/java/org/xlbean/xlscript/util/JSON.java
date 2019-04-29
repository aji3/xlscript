package org.xlbean.xlscript.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.xlbean.converter.ValueConverters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JSON {

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
        if (ValueConverters.canConvert(data.getClass())) {
            ret = ValueConverters.getValueConverter(data.getClass()).toString(data);
        } else if (data instanceof Map) {
            Map<Object, Object> map = new HashMap<>();
            ((Map<Object, Object>) data).entrySet().forEach(
                entry -> map.put(
                    convertToStringifyableObject(entry.getKey(), stack),
                    convertToStringifyableObject(entry.getValue(), stack)));
            ret = map;
        } else if (data instanceof List) {
            List<Object> list = new ArrayList<>();
            ((List<Object>) data).forEach(elem -> list.add(convertToStringifyableObject(elem, stack)));
            ret = list;
        } else {
            ret = "DROPPED DUE TO INCONVERTABLE OBJECT: " + data.getClass();
        }
        if (!(data instanceof String)) {
            stack.pop();
        }
        return ret;
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
