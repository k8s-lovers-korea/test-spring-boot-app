package com.k8sloverskorea.testspringbootapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Endpoint(id = "restart-monitor")
public class RestartMonitorEndpoint {
    
    private static final Logger logger = LoggerFactory.getLogger(RestartMonitorEndpoint.class);
    
    private final AtomicLong restartCount = new AtomicLong(0);
    private Instant lastStartTime;
    private Instant applicationReadyTime;
    
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        long count = restartCount.incrementAndGet();
        lastStartTime = Instant.now();
        applicationReadyTime = Instant.now();
        
        logger.info("Application restart detected - count: {}, ready at: {}", count, applicationReadyTime);
    }
    
    @ReadOperation
    public Map<String, Object> restartInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("restartCount", restartCount.get());
        info.put("lastStartTime", lastStartTime != null ? lastStartTime.toString() : null);
        info.put("applicationReadyTime", applicationReadyTime != null ? applicationReadyTime.toString() : null);
        info.put("uptime", getUptime());
        info.put("currentTime", Instant.now().toString());
        
        return info;
    }
    
    private String getUptime() {
        if (applicationReadyTime == null) {
            return "Not started";
        }
        
        long uptimeSeconds = Instant.now().getEpochSecond() - applicationReadyTime.getEpochSecond();
        long hours = uptimeSeconds / 3600;
        long minutes = (uptimeSeconds % 3600) / 60;
        long seconds = uptimeSeconds % 60;
        
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}