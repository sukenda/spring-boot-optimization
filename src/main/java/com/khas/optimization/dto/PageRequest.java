package com.khas.optimization.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

/**
 * Page request DTO for pagination parameters
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest {
    
    @Min(value = 0, message = "Page must be 0 or greater")
    private Integer page = 0;
    
    @Min(value = 1, message = "Size must be at least 1")
    @Max(value = 100, message = "Size must not exceed 100")
    private Integer size = 10;
    
    /**
     * Sort parameter in format: "field,direction"
     * Examples: "id,asc", "username,desc", "createdAt,asc"
     * Default: "id,asc"
     */
    private String sort = "id,asc";
    
    /**
     * Convert sort string to Spring Sort object
     * 
     * @return Sort object for Spring Data
     */
    public Sort getSort() {
        if (sort == null || sort.isEmpty()) {
            return Sort.by("id").ascending();
        }
        
        String[] parts = sort.split(",");
        if (parts.length < 1) {
            return Sort.by("id").ascending();
        }
        
        String field = parts[0].trim();
        String direction = parts.length > 1 ? parts[1].trim() : "asc";
        
        return direction.equalsIgnoreCase("desc") 
            ? Sort.by(field).descending() 
            : Sort.by(field).ascending();
    }
}


