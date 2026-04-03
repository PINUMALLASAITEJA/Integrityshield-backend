package com.integrityshield.backend.dto;

public class StudentFlagDTO {

    private String studentRoll;
    private long violationCount;

    public StudentFlagDTO(String studentRoll, long violationCount) {
        this.studentRoll = studentRoll;
        this.violationCount = violationCount;
    }

    public String getStudentRoll() {
        return studentRoll;
    }

    public long getViolationCount() {
        return violationCount;
    }
}
