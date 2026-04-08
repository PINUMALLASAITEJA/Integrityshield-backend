package com.integrityshield.backend.controller;

import com.integrityshield.backend.dto.LoginRequest;
import com.integrityshield.backend.dto.UserRegisterRequest;
import com.integrityshield.backend.entity.Role;
import com.integrityshield.backend.service.SessionStateService;
import com.integrityshield.backend.service.ViolationService;
import com.integrityshield.backend.service.PermissionService;
import com.integrityshield.backend.service.AuthService;

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
    private final PermissionService permissionService;
    private final AuthService authService;

    public StudentController(SessionStateService sessionService,
                             ViolationService violationService,
                             PermissionService permissionService,
                             AuthService authService) {

        this.sessionService = sessionService;
        this.violationService = violationService;
        this.permissionService = permissionService;
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> req) {

        try {
            String roll = req.get("rollNumber");
            String password = req.get("password");

            if (roll == null || password == null) {
                return ResponseEntity.badRequest().body("Invalid data");
            }

            UserRegisterRequest request = new UserRegisterRequest();
            request.setUserIdentifier(roll);
            request.setPassword(password);
            request.setRole(Role.STUDENT);

            String res = authService.register(request);

            return ResponseEntity.ok(res);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> req) {

        try {
            String roll = req.get("rollNumber");
            String password = req.get("password");

            if (roll == null || password == null) {
                return ResponseEntity.badRequest().body("Invalid data");
            }

            LoginRequest request = new LoginRequest();
            request.setUserIdentifier(roll);
            request.setPassword(password);

            String token = authService.login(request);

            return ResponseEntity.ok(Map.of("token", token));

        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    /* ================= DASHBOARD (ONLY JOIN HERE) ================= */

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/dashboard")
    public ResponseEntity<?> studentArea(Authentication auth) {

        String roll = auth.getName();

        if (sessionService.getActiveSessionId() == null) {
            return ResponseEntity.status(403).body("Session not active");
        }

        // ✅ JOIN ONLY HERE
        sessionService.studentJoined(roll);

        return ResponseEntity.ok("Session active");
    }

    /* ================= SESSION (NO JOIN HERE) ================= */

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/session")
    public ResponseEntity<?> getSession(Authentication auth) {

        var session = sessionService.getActiveSession();

        if (session == null) {
            return ResponseEntity.status(403).body("No active session");
        }

        List<String> apps = permissionService.getAllowedApps();

        return ResponseEntity.ok(
                Map.of(
                        "status", "ACTIVE",
                        "allowedApps", String.join(",", apps)
                )
        );
    }

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