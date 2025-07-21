package com.zorzi.backend.controller;

import com.zorzi.backend.dto.GameResponseDTO;
import com.zorzi.backend.dto.PlayerChoiceDTO;
import com.zorzi.backend.dto.SceneDTO;
import com.zorzi.backend.model.Scene;
import com.zorzi.backend.model.StoryState;
import com.zorzi.backend.model.User;
import com.zorzi.backend.repository.SceneRepository;
import com.zorzi.backend.repository.UserRepository;
import com.zorzi.backend.service.GameService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/game")
@CrossOrigin
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final UserRepository userRepository;
    private final SceneRepository sceneRepository;

    @PostMapping("/choice")
    public ResponseEntity<GameResponseDTO> makeChoice(Authentication authentication, @RequestBody PlayerChoiceDTO dto) {

        String username = authentication.getName();


        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));


        GameResponseDTO response = gameService.processChoice(dto, user.getId());

        if (response.nextSceneId != null) {
            user.setLastSceneId(response.nextSceneId);
            user.setLastSceneChange(Instant.now());
            userRepository.save(user);
        }

        return ResponseEntity.ok(response);
    }


    @GetMapping("/load")
    public ResponseEntity<SceneDTO> loadGame(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Long sceneId = user.getLastSceneId();
        if (sceneId == null) {
            sceneId = 1L;
            user.setLastSceneId(sceneId);
        }

        if (user.getLastSceneChange() == null) {
            user.setLastSceneChange(Instant.now());
        }

        userRepository.save(user);

        SceneDTO scenaDTO = gameService.getSceneForUser(sceneId, user.getId());
        return ResponseEntity.ok(scenaDTO);
    }


    @GetMapping("/scene/{sceneId}/user/{userId}")
    public ResponseEntity<SceneDTO> getSceneForUser(@PathVariable Long sceneId, @PathVariable Long userId) {
        return ResponseEntity.ok(gameService.getSceneForUser(sceneId, userId));
    }

    @GetMapping("/finale")
    public ResponseEntity<SceneDTO> getFinalScene(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        StoryState state = user.getStoryState();
        int score = user.getScore() != null ? user.getScore() : 0;

        Scene finale = gameService.determineFinale(state, score);
        return ResponseEntity.ok(gameService.mapSceneToDTO(finale, user));
    }
    @GetMapping("/start")
    public ResponseEntity<SceneDTO> startGame(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        StoryState newState = new StoryState();
        newState.setFlag("corvoPresente", true);
        user.setStoryState(newState);
        user.setScore(0);
        user.setLastSceneChange(Instant.now());
        user.setLastSceneId(1L);
        userRepository.save(user);

        SceneDTO scenaDTO = gameService.getSceneForUser(1L, user.getId());
        return ResponseEntity.ok(scenaDTO);
    }

    @GetMapping("/finaliDisponibili")
    public ResponseEntity<List<SceneDTO>> getFinaliDisponibili(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        List<SceneDTO> finali = gameService.getAvailableFinalesForUser(user);
        return ResponseEntity.ok(finali);
    }



}


