package com.k8sloverskorea.testspringbootapp.controller;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@RestController
@RequestMapping("/api/test")
public class TestScenariosController {
    
    private static final Logger logger = LoggerFactory.getLogger(TestScenariosController.class);
    
    @Autowired
    private Tracer tracer;
    
    // Thread lock for testing deadlock scenarios
    private final ReentrantLock testLock = new ReentrantLock();
    
    // Track locked threads
    private final Map<String, Thread> lockedThreads = new ConcurrentHashMap<>();
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        logger.info("Health check endpoint called");
        Map<String, String> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("timestamp", java.time.Instant.now().toString());
        response.put("service", "test-spring-boot-app");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/block-thread")
    public ResponseEntity<Map<String, String>> blockThread(@RequestParam(defaultValue = "30") int seconds) {
        Span span = tracer.spanBuilder("block-thread-endpoint").startSpan();
        String threadName = Thread.currentThread().getName();
        
        try {
            logger.warn("POST /api/test/block-thread - Blocking thread {} for {} seconds", threadName, seconds);
            
            // Acquire lock to block the thread
            testLock.lock();
            lockedThreads.put(threadName, Thread.currentThread());
            
            try {
                logger.info("Thread {} acquired lock and will hold it for {} seconds", threadName, seconds);
                Thread.sleep(seconds * 1000L);
                logger.info("Thread {} releasing lock after {} seconds", threadName, seconds);
            } catch (InterruptedException e) {
                logger.error("Thread {} was interrupted while holding lock", threadName, e);
                Thread.currentThread().interrupt();
            } finally {
                lockedThreads.remove(threadName);
                testLock.unlock();
            }
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Thread was blocked for " + seconds + " seconds");
            response.put("thread", threadName);
            response.put("duration", seconds + "s");
            
            return ResponseEntity.ok(response);
            
        } finally {
            span.end();
        }
    }
    
    @PostMapping("/hang")
    public ResponseEntity<Map<String, String>> hangThread(@RequestParam(defaultValue = "90") int seconds) {
        Span span = tracer.spanBuilder("hang-thread-endpoint").startSpan();
        String threadName = Thread.currentThread().getName();
        
        try {
            logger.warn("POST /api/test/hang - Hanging thread {} for {} seconds", threadName, seconds);
            
            // This will hang the thread for the specified duration
            long startTime = System.currentTimeMillis();
            long endTime = startTime + (seconds * 1000L);
            
            logger.info("Thread {} started hanging at {}", threadName, java.time.Instant.now());
            
            while (System.currentTimeMillis() < endTime) {
                try {
                    // Log every 10 seconds to show the thread is still hanging
                    Thread.sleep(10000);
                    long elapsed = (System.currentTimeMillis() - startTime) / 1000;
                    logger.debug("Thread {} has been hanging for {} seconds", threadName, elapsed);
                } catch (InterruptedException e) {
                    logger.error("Thread {} was interrupted while hanging", threadName, e);
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            logger.info("Thread {} finished hanging after {} seconds", threadName, seconds);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Thread hung for " + seconds + " seconds");
            response.put("thread", threadName);
            response.put("duration", seconds + "s");
            response.put("completedAt", java.time.Instant.now().toString());
            
            return ResponseEntity.ok(response);
            
        } finally {
            span.end();
        }
    }
    
    @GetMapping("/thread-status")
    public ResponseEntity<Map<String, Object>> getThreadStatus() {
        logger.info("GET /api/test/thread-status - Checking thread status");
        
        Map<String, Object> response = new HashMap<>();
        response.put("totalThreads", Thread.activeCount());
        response.put("lockedThreads", lockedThreads.size());
        response.put("lockedThreadNames", lockedThreads.keySet());
        response.put("lockHeld", testLock.isLocked());
        response.put("timestamp", java.time.Instant.now().toString());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/cpu-intensive")
    public ResponseEntity<Map<String, String>> cpuIntensiveTask(@RequestParam(defaultValue = "10") int seconds) {
        Span span = tracer.spanBuilder("cpu-intensive-endpoint").startSpan();
        String threadName = Thread.currentThread().getName();
        
        try {
            logger.warn("POST /api/test/cpu-intensive - Starting CPU intensive task on thread {} for {} seconds", threadName, seconds);
            
            long startTime = System.currentTimeMillis();
            long endTime = startTime + (seconds * 1000L);
            long counter = 0;
            
            // CPU intensive task
            while (System.currentTimeMillis() < endTime) {
                // Perform some calculations to consume CPU
                Math.sqrt(Math.random() * 1000000);
                counter++;
                
                // Log every million iterations
                if (counter % 1000000 == 0) {
                    long elapsed = (System.currentTimeMillis() - startTime) / 1000;
                    logger.debug("CPU intensive task on thread {} - {} iterations, {} seconds elapsed", 
                               threadName, counter, elapsed);
                }
            }
            
            long totalTime = (System.currentTimeMillis() - startTime) / 1000;
            logger.info("CPU intensive task completed on thread {} - {} iterations in {} seconds", 
                       threadName, counter, totalTime);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "CPU intensive task completed");
            response.put("thread", threadName);
            response.put("iterations", String.valueOf(counter));
            response.put("duration", totalTime + "s");
            
            return ResponseEntity.ok(response);
            
        } finally {
            span.end();
        }
    }
}