package com.integrityshield.backend;

import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@SpringBootApplication
@EnableScheduling
public class IntegrityShieldApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntegrityShieldApplication.class, args);
    }
}