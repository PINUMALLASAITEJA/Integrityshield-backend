package com.integrityshield.backend.dto;

import com.integrityshield.backend.entity.Role;

public class UserRegisterRequest {

    private String userIdentifier;
    private Role role;
    private String password;

    public String getUserIdentifier() { return userIdentifier; }
    public void setUserIdentifier(String userIdentifier) {
        this.userIdentifier = userIdentifier;
    }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getPassword() { return password; }
    public void setPassword(String password) {
        this.password = password;
    }
}