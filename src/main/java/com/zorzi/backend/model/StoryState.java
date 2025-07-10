package com.zorzi.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoryState {

    private Boolean corvoPresente = true;
    private Boolean hasAmuleto = false;

    private Map<String, Object> extra = new HashMap<>();

    public Object get(String key) {
        return switch (key) {
            case "corvoPresente" -> corvoPresente;
            case "hasAmuleto" -> hasAmuleto;
            default -> extra.get(key);
        };
    }

    public void set(String key, Object value) {
        switch (key) {
            case "corvoPresente" -> this.corvoPresente = castToBoolean(value);
            case "hasAmuleto" -> this.hasAmuleto = castToBoolean(value);
            default -> this.extra.put(key, value);
        }
    }

    public void setFlag(String key, boolean value) {
        set(key, value);
    }

    public boolean getFlagAsBoolean(String key, boolean defaultValue) {
        Object val = get(key);
        return (val instanceof Boolean b) ? b : defaultValue;
    }

    private Boolean castToBoolean(Object value) {
        if (value instanceof Boolean b) return b;
        if (value instanceof String s) return Boolean.parseBoolean(s);
        return false;
    }
}
