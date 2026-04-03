package com.integrityshield.backend.repository;

import com.integrityshield.backend.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {

    Optional<Session> findByStatus(String status);

    List<Session> findByStartTimeBetween(
            LocalDateTime start,
            LocalDateTime end
    );
}