package org.xlbean.xlhttp;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Jsons {

    public String stringify(Object data) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false);
        mapper.configure(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS, false);
        try {
            return mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            if ("Infinite recursion (StackOverflowError)".equals(e.getOriginalMessage())) {
                return "Error on stringify due to infinite recursion.";
            }
            throw new IllegalArgumentException("data cannot be converted to JSON string.", e);
        }
    }

    public Object parse(String jsonStr) {
        if (jsonStr == null) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            if (jsonStr.startsWith("{")) {
                return mapper.readValue(jsonStr, Map.class);
            } else if (jsonStr.startsWith("[")) {
                return mapper.readValue(jsonStr, List.class);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Illegal json string", e);
        }
        throw new IllegalArgumentException("Illegal json string");
    }

}
