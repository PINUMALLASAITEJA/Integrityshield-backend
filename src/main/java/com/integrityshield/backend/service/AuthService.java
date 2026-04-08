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

    /* ================= REGISTER ================= */

    public String register(UserRegisterRequest request) {

        if (request.getUserIdentifier() == null || request.getPassword() == null) {
            throw new RuntimeException("Invalid input");
        }

        if (userRepo.existsByUserIdentifier(request.getUserIdentifier())) {
            throw new RuntimeException("User already exists");
        }

        // 🔥 ROLE MUST COME FROM CONTROLLER (SAFE CONTROL)
        if (request.getRole() == null) {
            throw new RuntimeException("Role is required");
        }

        User user = new User(
                request.getUserIdentifier(),
                passwordEncoder.encode(request.getPassword()),
                request.getRole() // ✅ FIXED (NO HARDCODE)
        );

        userRepo.save(user);

        return "User registered successfully";
    }

    /* ================= LOGIN ================= */

    /* ================= LOGIN ================= */

    public String login(LoginRequest request) {

        if (request.getUserIdentifier() == null || request.getPassword() == null) {
            throw new RuntimeException("Invalid input");
        }

        // 🔥 ONLY STUDENT LOGIN ALLOWED HERE
        User user = userRepo.findByUserIdentifierAndRole(
                request.getUserIdentifier(),
                com.integrityshield.backend.entity.Role.STUDENT
        ).orElseThrow(() -> new RuntimeException("Student not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return jwtUtil.generateToken(
                user.getUserIdentifier(),
                user.getRole().name()
        );
    }
}