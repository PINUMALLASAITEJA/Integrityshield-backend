package com.integrityshield.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userIdentifier;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private Boolean active;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public User() {}

    public User(String userIdentifier, String password, Role role) {
        this.userIdentifier = userIdentifier;
        this.password = password;
        this.role = role;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    // -------- Getters --------

    public Long getId() { return id; }
    public String getUserIdentifier() { return userIdentifier; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    public Boolean getActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // -------- Optional Setter (if needed later) --------

    public void setActive(Boolean active) {
        this.active = active;
    }
}