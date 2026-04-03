package com.integrityshield.backend.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.integrityshield.backend.entity.User;
import com.integrityshield.backend.entity.Role;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserIdentifier(String userIdentifier);

    boolean existsByUserIdentifier(String userIdentifier);

    @Modifying
    @Transactional
    void deleteByRoleAndCreatedAtBefore(
            Role role,
            LocalDateTime time
    );
}