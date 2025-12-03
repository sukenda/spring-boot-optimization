package com.khas.optimization.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.time.Instant;
import java.util.Map;

/**
 * Health and system info controller for monitoring resource usage.
 * Using Spring WebFlux for reactive programming.
 */
@Tag(name = "System", description = "System information and health endpoints")
@RestController
@RequestMapping("/api")
public class HealthController {

    private final Instant startTime = Instant.now();

    @Operation(
        summary = "Get system information",
        description = "Returns system and memory information"
    )
    @ApiResponse(
        responseCode = "200",
        description = "System information",
        content = @Content(mediaType = "application/json")
    )
    @GetMapping(value = "/system-info", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> systemInfo() {
        return Mono.fromCallable(() -> {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            Runtime runtime = Runtime.getRuntime();

            long heapUsed = memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
            long heapMax = memoryBean.getHeapMemoryUsage().getMax() / (1024 * 1024);
            long nonHeapUsed = memoryBean.getNonHeapMemoryUsage().getUsed() / (1024 * 1024);
            
            return Map.<String, Object>of(
                "heap_used_mb", heapUsed,
                "heap_max_mb", heapMax,
                "non_heap_used_mb", nonHeapUsed,
                "available_processors", runtime.availableProcessors(),
                "system_load_average", osBean.getSystemLoadAverage() != -1 ? osBean.getSystemLoadAverage() : 0.0,
                "uptime_seconds", java.time.Duration.between(startTime, Instant.now()).getSeconds(),
                "java_version", System.getProperty("java.version"),
                "virtual_threads_enabled", Thread.currentThread().isVirtual()
            );
        });
    }
    
    /**
     * Protected endpoint - requires JWT authentication
     */
    @Operation(
        summary = "Protected endpoint example",
        description = "Example protected endpoint that requires JWT authentication"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Protected endpoint accessed successfully",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - JWT token required"
        )
    })
    @SecurityRequirement(name = "bearer-jwt")
    @GetMapping(value = "/protected", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> protectedEndpoint(org.springframework.web.server.ServerWebExchange exchange) {
        String username = (String) exchange.getAttributes().get("username");
        String[] roles = (String[]) exchange.getAttributes().get("roles");
        
        return Mono.just(Map.of(
            "message", "This is a protected endpoint",
            "username", username != null ? username : "unknown",
            "roles", roles != null ? roles : new String[0],
            "timestamp", Instant.now().toString()
        ));
    }
}

