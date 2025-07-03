package com.zorzi.backend.controller;

import com.zorzi.backend.model.User;
import com.zorzi.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;

    @GetMapping("/users")
    public ResponseEntity<List<UserInfo>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserInfo> usersInfo = users.stream()
                .map(u -> new UserInfo(u.getId(), u.getUsername(), u.getRole().name(), u.getScore()))
                .toList();

        return ResponseEntity.ok(usersInfo);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserInfo> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(u -> new UserInfo(u.getId(), u.getUsername(), u.getRole().name(), u.getScore()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private record UserInfo(Long id, String username, String role, Integer score) {}

    @PatchMapping("/users/{id}/reset")
    public ResponseEntity<?> resetUser(@PathVariable Long id, @RequestBody ResetRequest request) {
        return userRepository.findById(id).map(user -> {
            if (request.resetScore()) user.setScore(0);
            if (request.resetStory()) user.setStoryState("");
            userRepository.save(user);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) return ResponseEntity.notFound().build();
        userRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }


    public record ResetRequest(boolean resetScore, boolean resetStory) {}
}
