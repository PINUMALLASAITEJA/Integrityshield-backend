package com.integrityshield.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sessions")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long facultyId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String status; // ACTIVE / ENDED

    // 🔥 NEW FIELD
    @Column(length = 1000)
    private String allowedApps; // comma-separated

    // Getters & Setters
    public Long getId() { return id; }

    public Long getFacultyId() { return facultyId; }
    public void setFacultyId(Long facultyId) { this.facultyId = facultyId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAllowedApps() { return allowedApps; }
    public void setAllowedApps(String allowedApps) { this.allowedApps = allowedApps; }
}