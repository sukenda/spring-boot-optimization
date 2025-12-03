# Deployment Guide

## Production Deployment

### Prerequisites

1. **Server Requirements**
    - OS: Linux (Ubuntu 20.04+ recommended)
    - RAM: Minimum 1GB (2GB recommended)
    - CPU: 1+ cores
    - Disk: 10GB+ free space

2. **Software Requirements**
    - Java 21+ (or GraalVM 21+ for native image)
    - MySQL 8.0+
    - Systemd (for service management)

### Deployment Options

#### Option 1: JAR Deployment (Recommended for flexibility)

**Steps:**

1. **Build JAR:**
   ```bash
   make build-prod
   # or
   ./gradlew clean build -Pprofile=prod
   ```

2. **Transfer to Server:**
   ```bash
   scp build/libs/spring-boot-optimization-0.0.1-SNAPSHOT.jar user@server:/opt/app/
   ```

3. **Create Systemd Service:**
   ```bash
   sudo nano /etc/systemd/system/spring-boot-optimization.service
   ```

   ```ini
   [Unit]
   Description=Spring Boot optimization
   After=network.target mysql.service

   [Service]
   Type=simple
   User=appuser
   WorkingDirectory=/opt/app
   ExecStart=/usr/bin/java \
     -Xms256m \
     -Xmx384m \
     -XX:MetaspaceSize=64m \
     -XX:MaxMetaspaceSize=128m \
     -Xss512k \
     -XX:+UseSerialGC \
     -XX:+DisableExplicitGC \
     -jar /opt/app/spring-boot-optimization-0.0.1-SNAPSHOT.jar \
     --spring.profiles.active=prod
   Restart=always
   RestartSec=10

   [Install]
   WantedBy=multi-user.target
   ```

4. **Start Service:**
   ```bash
   sudo systemctl daemon-reload
   sudo systemctl enable spring-boot-optimization
   sudo systemctl start spring-boot-optimization
   sudo systemctl status spring-boot-optimization
   ```

#### Option 2: Native Image Deployment (Fastest boot)

**Steps:**

1. **Build Native Image:**
   ```bash
   make build-native
   # or
   ./gradlew nativeCompile
   ```

2. **Transfer to Server:**
   ```bash
   scp build/native/nativeCompile/spring-boot-optimization user@server:/opt/app/
   chmod +x /opt/app/spring-boot-optimization
   ```

3. **Create Systemd Service:**
   ```ini
   [Unit]
   Description=Spring Boot optimization (Native)
   After=network.target mysql.service

   [Service]
   Type=simple
   User=appuser
   WorkingDirectory=/opt/app
   ExecStart=/opt/app/spring-boot-optimization --spring.profiles.active=prod
   Restart=always
   RestartSec=10

   [Install]
   WantedBy=multi-user.target
   ```

## Environment Configuration

### Production Environment Variables

Create `/opt/app/.env` or set system environment variables:

```bash
# Database
export DB_URL=r2dbc:mysql://localhost:3306/proddb?useSSL=true&serverTimezone=Asia/Jakarta
export DB_USERNAME=prod_user
export DB_PASSWORD=secure_password_here

# JWT
export JWT_SECRET=your-very-secure-secret-key-minimum-256-bits-for-hmac-sha256
export JWT_EXPIRATION=86400000
export JWT_ISSUER=spring-boot-optimization
export JWT_AUDIENCE=spring-boot-optimization-users

# Server
export SERVER_PORT=8080
export SPRING_PROFILES_ACTIVE=prod
```

### Database Setup

1. **Create Production Database:**
   ```sql
   CREATE DATABASE proddb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER 'prod_user'@'localhost' IDENTIFIED BY 'secure_password';
   GRANT ALL PRIVILEGES ON proddb.* TO 'prod_user'@'localhost';
   FLUSH PRIVILEGES;
   ```

