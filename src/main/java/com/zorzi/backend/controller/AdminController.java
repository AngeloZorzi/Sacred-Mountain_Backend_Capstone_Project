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
                .map(u -> new UserInfo(u.getId(), u.getUsername(), u.getRole(), u.getScore()))
                .toList();

        return ResponseEntity.ok(usersInfo);
    }

    private record UserInfo(Long id, String username, Enum role, Integer score) {}
}
