package com.k8sloverskorea.testspringbootapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TestSpringBootApplication {

    private static final Logger logger = LoggerFactory.getLogger(TestSpringBootApplication.class);

    public static void main(String[] args) {
        logger.info("Starting Test Spring Boot Application...");
        SpringApplication.run(TestSpringBootApplication.class, args);
    }

    @EventListener(ApplicationStartedEvent.class)
    public void onApplicationStarted() {
        logger.info("Application started successfully - ready for test scenarios");
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("Application is ready to serve requests");
    }
}