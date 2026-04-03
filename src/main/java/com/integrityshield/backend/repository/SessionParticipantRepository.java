package com.integrityshield.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.integrityshield.backend.entity.SessionParticipant;

@Repository
public interface SessionParticipantRepository extends JpaRepository<SessionParticipant, Long> {
}