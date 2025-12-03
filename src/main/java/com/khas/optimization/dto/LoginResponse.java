package com.khas.optimization.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login Response DTO
 */
@Data
@NoArgsConstructor
public class LoginResponse {
    
    private Boolean success;
    private String token;
    private String type;
    private String username;
    private String message;
    
    /**
     * Constructor for successful login
     */
    public LoginResponse(Boolean success, String token, String type, String username) {
        this.success = success;
        this.token = token;
        this.type = type;
        this.username = username;
    }
    
    /**
     * Constructor for failed login
     */
    public LoginResponse(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
