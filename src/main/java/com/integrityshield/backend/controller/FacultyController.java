package com.integrityshield.backend.controller;

import com.integrityshield.backend.dto.StudentFlagDTO;
import com.integrityshield.backend.entity.Session;
import com.integrityshield.backend.entity.Violation;
import com.integrityshield.backend.service.ViolationService;
import com.integrityshield.backend.service.SessionStateService;
import com.integrityshield.backend.service.PermissionService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/faculty")
@PreAuthorize("hasRole('FACULTY')")
public class FacultyController {

    private final SessionStateService sessionService;
    private final ViolationService violationService;
    private final PermissionService permissionService;

    public FacultyController(SessionStateService sessionService,
                             ViolationService violationService,
                             PermissionService permissionService) {

        this.sessionService = sessionService;
        this.violationService = violationService;
        this.permissionService = permissionService;
    }

    /* ================= SESSION CONTROL ================= */

    @PostMapping("/start-session")
    public String startSession(@RequestParam(required = false) String allowedApps) {

        System.out.println("📡 Start session request received");

        if (sessionService.getActiveSessionId() != null) {
            System.out.println("⚠️ Session already active");
            return "Session already active";
        }

        // 🔥 FIX: handle null safely
        if (allowedApps == null || allowedApps.trim().isEmpty()) {
            allowedApps = ""; // means allow all apps
            System.out.println("⚠️ No allowedApps provided → allowing all apps");
        } else {
            System.out.println("✅ Allowed Apps: " + allowedApps);
        }

        Long sessionId = sessionService.start(1L, allowedApps);

        System.out.println("🚀 Session started with ID: " + sessionId);

        return "Session started. ID: " + sessionId;
    }

    @PostMapping("/stop-session")
    public String stopSession() {

        if (sessionService.getActiveSessionId() == null) {
            return "No active session to stop";
        }

        sessionService.stop();

        System.out.println("🛑 Session stopped");

        return "Session stopped";
    }

    /* ================= LIVE DATA ================= */

    @GetMapping("/alerts")
    public List<Violation> viewEscalatedAlerts() {

        Long sessionId = sessionService.getActiveSessionId();

        if (sessionId == null) return List.of();

        return violationService.getCurrentSessionAlerts();
    }

    @GetMapping("/students")
    public Set<String> activeStudents() {
        return sessionService.getActiveStudents();
    }

    /* ================= DATE BASED SESSION LIST ================= */

    @GetMapping("/sessions")
    public List<Session> getSessionsByDate(
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        return sessionService.getSessionsByDate(date);
    }

    /* ================= SESSION REPORT ================= */

    @GetMapping("/session-report/{sessionId}")
    public List<StudentFlagDTO> getSessionReport(
            @PathVariable Long sessionId) {

        return violationService.getSessionReport(sessionId);
    }

    /* ================= ALLOWED APPLICATIONS ================= */

    @PostMapping("/allow-app")
    public void allowApp(@RequestBody String appName) {
        permissionService.addAllowedApp(appName);
    }

    @GetMapping("/allowed-apps")
    public List<String> getAllowedApps() {
        return permissionService.getAllowedApps();
    }

    @DeleteMapping("/remove-app")
    public void removeApp(@RequestParam String appName) {
        permissionService.removeAllowedApp(appName);
    }

    /* ================= ALLOWED URLS ================= */

    @PostMapping("/allow-url")
    public void allowUrl(@RequestBody String url) {
        permissionService.addAllowedUrl(url);
    }

    @GetMapping("/allowed-urls")
    public List<String> getAllowedUrls() {
        return permissionService.getAllowedUrls();
    }

    @DeleteMapping("/remove-url")
    public void removeUrl(@RequestParam String url) {
        permissionService.removeAllowedUrl(url);
    }
}