package com.zorzi.backend.service;

import com.zorzi.backend.dto.AuthResponse;
import com.zorzi.backend.dto.RegisterRequest;
import com.zorzi.backend.enums.Role;
import com.zorzi.backend.model.User;
import com.zorzi.backend.repository.UserRepository;
import com.zorzi.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.PLAYER)
                .build();

        userRepository.save(user);
        String jwt = jwtService.generateToken(user);
        return new AuthResponse(jwt);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(RegisterRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Credenziali non valide");
        }

        String jwt = jwtService.generateToken(user);
        return new AuthResponse(jwt);
    }
}
