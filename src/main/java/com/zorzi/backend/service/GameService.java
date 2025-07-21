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
import java.util.List;

@Service
@RequiredArgsConstructor
public class GameService {

    private final UserRepository userRepository;
    private final SceneRepository sceneRepository;
    private final ChoiceRepository choiceRepository;

    public GameResponseDTO processChoice(PlayerChoiceDTO dto, Long userId) {
        User user = userRepository.findById(userId)
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
        }


        if (choice.getFlagChanges() != null) {
            choice.getFlagChanges().forEach(storyState::set);
        }

        if ("Segui la voce.".equals(choice.getText())) {
            storyState.setFlag("choseVoice", true);
            Instant lastChange = user.getLastSceneChange();
            if (lastChange != null) {
                long elapsed = Instant.now().getEpochSecond() - lastChange.getEpochSecond();
                if (elapsed <= 30) {
                    storyState.setFlag("choseInTime", true);
                }
            }
        }

        user.setStoryState(storyState); // salva dopo modifiche al state
        user.setScore((user.getScore() != null ? user.getScore() : 0) + choice.getScoreChange());
        user.setLastSceneChange(Instant.now());
        userRepository.save(user);

        Scene nextScene = choice.getNextScene();

        GameResponseDTO response = new GameResponseDTO();
        response.nextScene = nextScene != null ? mapSceneToDTO(nextScene, user) : null;
        response.nextSceneId = nextScene != null ? nextScene.getId() : null;
        response.updatedScore = user.getScore();
        response.corvoPresente = storyState.getFlagAsBoolean("corvoPresente", true);
        response.message = choice.getText();


