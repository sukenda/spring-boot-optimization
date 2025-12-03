package com.khas.optimization.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Convenience annotation for admin-only endpoints
 * Equivalent to: @RequiresRole(RoleConstants.ROLE_ADMIN)
 * 
 * Note: Annotation values must be compile-time constants, so we use string literal here.
 * See RoleConstants.ROLE_ADMIN for the constant value.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@RequiresRole("ROLE_ADMIN") // Must be string literal (compile-time constant)
public @interface RequiresAdmin {
}

