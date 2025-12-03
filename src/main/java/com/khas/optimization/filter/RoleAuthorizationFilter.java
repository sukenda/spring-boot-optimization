package com.khas.optimization.filter;

import com.khas.optimization.annotation.RequiresRole;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * Role Authorization Filter
 * Checks if user has required roles based on @RequiresRole annotation
 * Must run after JwtAuthenticationFilter (order = HIGHEST_PRECEDENCE + 1)
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RoleAuthorizationFilter implements WebFilter {
    
    private final RequestMappingHandlerMapping handlerMapping;
    
    public RoleAuthorizationFilter(@Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Get handler method for current request
        return handlerMapping.getHandler(exchange)
                .cast(HandlerMethod.class)
                .flatMap(handlerMethod -> {
                    // Check for @RequiresRole annotation on method
                    RequiresRole methodAnnotation = handlerMethod.getMethodAnnotation(RequiresRole.class);
                    
                    // Check for @RequiresRole annotation on class
                    RequiresRole classAnnotation = handlerMethod.getBeanType().getAnnotation(RequiresRole.class);
                    
                    // Method annotation takes precedence over class annotation
                    RequiresRole annotation = methodAnnotation != null ? methodAnnotation : classAnnotation;
                    
                    // If no annotation, allow access
                    if (annotation == null) {
                        return chain.filter(exchange);
                    }
                    
                    // Get user roles from exchange attributes (set by JwtAuthenticationFilter)
                    String[] userRoles = (String[]) exchange.getAttributes().get("roles");
                    
                    if (userRoles == null || userRoles.length == 0) {
                        // No roles in token, deny access
                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }
                    
                    // Check if user has required roles
                    boolean hasAccess = checkRoles(userRoles, annotation.value(), annotation.requireAll());
                    
                    if (!hasAccess) {
                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }
                    
                    // User has required roles, continue
                    return chain.filter(exchange);
                })
                .switchIfEmpty(chain.filter(exchange)) // If no handler found, continue
                .onErrorResume(error -> {
                    // On error, deny access for safety
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                });
    }
    
    /**
     * Check if user has required roles
     * 
     * @param userRoles Roles from JWT token
     * @param requiredRoles Required roles from annotation
     * @param requireAll If true, user must have ALL roles (AND), if false, user must have at least ONE (OR)
     * @return true if user has access, false otherwise
     */
    private boolean checkRoles(String[] userRoles, String[] requiredRoles, boolean requireAll) {
        if (requiredRoles == null || requiredRoles.length == 0) {
            return true; // No requirements, allow access
        }
        
        List<String> userRolesList = Arrays.asList(userRoles);
        
        if (requireAll) {
            // AND logic: user must have ALL required roles
            return Arrays.stream(requiredRoles)
                    .allMatch(userRolesList::contains);
        } else {
            // OR logic: user must have at least ONE required role
            return Arrays.stream(requiredRoles)
                    .anyMatch(userRolesList::contains);
        }
    }
}

