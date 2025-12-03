package com.khas.optimization.constants;

/**
 * Role Constants
 * Centralized role names to avoid hardcoded strings
 */
public final class RoleConstants {
    
    private RoleConstants() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Standard user role
     */
    public static final String ROLE_USER = "ROLE_USER";
    
    /**
     * Administrator role - Full access
     */
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    
    /**
     * Moderator role - Limited administrative access
     */
    public static final String ROLE_MODERATOR = "ROLE_MODERATOR";
    
    /**
     * Get all available roles
     * @return Array of all role constants
     */
    public static String[] getAllRoles() {
        return new String[]{
            ROLE_USER,
            ROLE_ADMIN,
            ROLE_MODERATOR
        };
    }
}

