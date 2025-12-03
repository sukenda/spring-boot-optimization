package com.khas.optimization.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.khas.optimization.dto.ErrorResponse;
import com.khas.optimization.service.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JWT Authentication Filter for WebFlux
 * Validates JWT tokens from Authorization header
 */
@Component
public class JwtAuthenticationFilter implements WebFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private final List<String> publicPaths = List.of(
        "/api/auth/login",
        "/api/auth/validate",
        "/actuator/health",
        "/actuator/info",
        "/swagger-ui",
        "/swagger-ui.html",
        "/v3/api-docs",
        "/webjars"
    );
    
    public JwtAuthenticationFilter(JwtService jwtService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        
        // Skip JWT validation for public paths
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }
        
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Unauthorized access attempt to {} - Missing or invalid Authorization header", path);
            return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }
        
        String token = authHeader.substring(7);
        
        if (!jwtService.validateToken(token)) {
            logger.warn("Unauthorized access attempt to {} - Invalid or expired token", path);
            return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
        
        // Add username to request attributes for use in controllers
        String username = jwtService.extractUsername(token);
        exchange.getAttributes().put("username", username);
        exchange.getAttributes().put("roles", jwtService.extractRoles(token));
        
        return chain.filter(exchange);
    }
    
    /**
     * Write error response with JSON body
     */
    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, HttpStatus status, String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        
        ErrorResponse error = new ErrorResponse(
            status.value(),
            status.getReasonPhrase(),
            message,
            exchange.getRequest().getPath().value()
        );
        
        try {
            String json = objectMapper.writeValueAsString(error);
            DataBuffer buffer = exchange.getResponse().bufferFactory()
                .wrap(json.getBytes(StandardCharsets.UTF_8));
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            logger.error("Error serializing error response", e);
            return exchange.getResponse().setComplete();
        }
    }
    
    private boolean isPublicPath(String path) {
        return publicPaths.stream().anyMatch(path::startsWith);
    }
}

