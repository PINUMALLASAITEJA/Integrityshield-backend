package com.integrityshield.backend.dto;

import com.integrityshield.backend.entity.Role;

public class AuthResponse {

    private String token;
    private Role role;

    public AuthResponse() {}

    // Used in register
    public AuthResponse(String token) {
        this.token = token;
    }

    // Used in login
    public AuthResponse(String token, Role role) {
        this.token = token;
        this.role = role;
    }

    public String getToken() { return token; }
    public Role getRole() { return role; }
}