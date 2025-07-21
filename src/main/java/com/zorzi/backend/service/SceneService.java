package com.zorzi.backend.service;

import com.zorzi.backend.dto.ChoiceDTO;
import com.zorzi.backend.dto.SceneDTO;
import com.zorzi.backend.model.Choice;
import com.zorzi.backend.model.Scene;
import com.zorzi.backend.repository.ChoiceRepository;
import com.zorzi.backend.repository.SceneRepository;
import com.zorzi.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SceneService {

    private final SceneRepository sceneRepository;
    private final ChoiceRepository choiceRepository;
    private final UserRepository userRepository;

    public List<SceneDTO> getAllScenes() {
        return sceneRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    public SceneDTO getSceneById(Long id) {
        return sceneRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new EntityNotFoundException("Scene not found"));
    }

    public SceneDTO createScene(SceneDTO dto) {
        Scene scene = new Scene();
        scene.setTitle(dto.title);
        scene.setText(dto.text);
        scene.setImageUrl(dto.imageUrl);

        Scene saved = sceneRepository.save(scene);

        if (dto.choices != null) {
            for (ChoiceDTO choiceDTO : dto.choices) {
                Choice choice = new Choice();
                choice.setText(choiceDTO.text);
                choice.setScene(saved);
                choice.setScoreChange(choiceDTO.scoreChange != null ? choiceDTO.scoreChange : 0);
                choice.setAvailableAfterSeconds(choiceDTO.availableAfterSeconds);
                choice.setRequiredFlags(choiceDTO.requiredFlags);

                if (choiceDTO.nextSceneId != null) {
                    Scene nextScene = sceneRepository.findById(choiceDTO.nextSceneId)
                            .orElseThrow(() -> new EntityNotFoundException("Next scene not found"));
                    choice.setNextScene(nextScene);
                }

                choiceRepository.save(choice);
                scene.getChoices().add(choice);
            }
        }

        Scene updated = sceneRepository.findByIdWithChoices(saved.getId())
                .orElseThrow(() -> new EntityNotFoundException("Scene not found"));

        return mapToDTO(updated);
    }

    public void deleteScene(Long id) {
        sceneRepository.deleteById(id);
    }

    public SceneDTO updateScene(Long id, SceneDTO dto) {
        Scene scene = sceneRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Scene not found"));

        scene.setTitle(dto.title);
        scene.setText(dto.text);
        scene.setImageUrl(dto.imageUrl);

        choiceRepository.deleteAll(scene.getChoices());
        scene.getChoices().clear();

        if (dto.choices != null) {
            for (ChoiceDTO choiceDTO : dto.choices) {
                Choice choice = new Choice();
                choice.setText(choiceDTO.text);
                choice.setScene(scene);
                choice.setScoreChange(choiceDTO.scoreChange != null ? choiceDTO.scoreChange : 0);
                choice.setAvailableAfterSeconds(choiceDTO.availableAfterSeconds);
                choice.setRequiredFlags(choiceDTO.requiredFlags);

                if (choiceDTO.nextSceneId != null) {
                    Scene nextScene = sceneRepository.findById(choiceDTO.nextSceneId)
                            .orElseThrow(() -> new EntityNotFoundException("Next scene not found"));
                    choice.setNextScene(nextScene);
                }

                choiceRepository.save(choice);
                scene.getChoices().add(choice);
            }
        }

        Scene saved = sceneRepository.save(scene);
        return mapToDTO(saved);
    }

    private SceneDTO mapToDTO(Scene scene) {
        SceneDTO dto = new SceneDTO();
        dto.id = scene.getId();
        dto.title = scene.getTitle();
        dto.text = scene.getText();
        dto.imageUrl = scene.getImageUrl();
        dto.backgroundMusic = scene.getBackgroundMusic();
        dto.animationType = scene.getAnimationType();


        dto.choices = scene.getChoices().stream().map(choice -> {
            ChoiceDTO cdto = new ChoiceDTO();
            cdto.id = choice.getId();
            cdto.text = choice.getText();
            cdto.nextSceneId = choice.getNextScene() != null ? choice.getNextScene().getId() : null;
            cdto.scoreChange = choice.getScoreChange();
            cdto.availableAfterSeconds = choice.getAvailableAfterSeconds();
            cdto.requiredFlags = choice.getRequiredFlags();
            return cdto;
        }).toList();

        return dto;
    }

    public Scene patchScene(Long id, Map<String, Object> updates) {
        Scene scene = sceneRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Scene not found"));

        updates.forEach((key, value) -> {
            switch (key) {
                case "title" -> scene.setTitle((String) value);
                case "text" -> scene.setText((String) value);
                case "imageUrl" -> scene.setImageUrl((String) value);
                case "backgroundMusic" -> scene.setBackgroundMusic((String) value);
                case "animationType" -> scene.setAnimationType((String) value);
                case "isFinale" -> scene.setFinale(Boolean.parseBoolean(value.toString()));
            }
        });

        return sceneRepository.save(scene);
    }

}
