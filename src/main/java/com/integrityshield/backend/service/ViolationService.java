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
    private final SimpMessagingTemplate messagingTemplate; // 🔥 NEW

    // 🔥 track ongoing violations
    private final Map<String, Long> activeStartTime = new HashMap<>();

    // 🔥 track counts
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

        violationCount.put(studentRoll,
                violationCount.getOrDefault(studentRoll, 0) + 1);

        Violation v = new Violation();
        v.setSessionId(session.getId());
        v.setStudentRoll(studentRoll);
        v.setMessage("Unauthorized app: " + appName + " (" + actualDuration + "s)");
        v.setLevel("WARNING");
        v.setTimestamp(LocalDateTime.now());

        repo.save(v);

        // 🔥 SEND TO DASHBOARD
        messagingTemplate.convertAndSend("/topic/faculty-alerts", v);

        System.out.println("⚠️ " + studentRoll +
                " used " + appName +
                " for " + actualDuration + " sec");
    }

    /* ================= LIVE ALERTS ================= */

    public List<Violation> getCurrentSessionAlerts() {

        var session = sessionService.getActiveSession();
        if (session == null) return List.of();

        return repo.findAll();
    }

    /* ================= REPORT ================= */

    public List<StudentFlagDTO> getSessionReport(Long sessionId) {

        List<Violation> violations = repo.findAll();

        Map<String, Integer> countMap = new HashMap<>();

        for (Violation v : violations) {
            countMap.put(
                    v.getStudentRoll(),
                    countMap.getOrDefault(v.getStudentRoll(), 0) + 1
            );
        }

        List<StudentFlagDTO> result = new ArrayList<>();

        for (String student : countMap.keySet()) {
            result.add(new StudentFlagDTO(student, countMap.get(student)));
        }

        return result;
    }

    /* ================= JOIN ================= */

    public void studentJoinedBroadcast(String roll) {

        System.out.println("🟢 Student joined: " + roll);

        // 🔥 FIX: SEND TO DASHBOARD
        messagingTemplate.convertAndSend("/topic/student-join", roll);
    }
}