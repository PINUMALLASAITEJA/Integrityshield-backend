package com.integrityshield.backend.service;

import com.integrityshield.backend.entity.Session;
import com.integrityshield.backend.repository.SessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionStateService {

    private final SessionRepository sessionRepo;
    private final PermissionService permissionService;

    private final Set<String> activeStudents = ConcurrentHashMap.newKeySet();

    public SessionStateService(SessionRepository sessionRepo,
                               PermissionService permissionService) {
        this.sessionRepo = sessionRepo;
        this.permissionService = permissionService;
    }

    public Long start(Long facultyId, String allowedApps) {

        sessionRepo.findByStatus("ACTIVE")
                .ifPresent(existing -> {
                    existing.setStatus("ENDED");
                    existing.setEndTime(LocalDateTime.now());
                    sessionRepo.save(existing);
                });

        // 🔥 RESET APPS EVERY SESSION
        permissionService.clearAllApps();

        Session session = new Session();
        session.setFacultyId(facultyId);
        session.setStartTime(LocalDateTime.now());
        session.setStatus("ACTIVE");

        List<String> apps = permissionService.getAllowedApps();
        String formatted = String.join(",", apps);

        session.setAllowedApps(formatted);

        sessionRepo.save(session);

        activeStudents.clear();

        System.out.println("🚀 New session started: " + session.getId());

        return session.getId();
    }

    public void stop() {

        Optional<Session> active = sessionRepo.findByStatus("ACTIVE");

        active.ifPresent(session -> {
            session.setStatus("ENDED");
            session.setEndTime(LocalDateTime.now());
            sessionRepo.save(session);
        });

        activeStudents.clear();
    }

    public Session getActiveSession() {
        return sessionRepo.findByStatus("ACTIVE").orElse(null);
    }

    public Long getActiveSessionId() {
        Session s = getActiveSession();
        return s != null ? s.getId() : null;
    }

    public void studentJoined(String roll) {

        if (!activeStudents.contains(roll)) {
            activeStudents.add(roll);
            System.out.println("🟢 Student joined: " + roll);
        }
    }

    public Set<String> getActiveStudents() {
        return activeStudents;
    }

    public List<Session> getSessionsByDate(LocalDate date) {
        return sessionRepo.findAll();
    }
}