        return response;
    }

    public Scene determineFinale(StoryState state, int score) {
        if (state.getFlagAsBoolean("hasAmuleto", false) &&
                state.getFlagAsBoolean("toldTruth", false) &&
                score >= 6) {
            return sceneRepository.findByTitle("L’Illuminazione")
                    .orElseThrow(() -> new EntityNotFoundException("Finale non trovato: L’Illuminazione"));
        }

        if (state.getFlagAsBoolean("embracedShadow", false)) {
            return sceneRepository.findByTitle("L’Integrazione dell’Ombra")
                    .orElseThrow(() -> new EntityNotFoundException("Finale non trovato: L’Integrazione dell’Ombra"));
        }

        String maskChoice = (String) state.get("maskChoice");
        if ("other".equals(maskChoice)) {
            return sceneRepository.findByTitle("Il Volto dell’Altro")
                    .orElseThrow(() -> new EntityNotFoundException("Finale non trovato: Il Volto dell’Altro"));
        }

        if ("self".equals(maskChoice)) {
            return sceneRepository.findByTitle("Il Ritorno all’Io")
                    .orElseThrow(() -> new EntityNotFoundException("Finale non trovato: Il Ritorno all’Io"));
        }

        if (state.getFlagAsBoolean("visitedFonte", false)) {
            return sceneRepository.findByTitle("Il Sigillo del Sé")
                    .orElseThrow(() -> new EntityNotFoundException("Finale non trovato: Il Sigillo del Sé"));
        }

        if (score <= 2 &&
                (!state.getFlagAsBoolean("toldTruth", true) ||
                        state.getFlagAsBoolean("choseOblivion", false))) {
            return sceneRepository.findByTitle("La Resa")
                    .orElseThrow(() -> new EntityNotFoundException("Finale non trovato: La Resa"));
        }

        if (state.getFlagAsBoolean("hasAmuleto", false) &&
                state.getFlagAsBoolean("choseVoice", false) &&
                state.getFlagAsBoolean("choseInTime", false)) {
            return sceneRepository.findByTitle("Il Cuore del Deserto")
                    .orElseThrow(() -> new EntityNotFoundException("Finale non trovato: Il Cuore del Deserto"));
        }

        return sceneRepository.findByTitle("Finale Sconosciuto")
                .orElseThrow(() -> new EntityNotFoundException("Finale sconosciuto non trovato"));
    }

    public SceneDTO getSceneForUser(Long sceneId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Scene scene = sceneRepository.findById(sceneId)
                .orElseThrow(() -> new EntityNotFoundException("Scene not found"));

        Instant now = Instant.now();
        Instant lastChange = user.getLastSceneChange();

        System.out.println("DEBUG: now = " + now);
        System.out.println("DEBUG: lastChange = " + lastChange);
        System.out.println("DEBUG: seconds elapsed = " + (lastChange != null ? now.getEpochSecond() - lastChange.getEpochSecond() : "null"));

        StoryState storyState = user.getStoryState();
        if (storyState == null) {
            storyState = new StoryState();
        }

        if (storyState.getFlagAsBoolean("corvoPresente", true) && hasElapsed(lastChange, 60)) {
            storyState.setFlag("corvoPresente", false);
            user.setStoryState(storyState);
            userRepository.save(user);
        }

        boolean isCorvoPresente = storyState.getFlagAsBoolean("corvoPresente", true);

        SceneDTO dto = mapSceneToDTO(scene, user);
        final Instant finalLastChange = lastChange;
        final StoryState finalStoryState = storyState;

        dto.choices = dto.choices.stream()
                .filter(c -> {
                    if (c.text.equalsIgnoreCase("Segui il corvo.") && !isCorvoPresente) {
                        System.out.println("DEBUG: filtro 'Segui il corvo.' escluso per corvoPresente=false");
                        return false;
                    }
                    return true;
                })
                .filter(c -> {
                    if (c.availableAfterSeconds == null) return true;
                    if (finalLastChange == null) {
                        System.out.println("DEBUG: filtro delay scelta " + c.text + " esclusa per lastChange null");
                        return false;
                    }
                    long elapsed = now.getEpochSecond() - finalLastChange.getEpochSecond();
                    boolean available = elapsed >= c.availableAfterSeconds;
                    System.out.println("DEBUG: scelta '" + c.text + "' delay=" + c.availableAfterSeconds + ", elapsed=" + elapsed + ", disponibile=" + available);
                    return available;
                })
                .filter(c -> {
                    if (c.requiredFlags == null || c.requiredFlags.isEmpty()) return true;
                    for (String flag : c.requiredFlags) {
                        if (!finalStoryState.getFlagAsBoolean(flag, false)) {
                            System.out.println("DEBUG: filtro flag scelta " + c.text + " esclusa per flag mancante: " + flag);
                            return false;
                        }
                    }
                    return true;
                })
                .toList();

        System.out.println("DEBUG: scelte filtrate: " + dto.choices.size());

        return dto;
    }



    public SceneDTO mapSceneToDTO(Scene scene, User user) {
        SceneDTO dto = new SceneDTO();
        dto.id = scene.getId();
        dto.title = scene.getTitle();
        dto.text = scene.getText();
        dto.imageUrl = scene.getImageUrl();
        dto.backgroundMusic = scene.getBackgroundMusic();
        dto.animationType = scene.getAnimationType();
        dto.lastSceneChange = user.getLastSceneChange();

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


    private boolean hasElapsed(Instant last, int seconds) {
        return last != null && Instant.now().getEpochSecond() - last.getEpochSecond() >= seconds;
    }

    public List<SceneDTO> getAvailableFinalesForUser(User user) {
        StoryState state = user.getStoryState();
        int score = user.getScore() != null ? user.getScore() : 0;

        List<Scene> finali = sceneRepository.findByIsFinaleTrue();

        return finali.stream()
                .filter(f -> isFinaleUnlocked(f.getTitle(), state, score))
                .map(f -> mapSceneToDTO(f, user))
                .toList();
    }

    private boolean isFinaleUnlocked(String title, StoryState state, int score) {
        return switch (title) {
            case "L’Illuminazione" -> state.getFlagAsBoolean("hasAmuleto", false)
                    && state.getFlagAsBoolean("toldTruth", false)
                    && score >= 6;
            case "L’Integrazione dell’Ombra" -> state.getFlagAsBoolean("embracedShadow", false);
            case "Il Volto dell’Altro" -> "other".equals(state.getExtraAsString("maskChoice"));
            case "Il Ritorno all’Io" -> "self".equals(state.getExtraAsString("maskChoice"));
            case "Il Sigillo del Sé" -> state.getFlagAsBoolean("visitedFonte", false);
            case "La Resa" -> score <= 2 && (!state.getFlagAsBoolean("toldTruth", true)
                    || state.getFlagAsBoolean("choseOblivion", false));
            case "Il Cuore del Deserto" -> state.getFlagAsBoolean("hasAmuleto", false)
                    && state.getFlagAsBoolean("choseVoice", false)
                    && state.getFlagAsBoolean("choseInTime", false);
            case "Finale Sconosciuto" -> true;
            default -> false;
        };
    }

}
