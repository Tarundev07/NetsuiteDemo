package com.atomicnorth.hrm.util;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;
import java.util.Map;

@Converter(autoApply = true)
public class JsonConverter implements AttributeConverter<Map<String, Integer>, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Integer> skillFeedback) {
        if (skillFeedback == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(skillFeedback);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting Map to JSON", e);
        }
    }

    @Override
    public Map<String, Integer> convertToEntityAttribute(String skillFeedbackJson) {
        if (skillFeedbackJson == null || skillFeedbackJson.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(skillFeedbackJson, Map.class);
        } catch (IOException e) {
            throw new RuntimeException("Error converting JSON to Map", e);
        }
    }
}

