package com.integrityshield.backend.service;

import com.integrityshield.backend.dto.LoginRequest;
import com.integrityshield.backend.dto.UserRegisterRequest;
import com.integrityshield.backend.entity.User;
import com.integrityshield.backend.repository.UserRepository;
import com.integrityshield.backend.security.JwtUtil;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
            return "User already exists";
        }

        User user = new User(
                request.getUserIdentifier(),
                passwordEncoder.encode(request.getPassword()),
                request.getRole()
        );

        userRepo.save(user);

        return "User registered successfully";
    }

    public String login(LoginRequest request) {

        Optional<User> optionalUser =
                userRepo.findByUserIdentifier(request.getUserIdentifier());

        if (optionalUser.isEmpty()) {
            return "User not found";
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(request.getPassword(),
                                     user.getPassword())) {
            return "Invalid credentials";
        }

        return jwtUtil.generateToken(
                user.getUserIdentifier(),
                user.getRole().name()
        );
    }
}