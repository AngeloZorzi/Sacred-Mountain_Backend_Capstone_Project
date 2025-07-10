package com.zorzi.backend.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zorzi.backend.model.StoryState;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class StoryStateConverter implements AttributeConverter<StoryState, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(StoryState attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Errore nel serializzare storyState", e);
        }
    }

    @Override
    public StoryState convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.isBlank()) {
                return new StoryState();
            }
            return objectMapper.readValue(dbData, StoryState.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Errore nel deserializzare storyState", e);
        }
    }
}
