package com.integrityshield.backend.repository;

import com.integrityshield.backend.entity.AllowedUrl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AllowedUrlRepository extends JpaRepository<AllowedUrl, Long> {

    Optional<AllowedUrl> findByUrl(String url);

    void deleteByUrl(String url);
}