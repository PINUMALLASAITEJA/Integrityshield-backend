package com.integrityshield.backend.controller;

import com.integrityshield.backend.security.JwtUtil;
import com.integrityshield.backend.service.SessionStateService;
import com.integrityshield.backend.service.ViolationService;
import com.integrityshield.backend.service.PermissionService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/student")
@CrossOrigin
public class StudentController {

    private final SessionStateService sessionService;
    private final ViolationService violationService;
    private final JwtUtil jwtUtil;
    private final PermissionService permissionService;

    public StudentController(SessionStateService sessionService,
                             ViolationService violationService,
                             JwtUtil jwtUtil,
                             PermissionService permissionService) {
        this.sessionService = sessionService;
        this.violationService = violationService;
        this.jwtUtil = jwtUtil;
        this.permissionService = permissionService;
    }

    /* ================= LOGIN ================= */

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> req) {

        String roll = req.get("rollNumber");
        String password = req.get("password");

        if (roll == null || password == null) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(roll, "STUDENT");

        return Map.of("token", token);
    }

    /* ================= DASHBOARD ================= */

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/dashboard")
    public ResponseEntity<?> studentArea(Authentication auth) {

        String roll = auth.getName();

        if (sessionService.getActiveSessionId() == null) {
            return ResponseEntity.status(403).body("Session not active");
        }

        // ✅ FIX: only once
        sessionService.studentJoined(roll);

        return ResponseEntity.ok("Session active");
    }

    /* ================= FETCH SESSION ================= */

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/session")
    public ResponseEntity<?> getSession(Authentication auth) {

        var session = sessionService.getActiveSession();

        if (session == null) {
            return ResponseEntity.status(403).body("No active session");
        }

        String roll = auth.getName();

        // ✅ FIX: ensure re-join every time
        sessionService.studentJoined(roll);

        List<String> apps = permissionService.getAllowedApps();

        return ResponseEntity.ok(
                Map.of(
                        "status", "ACTIVE",
                        "allowedApps", String.join(",", apps)
                )
        );
    }

    /* ================= REPORT VIOLATION ================= */

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/violation")
    public String reportViolation(@RequestParam String appName,
                                 @RequestParam(required = false) Long duration,
                                 @RequestParam(defaultValue = "false") boolean ongoing,
                                 Authentication auth) {

        String roll = auth.getName();

        if (sessionService.getActiveSessionId() == null) {
            return "No active session";
        }

        violationService.saveViolation(roll, appName, duration, ongoing);

        return "Violation recorded";
    }
}