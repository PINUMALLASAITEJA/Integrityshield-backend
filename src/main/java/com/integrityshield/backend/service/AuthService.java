package com.integrityshield.backend.service;

import com.integrityshield.backend.dto.LoginRequest;
import com.integrityshield.backend.dto.UserRegisterRequest;
import com.integrityshield.backend.entity.User;
import com.integrityshield.backend.repository.UserRepository;
import com.integrityshield.backend.security.JwtUtil;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepo,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public String register(UserRegisterRequest request) {

        if (userRepo.existsByUserIdentifier(request.getUserIdentifier())) {
            throw new RuntimeException("User already exists");
        }

        User user = new User(
                request.getUserIdentifier(),
                passwordEncoder.encode(request.getPassword()),
                request.getRole() // should be FACULTY
        );

        userRepo.save(user);

        return "User registered successfully";
    }

    public String login(LoginRequest request) {

        User user = userRepo.findByUserIdentifier(request.getUserIdentifier())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // 🔥 ALWAYS RETURN VALID JWT
        return jwtUtil.generateToken(
                user.getUserIdentifier(),
                user.getRole().name() // FACULTY → becomes ROLE_FACULTY
        );
    }
}