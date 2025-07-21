package com.zorzi.backend.controller;

import com.zorzi.backend.dto.ChoiceDTO;
import com.zorzi.backend.model.Choice;
import com.zorzi.backend.model.Scene;
import com.zorzi.backend.repository.ChoiceRepository;
import com.zorzi.backend.repository.SceneRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/choice")
@RequiredArgsConstructor
@CrossOrigin
public class ChoiceController {

    private final ChoiceRepository choiceRepository;
    private final SceneRepository sceneRepository;

    @PostMapping
    public ResponseEntity<Choice> createChoice(@RequestBody ChoiceDTO dto) {
        Scene scene = sceneRepository.findById(dto.sceneId)
                .orElseThrow(() -> new EntityNotFoundException("Scene not found"));

        Scene nextScene = (dto.nextSceneId != null)
                ? sceneRepository.findById(dto.nextSceneId)
                .orElseThrow(() -> new EntityNotFoundException("Next scene not found"))
                : null;

        Choice choice = new Choice();
        choice.setText(dto.text);
        choice.setScene(scene);
        choice.setNextScene(nextScene);
        choice.setScoreChange(dto.scoreChange != null ? dto.scoreChange : 0);
        choice.setAvailableAfterSeconds(dto.availableAfterSeconds);
        choice.setRequiredFlags(dto.requiredFlags);

        return ResponseEntity.ok(choiceRepository.save(choice));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Choice> updateChoice(@PathVariable Long id, @RequestBody ChoiceDTO dto) {
        Choice choice = choiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Choice not found"));


        choice.setText(dto.text);
        choice.setScoreChange(dto.scoreChange != null ? dto.scoreChange : 0);
        choice.setAvailableAfterSeconds(dto.availableAfterSeconds);
        choice.setRequiredFlags(dto.requiredFlags);


        if (dto.sceneId != null) {
            Scene scene = sceneRepository.findById(dto.sceneId)
                    .orElseThrow(() -> new EntityNotFoundException("Scene not found"));
            choice.setScene(scene);
        }


        if (dto.nextSceneId != null) {
            Scene nextScene = sceneRepository.findById(dto.nextSceneId)
                    .orElseThrow(() -> new EntityNotFoundException("Next scene not found"));
            choice.setNextScene(nextScene);
        } else {
            choice.setNextScene(null);
        }

        return ResponseEntity.ok(choiceRepository.save(choice));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Choice> patchChoice(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        Choice choice = choiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Choice not found"));

        updates.forEach((key, value) -> {
            switch (key) {
                case "text" -> choice.setText((String) value);
                case "scoreChange" -> choice.setScoreChange(Integer.parseInt(value.toString()));
                case "availableAfterSeconds" -> choice.setAvailableAfterSeconds(value == null ? null : Integer.parseInt(value.toString()));
                case "requiredFlags" -> choice.setRequiredFlags((List<String>) value);
                case "sceneId" -> {
                    Long sceneId = Long.parseLong(value.toString());
                    Scene scene = sceneRepository.findById(sceneId)
                            .orElseThrow(() -> new EntityNotFoundException("Scene not found"));
                    choice.setScene(scene);
                }
                case "nextSceneId" -> {
                    if (value == null) {
                        choice.setNextScene(null);
                    } else {
                        Long nextId = Long.parseLong(value.toString());
                        Scene nextScene = sceneRepository.findById(nextId)
                                .orElseThrow(() -> new EntityNotFoundException("Next scene not found"));
                        choice.setNextScene(nextScene);
                    }
                }
            }
        });

        return ResponseEntity.ok(choiceRepository.save(choice));
    }




}
