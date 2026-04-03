package com.integrityshield.backend.repository;

import com.integrityshield.backend.entity.AllowedApp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AllowedAppRepository extends JpaRepository<AllowedApp, Long> {

    Optional<AllowedApp> findByAppName(String appName);

    void deleteByAppName(String appName);
}