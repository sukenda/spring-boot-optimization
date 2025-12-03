# Entity Base Classes

## BaseEntity

Base class for entities with **auto-increment ID (Long)**.

**Features:**
- `id` (Long) - Auto-increment primary key
- `createdAt` (LocalDateTime) - Creation timestamp
- `updatedAt` (LocalDateTime) - Last update timestamp
- `deletedAt` (LocalDateTime) - Soft delete timestamp (null if not deleted)
- `prePersist()` - Initialize timestamps before save
- `preUpdate()` - Update timestamp before update
- `softDelete()` - Soft delete entity (sets deletedAt)
- `restore()` - Restore soft-deleted entity
- `isDeleted()` - Check if entity is soft deleted
- **Lombok**: Uses `@Getter` and `@Setter` annotations

**Usage Example:**
```java
@Table("users")
public class User extends BaseEntity {
    @Column("username")
    private String username;
    
    // ... other fields
    
    // Before saving:
    user.prePersist(); // Sets createdAt and updatedAt
}
```

**Current Entities Using BaseEntity:**
- `User` - User entity
- `Role` - Role entity

## BaseUuidEntity

Base class for entities with **UUID as primary key**.

**Features:**
- `id` (UUID) - UUID primary key (auto-generated)
- `createdAt` (LocalDateTime) - Creation timestamp
- `updatedAt` (LocalDateTime) - Last update timestamp
- `deletedAt` (LocalDateTime) - Soft delete timestamp (null if not deleted)
- `prePersist()` - Generate UUID and initialize timestamps
- `preUpdate()` - Update timestamp before update
- `softDelete()` - Soft delete entity (sets deletedAt)
- `restore()` - Restore soft-deleted entity
- `isDeleted()` - Check if entity is soft deleted
- **Lombok**: Uses `@Getter` and `@Setter` annotations

**Usage Example:**
```java
@Table("sessions")
public class Session extends BaseUuidEntity {
    @Column("user_id")
    private Long userId;
    
    @Column("token")
    private String token;
    
    // ... other fields
    
    // Before saving:
    session.prePersist(); // Generates UUID and sets timestamps
}
```

## When to Use Which?

### Use BaseEntity (Long ID) when:
- ✅ You need auto-increment IDs
- ✅ Database handles ID generation
- ✅ Better for performance (smaller index size)
- ✅ Traditional relational database design

### Use BaseUuidEntity (UUID) when:
- ✅ You need globally unique identifiers
- ✅ Distributed systems (no ID conflicts)
- ✅ Security (non-sequential IDs)
- ✅ Microservices architecture
- ✅ Better for replication/sharding

## Database Schema

### For BaseEntity (Long ID):
```sql
CREATE TABLE example_table (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    -- other columns
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL DEFAULT NULL,
    INDEX idx_deleted_at (deleted_at)
);
```

### For BaseUuidEntity (UUID):
```sql
CREATE TABLE example_table (
    id CHAR(36) PRIMARY KEY, -- or VARCHAR(36) or BINARY(16)
    -- other columns
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL DEFAULT NULL,
    INDEX idx_deleted_at (deleted_at)
);
```

## Soft Delete

Both `BaseEntity` and `BaseUuidEntity` support **soft delete** functionality:

```java
// Soft delete an entity
user.softDelete(); // Sets deletedAt timestamp

// Check if deleted
if (user.isDeleted()) {
    // Handle deleted entity
}

// Restore deleted entity
user.restore(); // Clears deletedAt
```

**Repository Queries:**
- All repository queries automatically exclude soft-deleted records (`deleted_at IS NULL`)
- Use `findById()` and other methods - they only return non-deleted entities
- For hard delete, use `delete()` method directly (use with caution!)

## Lombok Integration

All entities use **Lombok** annotations to reduce boilerplate code:

- `@Getter` - Auto-generates getter methods
- `@Setter` - Auto-generates setter methods

**Benefits:**
- ✅ Less boilerplate code
- ✅ Cleaner entity classes
- ✅ Automatic getter/setter generation
- ✅ IDE support (IntelliJ, Eclipse)

## Best Practices

1. ✅ Always call `prePersist()` before saving new entities
2. ✅ Always call `preUpdate()` before updating entities
3. ✅ Use `softDelete()` instead of hard delete when possible
4. ✅ Don't manually set `createdAt` after first save
5. ✅ Let `prePersist()` handle UUID generation (for BaseUuidEntity)
6. ✅ Use appropriate base class based on your requirements
7. ✅ All queries automatically filter soft-deleted records

