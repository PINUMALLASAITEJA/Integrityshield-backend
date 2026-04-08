package com.integrityshield.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication

// 🔥 FORCE SCAN EVERYTHING (CRITICAL FIX)
@ComponentScan(basePackages = "com.integrityshield.backend")

public class IntegrityShieldApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntegrityShieldApplication.class, args);
    }
}