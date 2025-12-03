package com.khas.optimization.repository;

import com.khas.optimization.entity.UserRole;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * UserRole Repository (R2DBC Reactive)
 * Note: R2DBC doesn't support composite keys directly, so we use queries for insert/delete
 */
public interface UserRoleRepository extends ReactiveCrudRepository<UserRole, Long> {
    
    /**
     * Find all roles for a user
     */
    @Query("SELECT * FROM user_roles WHERE user_id = :userId")
    Flux<UserRole> findByUserId(Long userId);
    
    /**
     * Find all users for a role
     */
    @Query("SELECT * FROM user_roles WHERE role_id = :roleId")
    Flux<UserRole> findByRoleId(Long roleId);
    
    /**
     * Delete all roles for a user
     */
    @Query("DELETE FROM user_roles WHERE user_id = :userId")
    Mono<Void> deleteByUserId(Long userId);
    
    /**
     * Delete a specific user-role relationship
     */
    @Query("DELETE FROM user_roles WHERE user_id = :userId AND role_id = :roleId")
    Mono<Void> deleteByUserIdAndRoleId(Long userId, Long roleId);
    
    /**
     * Check if user-role relationship exists
     */
    @Query("SELECT COUNT(*) > 0 FROM user_roles WHERE user_id = :userId AND role_id = :roleId")
    Mono<Boolean> existsByUserIdAndRoleId(Long userId, Long roleId);
    
    /**
     * Insert user role (for composite key handling)
     * Using INSERT IGNORE to handle duplicates gracefully
     */
    @Query("INSERT IGNORE INTO user_roles (user_id, role_id, created_at) VALUES (:userId, :roleId, NOW())")
    Mono<Void> insertUserRole(Long userId, Long roleId);
}

