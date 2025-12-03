package com.khas.optimization.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * User Entity for R2DBC
 * Extends BaseEntity for auto-increment ID
 */
@Getter
@Setter
@Table("users")
public class User extends BaseEntity {
    
    @Column("username")
    private String username;
    
    @Column("email")
    private String email;
    
    @Column("password_hash")
    private String passwordHash;
    
    @Column("enabled")
    private Boolean enabled;
    
    // Constructors
    public User() {
        super();
    }
    
    public User(String username, String email, String passwordHash) {
        super();
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.enabled = true;
    }
}
