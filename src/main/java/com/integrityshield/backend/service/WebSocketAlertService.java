package com.integrityshield.backend.service;

import com.integrityshield.backend.dto.AlertMessageDTO;
import com.integrityshield.backend.entity.Violation;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketAlertService {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketAlertService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendAlert(Violation violation) {

        AlertMessageDTO alert = new AlertMessageDTO(
                violation.getStudentRoll(),
                violation.getMessage(),
                violation.getLevel(),
                violation.getTimestamp()
        );

        messagingTemplate.convertAndSend(
                "/topic/faculty-alerts",
                alert
        );
    }
}