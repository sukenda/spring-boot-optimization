package com.khas.optimization.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(HealthController.class)
class HealthControllerTests {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void systemInfoEndpointShouldReturnMemoryInfo() {
        webTestClient.get()
                .uri("/api/system-info")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.heap_used_mb").exists()
                .jsonPath("$.java_version").exists();
    }
}

