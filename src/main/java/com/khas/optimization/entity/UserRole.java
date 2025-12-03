package com.khas.optimization.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * UserRole Entity for R2DBC
 * Junction table for many-to-many relationship between User and Role
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("user_roles")
public class UserRole {
    
    @Column("user_id")
    private Long userId;
    
    @Column("role_id")
    private Long roleId;
    
    @Column("created_at")
    private LocalDateTime createdAt;
    
    public UserRole(Long userId, Long roleId) {
        this.userId = userId;
        this.roleId = roleId;
        this.createdAt = LocalDateTime.now();
    }
}

