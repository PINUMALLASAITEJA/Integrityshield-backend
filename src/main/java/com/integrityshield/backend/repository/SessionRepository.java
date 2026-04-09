package com.integrityshield.backend.repository;

import com.integrityshield.backend.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {

    // ❌ OLD (kept for compatibility if needed)
    Optional<Session> findByStatus(String status);

    // ✅ NEW — HANDLE MULTIPLE ACTIVE SESSIONS (CRITICAL FIX)
    List<Session> findAllByStatus(String status);

    // ✅ EXISTING
    List<Session> findByStartTimeBetween(
            LocalDateTime start,
            LocalDateTime end
    );
}