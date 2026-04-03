package com.integrityshield.backend.controller;

import com.integrityshield.backend.dto.LoginRequest;
import com.integrityshield.backend.dto.UserRegisterRequest;
import com.integrityshield.backend.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public String register(@RequestBody UserRegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        return authService.login(request);  // must return JWT
    }
}