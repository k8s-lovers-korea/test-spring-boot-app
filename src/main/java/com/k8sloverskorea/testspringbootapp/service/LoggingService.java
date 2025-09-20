package com.k8sloverskorea.testspringbootapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class LoggingService {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingService.class);
    
    private final AtomicLong counter = new AtomicLong(0);
    
    @Autowired
    private TestEntityService entityService;
    
    @Value("${app.logging.scheduled.enabled:true}")
    private boolean scheduledLoggingEnabled;
    
    @Scheduled(fixedRate = 30000) // Every 30 seconds
    public void heartbeatLog() {
        if (!scheduledLoggingEnabled) {
            return;
        }
        long count = counter.incrementAndGet();
        logger.info("Application heartbeat #{} - timestamp: {} - active threads: {}", 
                   count, Instant.now(), Thread.activeCount());
    }
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void systemStatusLog() {
        if (!scheduledLoggingEnabled) {
            return;
        }
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        logger.info("System Status - Memory: {}/{} MB used, Max: {} MB, Free: {} MB, Active Threads: {}", 
                   usedMemory / 1024 / 1024,
                   totalMemory / 1024 / 1024,
                   maxMemory / 1024 / 1024,
                   freeMemory / 1024 / 1024,
                   Thread.activeCount());
    }
    
    @Scheduled(fixedRate = 120000) // Every 2 minutes
    public void databaseStatusLog() {
        if (!scheduledLoggingEnabled) {
            return;
        }
        try {
            int entityCount = entityService.getAllEntities().size();
            logger.info("Database Status - Total entities in memory: {}", entityCount);
        } catch (Exception e) {
            logger.error("Failed to get database status", e);
        }
    }
    
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void detailedSystemLog() {
        if (!scheduledLoggingEnabled) {
            return;
        }
        logger.info("=== Detailed System Status ===");
        logger.info("Application: test-spring-boot-app");
        logger.info("Uptime: {} ms", java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime());
        logger.info("JVM: {} {} {}", 
                   System.getProperty("java.vm.name"),
                   System.getProperty("java.vm.version"),
                   System.getProperty("java.vm.vendor"));
        logger.info("OS: {} {} {}", 
                   System.getProperty("os.name"),
                   System.getProperty("os.version"),
                   System.getProperty("os.arch"));
        logger.info("Available Processors: {}", Runtime.getRuntime().availableProcessors());
        logger.info("================================");
    }
}