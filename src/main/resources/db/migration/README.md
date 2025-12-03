# Database Migrations

## Structure

Migrations are organized in versioned SQL files following the naming convention:

```
V{version}__{description}.sql
```

## Naming Convention

- **Version**: Sequential number (V1, V2, V3, ...)
- **Description**: Brief description using underscores
- **Example**: `V1__initial_schema.sql`, `V2__insert_default_roles.sql`

## Migration Files

| Version | File | Description |
|---------|------|-------------|
| V1 | `V1__initial_schema.sql` | Initial database schema (users, roles, user_roles) |
| V2 | `V2__insert_default_roles.sql` | Insert default system roles |
| V3 | `V3__create_audit_tables.sql` | Create audit logging tables |

## Running Migrations

### Option 1: Using Spring Boot SQL Init (Development)

Migrations run automatically on startup when `spring.sql.init.mode=always` (development profile).

### Option 2: Manual Execution (Production)

1. **Connect to MySQL:**
   ```bash
   mysql -u root -p devdb
   ```

2. **Run migrations in order:**
   ```sql
   source src/main/resources/db/migration/V1__initial_schema.sql;
   source src/main/resources/db/migration/V2__insert_default_roles.sql;
   source src/main/resources/db/migration/V3__create_audit_tables.sql;
   ```

3. **Or using mysql command:**
   ```bash
   mysql -u root -p devdb < src/main/resources/db/migration/V1__initial_schema.sql
   mysql -u root -p devdb < src/main/resources/db/migration/V2__insert_default_roles.sql
   mysql -u root -p devdb < src/main/resources/db/migration/V3__create_audit_tables.sql
   ```

### Option 3: Using Flyway (Recommended for Production)

1. **Add Flyway dependency to `build.gradle`:**
   ```gradle
   implementation 'org.flywaydb:flyway-core'
   implementation 'org.flywaydb:flyway-mysql'
   ```

2. **Configure in `application.yml`:**
   ```yaml
   spring:
     flyway:
       enabled: true
       locations: classpath:db/migration
       baseline-on-migrate: true
   ```

## Creating New Migrations

1. **Create new file** following naming convention:
   ```
   V{next_version}__{description}.sql
   ```

2. **Example:**
   ```sql
   -- Migration: V4 - Add user preferences table
   CREATE TABLE IF NOT EXISTS user_preferences (
       id BIGINT AUTO_INCREMENT PRIMARY KEY,
       user_id BIGINT NOT NULL,
       preference_key VARCHAR(100) NOT NULL,
       preference_value TEXT,
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
       UNIQUE KEY uk_user_preference (user_id, preference_key),
       FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
   ```

3. **Update this README** with the new migration information

## Best Practices

1. ✅ **Always use IF NOT EXISTS** for CREATE TABLE statements
2. ✅ **Use transactions** for data migrations
3. ✅ **Test migrations** on development database first
4. ✅ **Backup database** before running migrations in production
5. ✅ **Version control** all migration files
6. ✅ **Document changes** in migration file comments
7. ✅ **Use meaningful names** for migrations
8. ✅ **Keep migrations small** and focused on one change

## Rollback

For rollback, create reverse migrations:

```
V{version}_rollback__{description}.sql
```

Example: `V4_rollback__remove_user_preferences.sql`

## Migration Checklist

Before deploying migrations to production:

- [ ] Test on development database
- [ ] Review SQL for syntax errors
- [ ] Check for potential data loss
- [ ] Verify foreign key constraints
- [ ] Test rollback procedure
- [ ] Backup production database
- [ ] Document any breaking changes
- [ ] Notify team members

