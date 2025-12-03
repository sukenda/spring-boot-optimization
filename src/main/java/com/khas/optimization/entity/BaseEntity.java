package com.khas.optimization.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;

/**
 * Base Entity for entities with auto-increment ID (Long)
 * Provides common fields: id, createdAt, updatedAt, deletedAt (soft delete)
 */
@Getter
@Setter
public abstract class BaseEntity {
    
    @Id
    private Long id;
    
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @Column("updated_at")
    private LocalDateTime updatedAt;
    
    @Column("deleted_at")
    private LocalDateTime deletedAt;
    
    /**
     * Initialize timestamps before save
     */
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
        this.deletedAt = null; // Ensure deletedAt is null on creation
    }
    
    /**
     * Update timestamp before update
     */
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Soft delete - set deletedAt timestamp
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Restore soft deleted entity
     */
    public void restore() {
        this.deletedAt = null;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Check if entity is soft deleted
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
