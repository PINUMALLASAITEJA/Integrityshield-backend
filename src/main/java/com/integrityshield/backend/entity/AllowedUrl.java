package com.integrityshield.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "allowed_urls")
public class AllowedUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String url;

    public AllowedUrl() {}

    public AllowedUrl(String url) {
        this.url = url;
    }

    public Long getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}