package com.khas.optimization.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard API Response wrapper for success responses
 * 
 * @param <T> Type of the data payload
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    private Boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    
    /**
     * Create a successful response with data
     * 
     * @param data The response data
     * @return ApiResponse with success=true
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, null, data, LocalDateTime.now());
    }
    
    /**
     * Create a successful response with data and message
     * 
     * @param data The response data
     * @param message Success message
     * @return ApiResponse with success=true
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now());
    }
}


