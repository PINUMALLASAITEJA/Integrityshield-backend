package com.integrityshield.backend.service;

import com.integrityshield.backend.repository.UserRepository;
import com.integrityshield.backend.repository.ViolationRepository;
import com.integrityshield.backend.entity.Role;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CleanupService {

    private final ViolationRepository violationRepo;
    private final UserRepository userRepo;

    public CleanupService(ViolationRepository violationRepo,
                          UserRepository userRepo) {
        this.violationRepo = violationRepo;
        this.userRepo = userRepo;
    }

    @Scheduled(cron = "0 0 * * * *") // every hour
    @Transactional
    public void cleanup() {

        LocalDateTime now = LocalDateTime.now();

        // Delete violations older than 3 hours
        violationRepo.deleteByTimestampBefore(
                now.minusHours(3)
        );

        // Delete student accounts older than 24 hours
        userRepo.deleteByRoleAndCreatedAtBefore(
                Role.STUDENT,
                now.minusHours(24)
        );

        System.out.println("Database cleaned at: " + now);
    }
}