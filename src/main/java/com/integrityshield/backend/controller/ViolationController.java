package com.integrityshield.backend.controller;

import com.integrityshield.backend.service.ViolationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/violation")
@CrossOrigin
public class ViolationController {

    private final ViolationService service;

    public ViolationController(ViolationService service) {
        this.service = service;
    }

    @PostMapping("/report")
    public String report(@RequestParam String studentRoll,
                         @RequestParam String appName) {

        // ✅ FIXED
        service.saveViolation(studentRoll, appName, null, false);

        return "Violation received";
    }
}