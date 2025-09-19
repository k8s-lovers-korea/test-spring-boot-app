package com.k8sloverskorea.testspringbootapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class TestSpringBootApplicationTests {

    @LocalServerPort
    private int port;

    @Test
    void contextLoads() {
        // Basic test to ensure the application context loads successfully
    }

    @Test
    void actuatorHealthEndpointIsAvailable() {
        TestRestTemplate restTemplate = new TestRestTemplate();
        String response = restTemplate.getForObject("http://localhost:" + port + "/actuator/health", String.class);
        // Basic check that health endpoint returns something
        assert response != null;
        assert response.contains("UP") || response.contains("status");
    }
}