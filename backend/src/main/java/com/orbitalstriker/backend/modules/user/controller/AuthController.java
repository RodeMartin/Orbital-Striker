package com.orbitalstriker.backend.modules.user.controller;

import com.orbitalstriker.backend.modules.user.model.User;
import com.orbitalstriker.backend.modules.user.repository.UserRepository;
import com.orbitalstriker.backend.modules.user.service.EmailService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final UserRepository userRepository;
    private final EmailService emailService;

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest req) {

        if (req.getUsername() == null || req.getUsername().isBlank()) {
            return new AuthResponse(false, "Felhasználónév kötelező.", null);
        }

        try {
            if (userRepository.existsByUsername(req.getUsername())) {
                User existing = userRepository.findByUsername(req.getUsername()).orElse(null);
                if (existing != null) {
                    return new AuthResponse(true, "Üdv újra, " + existing.getUsername() + "!", existing);
                }
            }
            User user = User.builder()
                    .username(req.getUsername())
                    .email(req.getEmail())
                    .isPremium(false)
                    .xp(0)
                    .level(1)
                    .bpXp(0)
                    .bpLevel(1)
                    .build();

            try {
                user = userRepository.save(user);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (req.getEmail() != null && !req.getEmail().isBlank()) {
                try {
                    emailService.sendEmail(req.getEmail(), "Welcome!", "Sikeres regisztráció!");
                } catch (Exception mailEx) {
                    mailEx.printStackTrace();
                }
            }

            return new AuthResponse(true, "Sikeres regisztráció!", user);

        } catch (Exception e) {
            e.printStackTrace();
            return new AuthResponse(false, "Váratlan szerverhiba regisztráció közben.", null);
        }
    }

@PostMapping("/login")
public AuthResponse login(@RequestBody LoginRequest req) {

    if (req.getEmail() == null || req.getEmail().isBlank()) {
        return new AuthResponse(false, "Email kötelező.", null);
    }

    User user = userRepository.findByEmail(req.getEmail()).orElse(null);

    if (user == null) {
        return new AuthResponse(false, "Nincs ilyen felhasználó.", null);
    }

    return new AuthResponse(true, "Sikeres bejelentkezés!", user);
}

@Data
public static class LoginRequest {
    private String email;
}

    @Data
    public static class RegisterRequest {
        private String username;
        private String email;
    }

    @Data
    @AllArgsConstructor
    public static class AuthResponse {
        private boolean success;
        private String message;
        private User user;
    }
}
