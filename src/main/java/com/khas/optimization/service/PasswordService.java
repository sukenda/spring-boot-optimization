package com.khas.optimization.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * Password Service for hashing and verifying passwords
 */
@Service
public class PasswordService {
    
    private static final int MIN_LENGTH = 8;
    private static final Pattern STRONG_PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );
    
    private final PasswordEncoder passwordEncoder;
    
    public PasswordService() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }
    
    /**
     * Validate password strength
     * Password must be at least 8 characters and contain:
     * - At least one lowercase letter
     * - At least one uppercase letter
     * - At least one digit
     * - At least one special character (@$!%*?&)
     * 
     * @param password Plain text password to validate
     * @throws IllegalArgumentException if password doesn't meet strength requirements
     */
    public void validatePasswordStrength(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(
                "Password must be at least " + MIN_LENGTH + " characters long"
            );
        }
        
        if (!STRONG_PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException(
                "Password must contain at least one uppercase letter, one lowercase letter, " +
                "one digit, and one special character (@$!%*?&)"
            );
        }
    }
    
    /**
     * Hash a plain text password
     * Note: Password strength validation should be done before calling this method
     */
    public String hashPassword(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }
    
    /**
     * Verify if plain password matches hashed password
     */
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        return passwordEncoder.matches(plainPassword, hashedPassword);
    }
}

