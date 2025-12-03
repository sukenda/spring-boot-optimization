package com.khas.optimization.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PasswordService
 */
@DisplayName("PasswordService Tests")
class PasswordServiceTest {
    
    private PasswordService passwordService;
    
    @BeforeEach
    void setUp() {
        passwordService = new PasswordService();
    }
    
    @Test
    @DisplayName("Should hash password successfully")
    void testHashPassword() {
        // Given
        String plainPassword = "testPassword123";
        
        // When
        String hashedPassword = passwordService.hashPassword(plainPassword);
        
        // Then
        assertNotNull(hashedPassword);
        assertNotEquals(plainPassword, hashedPassword);
        assertTrue(hashedPassword.length() > 50); // BCrypt hash is typically 60 characters
    }
    
    @Test
    @DisplayName("Should generate different hashes for same password")
    void testHashPasswordDifferentHashes() {
        // Given
        String plainPassword = "testPassword123";
        
        // When
        String hash1 = passwordService.hashPassword(plainPassword);
        String hash2 = passwordService.hashPassword(plainPassword);
        
        // Then
        assertNotEquals(hash1, hash2); // BCrypt generates different salts
    }
    
    @Test
    @DisplayName("Should verify correct password")
    void testVerifyPasswordCorrect() {
        // Given
        String plainPassword = "testPassword123";
        String hashedPassword = passwordService.hashPassword(plainPassword);
        
        // When
        boolean result = passwordService.verifyPassword(plainPassword, hashedPassword);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    @DisplayName("Should reject incorrect password")
    void testVerifyPasswordIncorrect() {
        // Given
        String plainPassword = "testPassword123";
        String wrongPassword = "wrongPassword";
        String hashedPassword = passwordService.hashPassword(plainPassword);
        
        // When
        boolean result = passwordService.verifyPassword(wrongPassword, hashedPassword);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    @DisplayName("Should handle empty password")
    void testHashEmptyPassword() {
        // Given
        String emptyPassword = "";
        
        // When
        String hashedPassword = passwordService.hashPassword(emptyPassword);
        
        // Then
        assertNotNull(hashedPassword);
        assertTrue(passwordService.verifyPassword(emptyPassword, hashedPassword));
    }
    
    @Test
    @DisplayName("Should handle null password gracefully")
    void testHashNullPassword() {
        // Given
        String nullPassword = null;
        
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            passwordService.hashPassword(nullPassword);
        });
    }
    
    @Test
    @DisplayName("Should verify password with special characters")
    void testPasswordWithSpecialCharacters() {
        // Given
        String passwordWithSpecialChars = "P@ssw0rd!#$%^&*()";
        
        // When
        String hashedPassword = passwordService.hashPassword(passwordWithSpecialChars);
        boolean result = passwordService.verifyPassword(passwordWithSpecialChars, hashedPassword);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    @DisplayName("Should verify password with unicode characters")
    void testPasswordWithUnicode() {
        // Given
        String passwordWithUnicode = "Pässwörd123";
        
        // When
        String hashedPassword = passwordService.hashPassword(passwordWithUnicode);
        boolean result = passwordService.verifyPassword(passwordWithUnicode, hashedPassword);
        
        // Then
        assertTrue(result);
    }
}

