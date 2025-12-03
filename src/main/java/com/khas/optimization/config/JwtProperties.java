package com.khas.optimization.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT Configuration Properties
 */
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    
    private String secret = "your-secret-key-change-this-in-production-minimum-256-bits";
    private long expiration = 86400000; // 24 hours in milliseconds
    private String issuer = "spring-boot-optimization";
    private String audience = "spring-boot-optimization-users";
    
    /**
     * Validate JWT secret key after properties are set
     * JWT secret must be at least 32 characters (256 bits) for HS256 algorithm
     */
    @PostConstruct
    public void validate() {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException(
                "JWT secret must be at least 32 characters (256 bits) for HS256 algorithm. " +
                "Current length: " + (secret != null ? secret.length() : 0) + ". " +
                "Please set JWT_SECRET environment variable with a secure key."
            );
        }
        
        if (expiration < 60000) { // Minimum 1 minute
            throw new IllegalStateException(
                "JWT expiration must be at least 60000 milliseconds (1 minute). " +
                "Current value: " + expiration
            );
        }
    }
    
    public String getSecret() {
        return secret;
    }
    
    public void setSecret(String secret) {
        this.secret = secret;
    }
    
    public long getExpiration() {
        return expiration;
    }
    
    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }
    
    public String getIssuer() {
        return issuer;
    }
    
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
    
    public String getAudience() {
        return audience;
    }
    
    public void setAudience(String audience) {
        this.audience = audience;
    }
}

