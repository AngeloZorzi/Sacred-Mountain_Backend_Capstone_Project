package com.zorzi.backend.controller;

import com.zorzi.backend.model.StoryState;
import com.zorzi.backend.model.User;
import com.zorzi.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        return ResponseEntity.ok(new UserResponse(
                user.getUsername(),
                user.getRole(),
                user.getScore(),
                user.getStoryState()
        ));
    }

    @PostMapping("/progress")
    public ResponseEntity<?> saveProgress(@AuthenticationPrincipal UserDetails userDetails,
                                          @RequestBody ProgressRequest request) {
        String username = userDetails.getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        user.setStoryState(request.storyState());
        user.setScore(request.score());
        userRepository.save(user);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardEntry>> getLeaderboard() {
        List<User> topUsers = userRepository.findTop10ByOrderByScoreDesc();

        List<LeaderboardEntry> leaderboard = topUsers.stream()
                .map(u -> new LeaderboardEntry(u.getUsername(), u.getScore()))
                .toList();

        return ResponseEntity.ok(leaderboard);
    }

    private record UserResponse(String username, Enum role, Integer score, StoryState storyState) {}
    private record ProgressRequest(StoryState storyState, Integer score) {}
    private record LeaderboardEntry(String username, Integer score) {}
}
