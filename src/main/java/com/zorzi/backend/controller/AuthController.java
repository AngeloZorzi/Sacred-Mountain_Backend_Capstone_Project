package com.zorzi.backend.controller;

import com.zorzi.backend.dto.AuthResponse;
import com.zorzi.backend.dto.RegisterRequest;
import com.zorzi.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody RegisterRequest request) {
        return userService.login(request);
    }
}

