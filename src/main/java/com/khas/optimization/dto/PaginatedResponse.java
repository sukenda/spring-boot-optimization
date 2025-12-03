package com.khas.optimization.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Paginated API Response wrapper for list responses with pagination
 * 
 * @param <T> Type of the data items in the list
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaginatedResponse<T> {
    
    private Boolean success;
    private String message;
    private List<T> data;
    private PaginationMeta pagination;
    private LocalDateTime timestamp;
    
    /**
     * Pagination metadata
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationMeta {
        private Integer page;
        private Integer size;
        private Long totalElements;
        private Integer totalPages;
        private Boolean hasNext;
        private Boolean hasPrevious;
    }
    
    /**
     * Create a paginated response
     * 
     * @param data List of items for current page
     * @param page Current page number (0-indexed)
     * @param size Page size
     * @param totalElements Total number of elements
     * @return PaginatedResponse with pagination metadata
     */
    public static <T> PaginatedResponse<T> of(
            List<T> data, 
            int page, 
            int size, 
            long totalElements) {
        int totalPages = totalElements > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        
        PaginationMeta meta = new PaginationMeta(
            page,
            size,
            totalElements,
            totalPages,
            page < totalPages - 1,
            page > 0
        );
        
        return new PaginatedResponse<>(
            true,
            null,
            data,
            meta,
            LocalDateTime.now()
        );
    }
    
    /**
     * Create a paginated response with message
     * 
     * @param data List of items for current page
     * @param page Current page number (0-indexed)
     * @param size Page size
     * @param totalElements Total number of elements
     * @param message Success message
     * @return PaginatedResponse with pagination metadata and message
     */
    public static <T> PaginatedResponse<T> of(
            List<T> data, 
            int page, 
            int size, 
            long totalElements,
            String message) {
        int totalPages = totalElements > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        
        PaginationMeta meta = new PaginationMeta(
            page,
            size,
            totalElements,
            totalPages,
            page < totalPages - 1,
            page > 0
        );
        
        return new PaginatedResponse<>(
            true,
            message,
            data,
            meta,
            LocalDateTime.now()
        );
    }
}


