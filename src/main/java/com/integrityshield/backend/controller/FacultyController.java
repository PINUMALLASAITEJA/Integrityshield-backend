package com.integrityshield.backend.controller;

import com.integrityshield.backend.dto.StudentFlagDTO;
import com.integrityshield.backend.entity.Session;
import com.integrityshield.backend.entity.Violation;
import com.integrityshield.backend.service.ViolationService;
import com.integrityshield.backend.service.SessionStateService;
import com.integrityshield.backend.service.PermissionService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

    /* ================= SESSION ================= */

    @PostMapping("/start-session")
    public String startSession(@RequestParam(required = false) String allowedApps,
                               Authentication auth) {

        if (sessionService.getActiveSessionId() != null) {
            return "Session already active";
        }

        Long sessionId = sessionService.start(1L, allowedApps);

        // 🔥 CRITICAL FIX (RESET VIOLATIONS HERE)
        violationService.resetViolations();

        return "Session started. ID: " + sessionId;
    }

    @PostMapping("/stop-session")
    public String stopSession() {

        if (sessionService.getActiveSessionId() == null) {
            return "No active session to stop";
        }

        sessionService.stop();
        return "Session stopped";
    }

    /* ================= ALERTS ================= */

    @GetMapping("/alerts")
    public List<Violation> viewEscalatedAlerts() {

        Long sessionId = sessionService.getActiveSessionId();
        if (sessionId == null) return List.of();

        return violationService.getCurrentSessionAlerts();
    }

    /* ================= STUDENTS ================= */

    @GetMapping("/students")
    public Set<String> activeStudents() {
        return sessionService.getActiveStudents();
    }

    /* ================= SESSIONS ================= */

    @GetMapping("/sessions")
    public List<Session> getSessionsByDate(
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        return sessionService.getSessionsByDate(date);
    }

    @GetMapping("/session-report/{sessionId}")
    public List<StudentFlagDTO> getSessionReport(
            @PathVariable Long sessionId) {

        return violationService.getSessionReport(sessionId);
    }

    /* ================= ALLOWED APPS ================= */

    @PostMapping("/allow-app")
    public void allowApp(@RequestBody String appName) {
        permissionService.addAllowedApp(appName.trim()); // ✅ FIX (clean input)
    }

    @GetMapping("/allowed-apps")
    public List<String> getAllowedApps() {
        return permissionService.getAllowedApps();
    }

    @DeleteMapping("/remove-app")
    public void removeApp(@RequestParam String appName) {
        permissionService.removeAllowedApp(appName.trim()); // ✅ FIX
    }

    /* ================= ALLOWED URLS ================= */

    @PostMapping("/allow-url")
    public void allowUrl(@RequestBody String url) {
        permissionService.addAllowedUrl(url.trim()); // ✅ FIX
    }

    @GetMapping("/allowed-urls")
    public List<String> getAllowedUrls() {
        return permissionService.getAllowedUrls();
    }

    @DeleteMapping("/remove-url")
    public void removeUrl(@RequestParam String url) {
        permissionService.removeAllowedUrl(url.trim()); // ✅ FIX
    }
}