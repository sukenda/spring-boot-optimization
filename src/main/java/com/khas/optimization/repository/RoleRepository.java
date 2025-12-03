package com.khas.optimization.repository;

import com.khas.optimization.entity.Role;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

/**
 * Role Repository (R2DBC Reactive)
 */
public interface RoleRepository extends ReactiveCrudRepository<Role, Long> {
    
    Mono<Role> findByName(String name);
}

