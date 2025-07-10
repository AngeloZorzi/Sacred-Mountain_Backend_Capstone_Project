package com.zorzi.backend.controller;

import com.zorzi.backend.dto.SceneDTO;
import com.zorzi.backend.service.SceneService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/scenes")
@CrossOrigin
public class SceneController {

    private final SceneService sceneService;

    public SceneController(SceneService sceneService) {
        this.sceneService = sceneService;
    }

    @GetMapping
    public List<SceneDTO> getAllScenes() {
        return sceneService.getAllScenes();
    }

    @GetMapping("/{id}")
    public SceneDTO getScene(@PathVariable Long id) {
        return sceneService.getSceneById(id);
    }

    @PostMapping
    public ResponseEntity<SceneDTO> createScene(@RequestBody SceneDTO dto) {
        SceneDTO created = sceneService.createScene(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteScene(@PathVariable Long id) {
        sceneService.deleteScene(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<SceneDTO> updateScene(@PathVariable Long id, @RequestBody SceneDTO dto) {
        SceneDTO updated = sceneService.updateScene(id, dto);
        return ResponseEntity.ok(updated);
    }

}

