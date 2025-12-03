package com.khas.optimization.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify required roles for accessing a method or class
 * 
 * Usage:
 * - @RequiresRole("ROLE_ADMIN") - Requires ADMIN role
 * - @RequiresRole({"ROLE_ADMIN", "ROLE_MODERATOR"}) - Requires ADMIN OR MODERATOR (OR logic)
 * - @RequiresRole(value = {"ROLE_ADMIN", "ROLE_MODERATOR"}, requireAll = true) - Requires ADMIN AND MODERATOR (AND logic)
 * 
 * Available roles (see RoleConstants):
 * - ROLE_USER - Standard user role
 * - ROLE_ADMIN - Administrator role
 * - ROLE_MODERATOR - Moderator role
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRole {
    /**
     * Array of required roles
     */
    String[] value();
    
    /**
     * If true, user must have ALL specified roles (AND logic)
     * If false, user must have at least ONE of the specified roles (OR logic)
     * Default: false (OR logic)
     */
    boolean requireAll() default false;
}

