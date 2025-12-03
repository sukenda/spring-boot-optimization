package com.khas.optimization.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * User Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    
    private Long id;
    private String username;
    private String email;
    private Boolean enabled;
    private List<String> roles; // List of role names
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructor without roles (for backward compatibility)
    public UserResponse(Long id, String username, String email, Boolean enabled, 
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.enabled = enabled;
        this.roles = List.of(); // Empty list by default
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
