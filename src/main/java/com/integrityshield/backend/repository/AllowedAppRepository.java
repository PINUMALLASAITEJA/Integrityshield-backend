package com.integrityshield.backend.repository;

import com.integrityshield.backend.entity.AllowedApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface AllowedAppRepository extends JpaRepository<AllowedApp, Long> {

    Optional<AllowedApp> findByAppNameIgnoreCase(String appName);

    // 🔥 FINAL FIX (MANUAL QUERY)
    @Modifying
    @Transactional
    @Query("DELETE FROM AllowedApp a WHERE LOWER(a.appName) = LOWER(:appName)")
    void deleteAppIgnoreCase(String appName);
}