2. **Run Migrations:**
   ```bash
   mysql -u prod_user -p proddb < src/main/resources/db/migration/V1__initial_schema.sql
   mysql -u prod_user -p proddb < src/main/resources/db/migration/V2__insert_default_roles.sql
   mysql -u prod_user -p proddb < src/main/resources/db/migration/V3__create_audit_tables.sql
   mysql -u prod_user -p proddb < src/main/resources/db/migration/V4__add_deleted_at_for_soft_delete.sql
   ```

## Security Hardening

### 1. Firewall Configuration

```bash
# Allow only necessary ports
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 8080/tcp  # Application
sudo ufw enable
```

### 2. Application User

```bash
# Create dedicated user
sudo useradd -r -s /bin/false appuser
sudo chown -R appuser:appuser /opt/app
```

### 3. SSL/TLS Configuration

For production, use reverse proxy (Nginx/Apache) with SSL:

```nginx
server {
    listen 443 ssl;
    server_name your-domain.com;
    
    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;
    
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### 4. JWT Secret

**IMPORTANT:** Change JWT secret in production:

```bash
# Generate secure secret (32+ characters)
openssl rand -base64 32
```

## Monitoring

### Health Checks

Configure health check endpoint:

```bash
curl http://localhost:8080/actuator/health
```

### Logging

Logs are written to:

- Console output
- `logs/application.log` (file)

Configure log rotation in production.

### Metrics

Access metrics:

```bash
curl http://localhost:8080/actuator/metrics
```

## Backup Strategy

### Database Backup

```bash
# Daily backup script
#!/bin/bash
mysqldump -u prod_user -p proddb > /backup/proddb_$(date +%Y%m%d).sql
```

### Application Backup

```bash
# Backup JAR/native image
cp /opt/app/spring-boot-optimization* /backup/
```

## Scaling

### Horizontal Scaling

1. Deploy multiple instances
2. Use load balancer (Nginx/HAProxy)
3. Configure session affinity (if needed)
4. Use shared database

### Vertical Scaling

1. Increase JVM heap size
2. Adjust connection pool size
3. Monitor resource usage
4. Optimize queries

## Rollback Procedure

### Application Rollback

1. Stop service: `sudo systemctl stop spring-boot-optimization`
2. Restore previous version
3. Start service: `sudo systemctl start spring-boot-optimization`

### Database Rollback

1. Review migration files
2. Create rollback migration
3. Test on staging first
4. Execute rollback migration

## Troubleshooting Production Issues

### Application Not Starting

1. Check logs: `sudo journalctl -u spring-boot-optimization -f`
2. Verify Java version
3. Check database connectivity
4. Verify environment variables

### High Memory Usage

1. Monitor with `/api/system-info`
2. Adjust JVM heap size
3. Check for memory leaks
4. Review connection pool settings

### Database Connection Issues

1. Verify MySQL is running
2. Check connection string
3. Verify user permissions
4. Check firewall rules

## Performance Tuning

### JVM Tuning

Adjust based on server resources:

```bash
-Xms256m -Xmx384m          # Heap size
-XX:MetaspaceSize=64m      # Metaspace
-XX:+UseSerialGC           # GC algorithm
```

### Database Tuning

- Optimize connection pool
- Add database indexes
- Monitor slow queries
- Configure MySQL buffer pool

## Maintenance

### Regular Tasks

1. **Daily:**
    - Monitor health endpoints
    - Check logs for errors
    - Verify backups

2. **Weekly:**
    - Review metrics
    - Check disk space
    - Update dependencies (if needed)

3. **Monthly:**
    - Security updates
    - Performance review
    - Database optimization

## Disaster Recovery

### Recovery Plan

1. **Database Failure:**
    - Restore from backup
    - Re-run migrations if needed
    - Verify data integrity

2. **Application Failure:**
    - Restore from backup
    - Verify configuration
    - Restart service

3. **Server Failure:**
    - Provision new server
    - Restore application
    - Restore database
    - Update DNS/load balancer

