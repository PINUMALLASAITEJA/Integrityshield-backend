package com.integrityshield.backend.dto;

import java.time.LocalDateTime;

public class AlertMessageDTO {

    private String studentRoll;
    private String message;
    private String level;
    private LocalDateTime timestamp;

    public AlertMessageDTO(String studentRoll,
                           String message,
                           String level,
                           LocalDateTime timestamp) {
        this.studentRoll = studentRoll;
        this.message = message;
        this.level = level;
        this.timestamp = timestamp;
    }

    public String getStudentRoll() { return studentRoll; }
    public String getMessage() { return message; }
    public String getLevel() { return level; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
