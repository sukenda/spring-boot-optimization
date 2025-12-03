package com.khas.optimization.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Role Entity for R2DBC
 * Extends BaseEntity for auto-increment ID
 */
@Getter
@Setter
@Table("roles")
public class Role extends BaseEntity {
    
    @Column("name")
    private String name;
    
    @Column("description")
    private String description;
    
    // Constructors
    public Role() {
        super();
    }
    
    public Role(String name, String description) {
        super();
        this.name = name;
        this.description = description;
    }
}
