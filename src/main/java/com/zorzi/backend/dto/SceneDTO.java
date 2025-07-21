package com.zorzi.backend.dto;

import java.time.Instant;
import java.util.List;

public class SceneDTO {
    public Long id;
    public String title;
    public String text;
    public String imageUrl;
    public List<ChoiceDTO> choices;
    public String backgroundMusic;
    public String animationType;
    public Instant lastSceneChange;
}
