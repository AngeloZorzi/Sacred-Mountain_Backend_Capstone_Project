package com.zorzi.backend.controller;

import com.zorzi.backend.dto.GameResponseDTO;
import com.zorzi.backend.dto.PlayerChoiceDTO;
import com.zorzi.backend.dto.SceneDTO;
import com.zorzi.backend.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
@CrossOrigin
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @PostMapping("/choice")
    public ResponseEntity<GameResponseDTO> makeChoice(@RequestBody PlayerChoiceDTO dto) {
        GameResponseDTO response = gameService.processChoice(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/scene/{sceneId}/user/{userId}")
    public ResponseEntity<SceneDTO> getSceneForUser(@PathVariable Long sceneId, @PathVariable Long userId) {
        return ResponseEntity.ok(gameService.getSceneForUser(sceneId, userId));
    }

}

