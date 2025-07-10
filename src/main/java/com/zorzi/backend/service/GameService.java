package com.zorzi.backend.service;

import com.zorzi.backend.dto.ChoiceDTO;
import com.zorzi.backend.dto.GameResponseDTO;
import com.zorzi.backend.dto.PlayerChoiceDTO;
import com.zorzi.backend.dto.SceneDTO;
import com.zorzi.backend.model.Choice;
import com.zorzi.backend.model.Scene;
import com.zorzi.backend.model.StoryState;
import com.zorzi.backend.model.User;
import com.zorzi.backend.repository.ChoiceRepository;
import com.zorzi.backend.repository.SceneRepository;
import com.zorzi.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class GameService {

    private final UserRepository userRepository;
    private final SceneRepository sceneRepository;
    private final ChoiceRepository choiceRepository;

    public GameResponseDTO processChoice(PlayerChoiceDTO dto) {

        User user = userRepository.findById(dto.userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Scene scene = sceneRepository.findById(dto.sceneId)
                .orElseThrow(() -> new EntityNotFoundException("Scene not found"));

        Choice choice = choiceRepository.findById(dto.choiceId)
                .orElseThrow(() -> new EntityNotFoundException("Choice not found"));

        if (!choice.getScene().getId().equals(scene.getId())) {
            throw new IllegalArgumentException("Choice does not belong to scene");
        }

        StoryState storyState = user.getStoryState();
        if (storyState == null) {
            storyState = new StoryState();
            user.setStoryState(storyState);
        }

        String message = "";

        if ("Segui il corvo.".equalsIgnoreCase(choice.getText())) {
            storyState.setFlag("corvoPresente", false);
            message = "Hai seguito il corvo. Ma ora è sparito...";
        }

        if ("Raccogli l'oggetto.".equalsIgnoreCase(choice.getText())) {
            storyState.setFlag("hasAmuleto", true);
            message = "Hai raccolto un oggetto misterioso.";
        }

        user.setScore((user.getScore() != null ? user.getScore() : 0) + choice.getScoreChange());
        user.setLastSceneChange(Instant.now());

        userRepository.save(user);

        Scene nextScene = choice.getNextScene();

        GameResponseDTO response = new GameResponseDTO();
        response.nextScene = nextScene != null ? mapSceneToDTO(nextScene) : null;
        response.updatedScore = user.getScore();
        response.corvoPresente = storyState.getFlagAsBoolean("corvoPresente", true);
        response.message = message;

        return response;
    }

    private SceneDTO mapSceneToDTO(Scene scene) {
        SceneDTO dto = new SceneDTO();
        dto.id = scene.getId();
        dto.title = scene.getTitle();
        dto.text = scene.getText();
        dto.imageUrl = scene.getImageUrl();
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

    public SceneDTO getSceneForUser(Long sceneId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Scene scene = sceneRepository.findById(sceneId)
                .orElseThrow(() -> new EntityNotFoundException("Scene not found"));

        Instant now = Instant.now();
        Instant lastChange = user.getLastSceneChange();

        StoryState storyState = user.getStoryState();
        if (storyState == null) {
            storyState = new StoryState();
            user.setStoryState(storyState);
        }

        boolean isCorvoPresente = storyState.getFlagAsBoolean("corvoPresente", true);

        if (sceneId == 1 && isCorvoPresente && lastChange != null) {
            long elapsed = now.getEpochSecond() - lastChange.getEpochSecond();
            if (elapsed >= 60) {
                storyState.setFlag("corvoPresente", false);
                isCorvoPresente = false; // aggiorna anche la variabile locale
                userRepository.save(user);
            }
        }

        SceneDTO dto = mapSceneToDTO(scene);
        final boolean finalIsCorvoPresente = isCorvoPresente;
        final StoryState finalStoryState = storyState;

        dto.choices = dto.choices.stream()
                .filter(c -> !(c.text.equalsIgnoreCase("Segui il corvo.") && !finalIsCorvoPresente))
                .filter(c -> {
                    if (c.availableAfterSeconds == null) return true;
                    if (lastChange == null) return false;
                    long elapsed = now.getEpochSecond() - lastChange.getEpochSecond();
                    return elapsed >= c.availableAfterSeconds;
                })
                .filter(c -> {
                    if (c.requiredFlags == null || c.requiredFlags.isEmpty()) return true;
                    for (String flag : c.requiredFlags) {
                        if (!finalStoryState.getFlagAsBoolean(flag, false)) return false;
                    }
                    return true;
                })
                .toList();

        return dto;
    }

}
