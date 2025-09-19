package com.k8sloverskorea.testspringbootapp.controller;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@RestController
@RequestMapping("/api/test")
@Tag(name = "테스트 시나리오 API", description = "스레드 블로킹/행, CPU 부하 등 테스트용 API")
public class TestScenariosController {
    
    private static final Logger logger = LoggerFactory.getLogger(TestScenariosController.class);
    
    @Autowired
    private Tracer tracer;

    @Value("${server.tomcat.threads.max:5}")
    private int maxServerThreads;

    @Value("${server.port:8080}")
    private int serverPort;

    // Reusable HTTP client for internal fan-out calls
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    
    // Thread lock for testing deadlock scenarios
    private final ReentrantLock testLock = new ReentrantLock();
    
    // Track threads: waiting to acquire lock vs currently holding the lock
    private final Map<String, Thread> waitingThreads = new ConcurrentHashMap<>();
    private final Map<String, Thread> lockedThreads = new ConcurrentHashMap<>();
    
    @GetMapping("/health")
    @Operation(summary = "헬스 체크", description = "애플리케이션의 건강 상태와 기본 정보를 반환합니다.")
    public ResponseEntity<Map<String, String>> health() {
        logger.info("Health check endpoint called");
        Map<String, String> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("timestamp", java.time.Instant.now().toString());
        response.put("service", "test-spring-boot-app");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/block-thread")
    @Operation(summary = "스레드 블로킹 및 풀 소진", description = "한 번 호출해도 내부적으로 나머지 요청 스레드까지 동시에 호출하여 톰캣 요청 스레드 풀(기본 5개)을 모두 소진합니다. 각 스레드는 지정한 시간(초) 동안 락으로 블로킹됩니다. 대기/보유 스레드는 /api/test/thread-status에서 확인 가능.")
    public ResponseEntity<Map<String, String>> blockThread(
            @Parameter(description = "블로킹할 시간(초)") @RequestParam(defaultValue = "30") int seconds,
            @Parameter(hidden = true) @RequestParam(defaultValue = "false") boolean internal
    ) {
        Span span = tracer.spanBuilder("block-thread-endpoint").startSpan();
        String threadName = Thread.currentThread().getName();

        try {
            logger.warn("POST /api/test/block-thread - Blocking thread {} for {} seconds (internal={})", threadName, seconds, internal);

            // Fan-out: only for external (non-internal) trigger, spawn additional requests to exhaust pool
            if (!internal) {
                int toSpawn = Math.max(0, maxServerThreads - 1);
                try {
                    for (int i = 0; i < toSpawn; i++) {
                        String url = "http://localhost:" + serverPort + "/api/test/block-thread?seconds=" + seconds + "&internal=true";
                        HttpRequest req = HttpRequest.newBuilder()
                                .uri(URI.create(url))
                                .timeout(Duration.ofSeconds(Math.max(5, seconds + 5)))
                                .POST(HttpRequest.BodyPublishers.noBody())
                                .build();
                        httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                                .exceptionally(ex -> {
                                    logger.error("Internal fan-out request failed: {}", ex.toString());
                                    return null;
                                });
                    }
                    logger.info("Spawned {} internal requests to exhaust thread pool (max={})", toSpawn, maxServerThreads);
                } catch (Exception e) {
                    logger.error("Error during internal fan-out to exhaust threads", e);
                }
            }

            // Mark this thread as waiting for the lock (will be enqueued by ReentrantLock)
            waitingThreads.put(threadName, Thread.currentThread());

            try {
                // This call will block and enqueue the thread, increasing queueLength/hasQueuedThreads
                testLock.lock();

                // Once acquired, move from waiting to locked holder
                waitingThreads.remove(threadName);
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
            } finally {
                // Ensure cleanup in any case
                waitingThreads.remove(threadName);
                lockedThreads.remove(threadName);
            }

            Map<String, String> response = new HashMap<>();
            response.put("message", "Thread was blocked for " + seconds + " seconds");
            response.put("thread", threadName);
            response.put("duration", seconds + "s");
            response.put("internal", String.valueOf(internal));

            return ResponseEntity.ok(response);

        } finally {
            span.end();
        }
    }
    
    @PostMapping("/hang")
    @Operation(summary = "스레드 행(Hang)", description = "현재 요청 스레드를 지정한 시간(초) 동안 바쁜 대기 루프로 행 상태로 둡니다.")
    public ResponseEntity<Map<String, String>> hangThread(@Parameter(description = "행 상태로 둘 시간(초)") @RequestParam(defaultValue = "90") int seconds) {
        Span span = tracer.spanBuilder("hang-thread-endpoint").startSpan();
        String threadName = Thread.currentThread().getName();
        
        try {
            logger.warn("POST /api/test/hang - Hanging thread {} for {} seconds", threadName, seconds);
            
            // This will hang the thread for the specified duration
            long startTime = System.currentTimeMillis();
            long endTime = startTime + (seconds * 1000L);
            
            logger.info("Thread {} started hanging at {}", threadName, java.time.Instant.now());
            
            while (true) {
                long now = System.currentTimeMillis();
                long remaining = endTime - now;
                if (remaining <= 0) {
                    break;
                }
                long sleepMs = Math.min(10000L, remaining);
                try {
                    Thread.sleep(sleepMs);
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
    @Operation(summary = "스레드 상태 조회", description = "현재 애플리케이션의 전체/락 대기/락 보유 스레드 수와 락 상태를 반환합니다.")
    public ResponseEntity<Map<String, Object>> getThreadStatus() {
        logger.info("GET /api/test/thread-status - Checking thread status");

        Map<String, Object> response = new HashMap<>();
        response.put("totalThreads", Thread.activeCount());
        response.put("waitingThreads", waitingThreads.size());
        response.put("waitingThreadNames", waitingThreads.keySet());
        response.put("lockedThreads", lockedThreads.size());
        response.put("lockedThreadNames", lockedThreads.keySet());
        response.put("lockHeld", testLock.isLocked());
        response.put("hasQueuedThreads", testLock.hasQueuedThreads());
        response.put("queueLength", testLock.getQueueLength());
        response.put("timestamp", java.time.Instant.now().toString());

        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/cpu-intensive")
    @Operation(summary = "CPU 집약 작업", description = "지정한 시간(초) 동안 난수 계산으로 CPU 부하를 발생시킵니다.")
    public ResponseEntity<Map<String, String>> cpuIntensiveTask(@Parameter(description = "작업을 수행할 시간(초)") @RequestParam(defaultValue = "10") int seconds) {
        Span span = tracer.spanBuilder("cpu-intensive-endpoint").startSpan();
        String threadName = Thread.currentThread().getName();
        
        try {
            logger.warn("POST /api/test/cpu-intensive - Starting CPU intensive task on thread {} for {} seconds", threadName, seconds);
            
            long startTime = System.currentTimeMillis();
            long endTime = startTime + (seconds * 1000L);
            long counter = 0;
            double resultAccumulator = 0.0d;
            
            // CPU intensive task
            while (System.currentTimeMillis() < endTime) {
                // Perform some calculations to consume CPU and use the result
                resultAccumulator += Math.sqrt(Math.random() * 1_000_000);
                counter++;
                
                // Log every million iterations
                if (counter % 1_000_000 == 0) {
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
            response.put("resultChecksum", Long.toString((long) resultAccumulator));
            
            return ResponseEntity.ok(response);
            
        } finally {
            span.end();
        }
    }
}