package com.integrityshield.backend.service;

import com.integrityshield.backend.dto.StudentFlagDTO;
import com.integrityshield.backend.entity.Violation;
import com.integrityshield.backend.repository.ViolationRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ViolationService {

    private final ViolationRepository repo;
    private final SessionStateService sessionService;
    private final SimpMessagingTemplate messagingTemplate;

    private final Map<String, Long> activeStartTime = new HashMap<>();
    private final Map<String, Integer> violationCount = new HashMap<>();

    public ViolationService(ViolationRepository repo,
                            SessionStateService sessionService,
                            SimpMessagingTemplate messagingTemplate) {
        this.repo = repo;
        this.sessionService = sessionService;
        this.messagingTemplate = messagingTemplate;
    }

    public void saveViolation(String studentRoll,
                              String appName,
                              Long duration,
                              boolean ongoing) {

        var session = sessionService.getActiveSession();
        if (session == null) return;

        if (appName != null && appName.toLowerCase().contains("cmd")) return;

        String key = studentRoll + "_" + appName.toLowerCase();

        if (ongoing) {
            activeStartTime.put(key, System.currentTimeMillis());
            return;
        }

        long start = activeStartTime.getOrDefault(key, System.currentTimeMillis());
        long actualDuration = duration != null
                ? duration
                : (System.currentTimeMillis() - start) / 1000;

        activeStartTime.remove(key);

        int count = violationCount.getOrDefault(studentRoll, 0) + 1;
        violationCount.put(studentRoll, count);

        // ✅ FIRST WARNING ONLY
        if (count == 1) {
            messagingTemplate.convertAndSend(
                    "/topic/student-warning/" + studentRoll,
                    "Please focus on work"
            );
            return;
        }

        Violation v = new Violation();
        v.setSessionId(session.getId());
        v.setStudentRoll(studentRoll);
        v.setMessage("Unauthorized app: " + appName + " (" + actualDuration + "s)");
        v.setLevel("WARNING");
        v.setTimestamp(LocalDateTime.now());

        repo.save(v);

        messagingTemplate.convertAndSend("/topic/faculty-alerts", v);
    }

    // ✅ RESET (NOW CALLED FROM CONTROLLER)
    public void resetViolations() {
        violationCount.clear();
        activeStartTime.clear();
    }

    public List<Violation> getCurrentSessionAlerts() {
        return repo.findAll();
    }

    public List<StudentFlagDTO> getSessionReport(Long sessionId) {
        return new ArrayList<>();
    }
}