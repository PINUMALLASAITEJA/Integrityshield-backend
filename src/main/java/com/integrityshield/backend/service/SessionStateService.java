package com.integrityshield.backend.service;

import com.integrityshield.backend.entity.Session;
import com.integrityshield.backend.repository.SessionRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionStateService {

    private final SessionRepository sessionRepo;
    private final PermissionService permissionService;
    private final SimpMessagingTemplate messagingTemplate;

    private final Set<String> activeStudents = ConcurrentHashMap.newKeySet();

    public SessionStateService(SessionRepository sessionRepo,
                               PermissionService permissionService,
                               SimpMessagingTemplate messagingTemplate) {

        this.sessionRepo = sessionRepo;
        this.permissionService = permissionService;
        this.messagingTemplate = messagingTemplate;
    }

    public Long start(Long facultyId, String allowedApps) {

        sessionRepo.findByStatus("ACTIVE")
                .ifPresent(existing -> {
                    existing.setStatus("ENDED");
                    existing.setEndTime(LocalDateTime.now());
                    sessionRepo.save(existing);
                });

        permissionService.clearAllApps();

        Session session = new Session();
        session.setFacultyId(facultyId);
        session.setStartTime(LocalDateTime.now());
        session.setStatus("ACTIVE");

        sessionRepo.save(session);

        // ✅ RESET STUDENTS
        activeStudents.clear();

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

        messagingTemplate.convertAndSend("/topic/session-end", "ENDED");
    }

    public Session getActiveSession() {
        return sessionRepo.findByStatus("ACTIVE").orElse(null);
    }

    public Long getActiveSessionId() {
        Session s = getActiveSession();
        return s != null ? s.getId() : null;
    }

    public void studentJoined(String roll) {

        // 🔥 CRITICAL SAFETY CHECK
        if (getActiveSessionId() == null) {
            throw new RuntimeException("No active session");
        }

        if (activeStudents.add(roll)) {
            messagingTemplate.convertAndSend("/topic/student-join", roll);
        }
    }

    public Set<String> getActiveStudents() {
        return activeStudents;
    }

    public List<Session> getSessionsByDate(LocalDate date) {
        return sessionRepo.findAll();
    }
}