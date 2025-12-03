package com.khas.optimization.repository;

import com.khas.optimization.entity.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * User Repository (R2DBC Reactive)
 * All queries exclude soft-deleted records (deleted_at IS NULL)
 */
public interface UserRepository extends ReactiveCrudRepository<User, Long> {
    
    @Query("SELECT * FROM users WHERE username = :username AND deleted_at IS NULL")
    Mono<User> findByUsername(String username);
    
    @Query("SELECT * FROM users WHERE email = :email AND deleted_at IS NULL")
    Mono<User> findByEmail(String email);
    
    @Query("SELECT * FROM users WHERE username = :username AND enabled = true AND deleted_at IS NULL")
    Mono<User> findEnabledUserByUsername(String username);
    
    @Query("SELECT COUNT(*) FROM users WHERE username = :username AND deleted_at IS NULL")
    Mono<Long> countByUsername(String username);
    
    default Mono<Boolean> existsByUsername(String username) {
        return countByUsername(username).map(count -> count > 0);
    }
    
    @Query("SELECT COUNT(*) FROM users WHERE email = :email AND deleted_at IS NULL")
    Mono<Long> countByEmail(String email);
    
    default Mono<Boolean> existsByEmail(String email) {
        return countByEmail(email).map(count -> count > 0);
    }
    
    @Query("SELECT * FROM users WHERE deleted_at IS NULL")
    Flux<User> findAll();
    
    @Query("SELECT * FROM users WHERE id = :id AND deleted_at IS NULL")
    Mono<User> findById(Long id);
    
    @Query("SELECT COUNT(*) FROM users WHERE deleted_at IS NULL")
    Mono<Long> countAll();
}

