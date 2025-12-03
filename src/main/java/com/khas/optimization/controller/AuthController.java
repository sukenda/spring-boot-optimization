package com.khas.optimization.controller;

import com.khas.optimization.dto.LoginRequest;
import com.khas.optimization.dto.LoginResponse;
import com.khas.optimization.service.JwtService;
import com.khas.optimization.service.PasswordService;
import com.khas.optimization.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Authentication Controller
 * Provides endpoints for JWT token generation
 */
@Tag(name = "Authentication", description = "Authentication endpoints for JWT token generation and validation")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final JwtService jwtService;
    private final UserService userService;
    private final PasswordService passwordService;
    
    public AuthController(JwtService jwtService, UserService userService, PasswordService passwordService) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.passwordService = passwordService;
    }
    
    /**
     * Login endpoint - validates credentials and generates JWT token
     */
    @Operation(
        summary = "User login",
        description = "Authenticates user credentials and returns JWT token"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid credentials",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))
        )
    })
    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Login attempt for username: {}", request.getUsername());
        
        return userService.getUserByUsername(request.getUsername())
                .flatMap(user -> {
                    // Verify password
                    if (passwordService.verifyPassword(request.getPassword(), user.getPasswordHash())) {
                        logger.info("Successful login for username: {}", request.getUsername());
                        
                        // Get user roles from database
                        return userService.getUserRoles(user.getId())
                                .map(role -> role.getName())
                                .collectList()
                                .flatMap(roleNames -> {
                                    // Convert to role names array
                                    String[] roleNamesArray = roleNames.isEmpty() 
                                            ? new String[0] 
                                            : roleNames.toArray(new String[0]);
                                    
                                    // Generate token with roles from database
                                    String token;
                                    if (roleNamesArray.length > 0) {
                                        token = jwtService.generateToken(user.getUsername(), roleNamesArray);
                                    } else {
                                        // If no roles found, generate token without roles
                                        token = jwtService.generateToken(user.getUsername());
                                    }
                                    
                                    LoginResponse response = new LoginResponse(
                                            true,
                                            token,
                                            "Bearer",
                                            user.getUsername()
                                    );
                                    
                                    return Mono.just(ResponseEntity.ok(response));
                                });
                    } else {
                        logger.warn("Failed login attempt for username: {} - Invalid password", request.getUsername());
                        LoginResponse response = new LoginResponse(
                                false,
                                "Invalid username or password"
                        );
                        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response));
                    }
                })
                .switchIfEmpty(Mono.defer(() -> {
                    logger.warn("Login attempt for non-existent username: {}", request.getUsername());
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new LoginResponse(false, "Invalid username or password")));
                }))
                .onErrorResume(error -> {
                    logger.error("Login failed for username: {} - Error: {}", request.getUsername(), error.getMessage(), error);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new LoginResponse(false, "Login failed: " + error.getMessage())));
                });
    }
    
    /**
     * Validate token endpoint
     */
    @Operation(
        summary = "Validate JWT token",
        description = "Validates a JWT token and returns token information"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Token validation result",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping(value = "/validate", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.just(Map.of(
                "valid", false,
                "message", "Invalid authorization header"
            ));
        }
        
        String token = authHeader.substring(7);
        boolean isValid = jwtService.validateToken(token);
        
        if (isValid) {
            String username = jwtService.extractUsername(token);
            String[] roles = jwtService.extractRoles(token);
            
            return Mono.just(Map.of(
                "valid", true,
                "username", username,
                "roles", roles
            ));
        }
        
        return Mono.just(Map.of(
            "valid", false,
            "message", "Token is invalid or expired"
        ));
    }
}

