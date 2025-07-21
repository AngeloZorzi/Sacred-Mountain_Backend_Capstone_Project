package com.zorzi.backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.zorzi.backend.config.FlagChangesConverter;
import com.zorzi.backend.config.StringListConverter;
import jakarta.persistence.*;

import java.util.List;
import java.util.Map;

@Entity
@Table(name = "choices")
public class Choice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;

    @ManyToOne
    @JoinColumn(name = "scene_id")
    @JsonBackReference
    private Scene scene;


    @ManyToOne
    @JoinColumn(name = "next_scene_id")
    private Scene nextScene;

    @Column(nullable = false)
    private int scoreChange = 0;

    @Column
    private Integer availableAfterSeconds;

    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> requiredFlags;

    @Column(columnDefinition = "TEXT")
    @Convert(converter = FlagChangesConverter.class)
    private Map<String, Object> flagChanges;



    public Choice() {}

    public Choice(Long id, String text, Scene scene, Scene nextScene, Integer availableAfterSeconds, List<String> requiredFlags, Map<String, Object> flagChanges) {
        this.id = id;
        this.text = text;
        this.scene = scene;
        this.nextScene = nextScene;
        this.availableAfterSeconds = availableAfterSeconds;
        this.requiredFlags = requiredFlags;
        this.flagChanges = flagChanges;
    }



    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Scene getScene() { return scene; }
    public void setScene(Scene scene) { this.scene = scene; }

    public Scene getNextScene() { return nextScene; }
    public void setNextScene(Scene nextScene) { this.nextScene = nextScene; }

    public int getScoreChange() { return scoreChange; }
    public void setScoreChange(int scoreChange) { this.scoreChange = scoreChange; }

    public Integer getAvailableAfterSeconds() { return availableAfterSeconds; }
    public void setAvailableAfterSeconds(Integer availableAfterSeconds) { this.availableAfterSeconds = availableAfterSeconds; }

    public List<String> getRequiredFlags() { return requiredFlags; }
    public void setRequiredFlags(List<String> requiredFlags) { this.requiredFlags = requiredFlags; }

    public Map<String, Object> getFlagChanges() {
        return flagChanges;
    }

    public void setFlagChanges(Map<String, Object> flagChanges) {
        this.flagChanges = flagChanges;
    }
}
