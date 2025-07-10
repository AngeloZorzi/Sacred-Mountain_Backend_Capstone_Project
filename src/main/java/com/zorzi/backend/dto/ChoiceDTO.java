package com.zorzi.backend.dto;

import java.util.List;

public class ChoiceDTO {
    public Long id;
    public String text;
    public Long nextSceneId;
    public Integer scoreChange = 0;
    public Integer availableAfterSeconds;
    public List<String> requiredFlags;
}
