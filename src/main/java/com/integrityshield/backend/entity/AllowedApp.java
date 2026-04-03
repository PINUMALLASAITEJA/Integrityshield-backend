package com.integrityshield.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "allowed_apps")
public class AllowedApp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String appName;

    public AllowedApp() {}

    public AllowedApp(String appName) {
        this.appName = appName;
    }

    public Long getId() {
        return id;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
}