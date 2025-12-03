# Troubleshooting Guide

## Common Issues and Solutions

### Application Won't Start

#### Issue: Port Already in Use

**Error:**

```
Port 8080 is already in use
```

**Solution:**

```bash
# Change port
export SERVER_PORT=8081
make run-dev

# Or kill process using port
lsof -ti:8080 | xargs kill -9
```

#### Issue: Java Version Mismatch

**Error:**

```
Unsupported class file major version
```

**Solution:**

```bash
# Check Java version
java -version

# Should be Java 21+
# Update JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
```

#### Issue: Database Connection Failed

**Error:**

```
Connection refused
```

**Solution:**

1. Check MySQL is running:
   ```bash
   sudo systemctl status mysql
   sudo systemctl start mysql
   ```

2. Verify connection string:
   ```bash
   echo $DB_URL
   ```

3. Test connection:
   ```bash
   mysql -u root -p -h localhost
   ```

### Build Issues

#### Issue: Gradle Build Fails

**Error:**

```
Build failed with unknown error
```

**Solution:**

```bash
# Clean build
make clean
make build

# Or
./gradlew clean build --refresh-dependencies
```

#### Issue: Dependency Resolution Failed

**Error:**

```
Could not resolve dependencies
```

**Solution:**

```bash
# Clear Gradle cache
rm -rf ~/.gradle/caches/

# Rebuild
./gradlew clean build --refresh-dependencies
```

### Runtime Issues

#### Issue: Out of Memory

**Error:**

```
java.lang.OutOfMemoryError: Java heap space
```

**Solution:**

1. Increase heap size:
   ```bash
   java -Xmx512m -jar app.jar
   ```

2. Check memory usage:
   ```bash
   curl http://localhost:8080/api/system-info
   ```

3. Review memory settings in `run.sh`

#### Issue: Slow Performance

**Symptoms:**

- Slow response times
- High CPU usage
- High memory usage

**Solution:**

1. Check system resources:
   ```bash
   curl http://localhost:8080/api/system-info
   ```

2. Review connection pool settings
3. Check database performance
4. Enable native image for faster boot

#### Issue: JWT Token Invalid

**Error:**

```
401 Unauthorized
```

**Solution:**

1. Verify token is included:
   ```bash
   curl -H "Authorization: Bearer YOUR_TOKEN" http://localhost:8080/api/protected
   ```

2. Check token expiration
3. Verify JWT secret matches
4. Regenerate token if needed

### Database Issues

#### Issue: Migration Failed

**Error:**

```
Migration failed
```

**Solution:**

1. Check migration file syntax
2. Verify database permissions
3. Check for conflicting migrations
4. Review migration logs

#### Issue: Soft Delete Not Working

**Symptoms:**

- Deleted records still appear

**Solution:**

1. Verify `deleted_at` column exists:
   ```sql
   DESCRIBE users;
   ```

2. Check migration V4 was executed
3. Verify repository queries include `deleted_at IS NULL`

### Native Image Issues

#### Issue: Native Image Build Fails

**Error:**

```
Native image build failed
```

**Solution:**

1. Verify GraalVM is installed:
   ```bash
   java -version
   # Should show GraalVM
   ```

2. Check native-image tool:
   ```bash
   native-image --version
   ```

3. Review reflection configuration
4. Check for unsupported features

#### Issue: Native Image Runtime Error

**Error:**

```
Runtime error in native image
```

**Solution:**

1. Check reflection configuration
2. Verify all resources are included
3. Review native image logs
4. Test with JAR first

## Debugging Tips

### Enable Debug Logging

```yaml
logging:
  level:
    root: DEBUG
    com.khas: DEBUG
```

### Check Logs

```bash
# Application logs
tail -f logs/application.log

# System logs (systemd)
sudo journalctl -u spring-boot-optimization -f
```

### Use Actuator

Access debugging endpoints:

```bash
# Environment
curl http://localhost:8080/actuator/env

# Configuration
curl http://localhost:8080/actuator/configprops

# Beans
curl http://localhost:8080/actuator/beans
```

### Database Debugging

```sql
-- Check tables
SHOW
TABLES;

-- Check user table
SELECT *
FROM users LIMIT 10;

-- Check soft-deleted records
SELECT *
FROM users
WHERE deleted_at IS NOT NULL;
```

## Getting Help

### Check Documentation

1. [Main README](../README.md)
2. [Configuration Guide](../CONFIG.md)
3. [Database Guide](../DATABASE.md)
4. [API Documentation](./API.md)

### Common Commands

```bash
# Check application status
curl http://localhost:8080/actuator/health

# Check system info
curl http://localhost:8080/api/system-info

# View logs
tail -f logs/application.log

# Restart application
sudo systemctl restart spring-boot-optimization
```

## Still Having Issues?

1. Check application logs
2. Review error messages
3. Verify configuration
4. Test with minimal setup
5. Check GitHub issues
6. Submit new issue with:
    - Error message
    - Steps to reproduce
    - Environment details
    - Logs (if applicable)

