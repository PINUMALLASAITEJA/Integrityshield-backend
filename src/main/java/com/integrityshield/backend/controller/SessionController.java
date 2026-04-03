package com.integrityshield.backend.controller;

import com.integrityshield.backend.entity.Session;
import com.integrityshield.backend.service.SessionStateService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/session")
@CrossOrigin
public class SessionController {

    private final SessionStateService service;

    public SessionController(SessionStateService service) {
        this.service = service;
    }

    // 🔥 START SESSION
    @PostMapping("/start")
    public Long startSession(@RequestParam Long facultyId,
                             @RequestParam String allowedApps) {

        return service.start(facultyId, allowedApps);
    }

    // 🔥 STOP SESSION
    @PostMapping("/stop")
    public String stopSession() {
        service.stop();
        return "Session stopped";
    }

    // 🔥 STUDENT FETCH SESSION
    @GetMapping("/active")
    public Session getActiveSession() {
        return service.getActiveSession();
    }
}