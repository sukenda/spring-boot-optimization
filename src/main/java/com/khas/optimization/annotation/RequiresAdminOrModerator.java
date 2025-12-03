package com.khas.optimization.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Convenience annotation for admin or moderator endpoints
 * Equivalent to: @RequiresRole({RoleConstants.ROLE_ADMIN, RoleConstants.ROLE_MODERATOR})
 * 
 * Note: Annotation values must be compile-time constants, so we use string literals here.
 * See RoleConstants for the constant values.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@RequiresRole({"ROLE_ADMIN", "ROLE_MODERATOR"}) // Must be string literals (compile-time constants)
public @interface RequiresAdminOrModerator {
}

