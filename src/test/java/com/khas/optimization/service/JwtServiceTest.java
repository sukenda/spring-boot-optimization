package com.khas.optimization.service;

import com.khas.optimization.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtService
 */
@DisplayName("JwtService Tests")
class JwtServiceTest {
    
    private JwtService jwtService;
    private JwtProperties jwtProperties;
    
    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret("test-secret-key-minimum-256-bits-for-hmac-sha-256-algorithm-test");
        jwtProperties.setExpiration(86400000L); // 24 hours
        jwtProperties.setIssuer("test-issuer");
        jwtProperties.setAudience("test-audience");
        
        jwtService = new JwtService(jwtProperties);
    }
    
    @Test
    @DisplayName("Should generate token successfully")
    void testGenerateToken() {
        // Given
        String username = "testuser";
        
        // When
        String token = jwtService.generateToken(username);
        
        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }
    
    @Test
    @DisplayName("Should generate token with roles")
    void testGenerateTokenWithRoles() {
        // Given
        String username = "testuser";
        String[] roles = {"USER", "ADMIN"};
        
        // When
        String token = jwtService.generateToken(username, roles);
        
        // Then
        assertNotNull(token);
        String[] extractedRoles = jwtService.extractRoles(token);
        assertEquals(2, extractedRoles.length);
        assertArrayEquals(roles, extractedRoles);
    }
    
    @Test
    @DisplayName("Should generate token with extra claims")
    void testGenerateTokenWithExtraClaims() {
        // Given
        String username = "testuser";
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("customClaim", "customValue");
        
        // When
        String token = jwtService.generateToken(username, extraClaims);
        
        // Then
        assertNotNull(token);
        String extractedUsername = jwtService.extractUsername(token);
        assertEquals(username, extractedUsername);
    }
    
    @Test
    @DisplayName("Should extract username from token")
    void testExtractUsername() {
        // Given
        String username = "testuser";
        String token = jwtService.generateToken(username);
        
        // When
        String extractedUsername = jwtService.extractUsername(token);
        
        // Then
        assertEquals(username, extractedUsername);
    }
    
    @Test
    @DisplayName("Should extract expiration from token")
    void testExtractExpiration() {
        // Given
        String username = "testuser";
        String token = jwtService.generateToken(username);
        
        // When
        Date expiration = jwtService.extractExpiration(token);
        
        // Then
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }
    
    @Test
    @DisplayName("Should extract roles from token")
    void testExtractRoles() {
        // Given
        String username = "testuser";
        String[] roles = {"USER", "ADMIN", "MODERATOR"};
        String token = jwtService.generateToken(username, roles);
        
        // When
        String[] extractedRoles = jwtService.extractRoles(token);
        
        // Then
        assertNotNull(extractedRoles);
        assertEquals(3, extractedRoles.length);
        assertArrayEquals(roles, extractedRoles);
    }
    
    @Test
    @DisplayName("Should return empty array when no roles in token")
    void testExtractRolesNoRoles() {
        // Given
        String username = "testuser";
        String token = jwtService.generateToken(username);
        
        // When
        String[] extractedRoles = jwtService.extractRoles(token);
        
        // Then
        assertNotNull(extractedRoles);
        assertEquals(0, extractedRoles.length);
    }
    
    @Test
    @DisplayName("Should validate valid token")
    void testValidateTokenValid() {
        // Given
        String username = "testuser";
        String token = jwtService.generateToken(username);
        
        // When
        Boolean isValid = jwtService.validateToken(token);
        
        // Then
        assertTrue(isValid);
    }
    
    @Test
    @DisplayName("Should validate token with username")
    void testValidateTokenWithUsername() {
        // Given
        String username = "testuser";
        String token = jwtService.generateToken(username);
        
        // When
        Boolean isValid = jwtService.validateToken(token, username);
        
        // Then
        assertTrue(isValid);
    }
    
    @Test
    @DisplayName("Should reject token with wrong username")
    void testValidateTokenWrongUsername() {
        // Given
        String username = "testuser";
        String wrongUsername = "wronguser";
        String token = jwtService.generateToken(username);
        
        // When
        Boolean isValid = jwtService.validateToken(token, wrongUsername);
        
        // Then
        assertFalse(isValid);
    }
    
    @Test
    @DisplayName("Should reject invalid token")
    void testValidateTokenInvalid() {
        // Given
        String invalidToken = "invalid.token.here";
        
        // When
        Boolean isValid = jwtService.validateToken(invalidToken);
        
        // Then
        assertFalse(isValid);
    }
    
    @Test
    @DisplayName("Should reject empty token")
    void testValidateTokenEmpty() {
        // Given
        String emptyToken = "";
        
        // When
        Boolean isValid = jwtService.validateToken(emptyToken);
        
        // Then
        assertFalse(isValid);
    }
    
    @Test
    @DisplayName("Should reject null token")
    void testValidateTokenNull() {
        // Given
        String nullToken = null;
        
        // When & Then
        assertThrows(Exception.class, () -> {
            jwtService.validateToken(nullToken);
        });
    }
    
    @Test
    @DisplayName("Should generate different tokens for same user")
    void testGenerateTokenDifferentTokens() {
        // Given
        String username = "testuser";
        
        // When
        String token1 = jwtService.generateToken(username);
        String token2 = jwtService.generateToken(username);
        
        // Then
        assertNotEquals(token1, token2); // Different issuedAt times
    }
}

