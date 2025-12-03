package com.khas.optimization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot Application optimized for low-resource servers.
 * 
 * Optimizations applied:
 * - Lazy initialization enabled
 * - Virtual threads enabled (Java 21+)
 * - Undertow server (lighter than Tomcat)
 * - Minimal dependencies
 * - JVM tuning for 1GB RAM
 */
@SpringBootApplication
public class SpringBootOptimizationApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SpringBootOptimizationApplication.class);
        app.run(args);
    }
}

