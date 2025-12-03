# Deployment Guide - Ubuntu Server dengan Systemd

Panduan lengkap untuk deploy Spring Boot optimization di Ubuntu server menggunakan systemd.

## Prerequisites

### Server Requirements
- **OS**: Ubuntu 20.04+ atau 22.04 LTS
- **RAM**: Minimum 1GB (2GB recommended)
- **CPU**: 1+ cores
- **Disk**: 10GB+ free space

### Software Requirements
- **Java 21+** (OpenJDK atau Temurin)
- **MySQL 8.0+** (untuk database)
- **Systemd** (sudah included di Ubuntu)

## Step 1: Install Java 21

```bash
# Update package list
sudo apt update

# Install OpenJDK 21
sudo apt install -y openjdk-21-jdk

# Verify installation
java -version
# Should show: openjdk version "21.x.x"
```

**Atau menggunakan SDKMAN (jika sudah terinstall):**
```bash
sdk install java 21.0.1-tem
sdk use java 21.0.1-tem
```

## Step 2: Setup Database (MySQL)

```bash
# Install MySQL
sudo apt install -y mysql-server

# Secure MySQL installation
sudo mysql_secure_installation

# Create database and user
sudo mysql -u root -p << EOF
CREATE DATABASE proddb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'prod_user'@'localhost' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON proddb.* TO 'prod_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
EOF
```

## Step 3: Build JAR File

**Di development machine:**
```bash
# Check current version
make version
# Output: Current version: 1.0.0

# Build production JAR
make build-prod
# atau
./gradlew clean build -x test -x processAot -Pprofile=prod

# JAR akan ada di: build/libs/spring-boot-optimization-1.0.0.jar
# Version diambil dari: git tag > version.properties > build.gradle
```

**Version Management:**
- Lihat [VERSIONING.md](../VERSIONING.md) untuk panduan lengkap
- Quick: `make version-bump` untuk bump patch version
- Quick: `make version-release` untuk release version

## Step 4: Transfer JAR ke Server

```bash
# Get version
VERSION=$(make version | grep "Current version" | awk '{print $3}')

# Copy JAR ke server
scp build/libs/spring-boot-optimization-${VERSION}.jar user@server:/tmp/

# Atau menggunakan rsync
rsync -avz build/libs/spring-boot-optimization-${VERSION}.jar user@server:/tmp/
```

## Step 5: Setup Systemd Service

### Opsi A: Menggunakan Setup Script (Recommended)

```bash
# Di server, copy deployment files
scp -r deployment/ user@server:/tmp/

# SSH ke server
ssh user@server

# Run setup script
cd /tmp/deployment
sudo chmod +x setup-systemd.sh
sudo ./setup-systemd.sh
```

### Opsi B: Manual Setup

**1. Create application user:**
```bash
sudo useradd -r -s /bin/false -d /opt/spring-boot-optimization springboot
```

**2. Create application directory:**
```bash
sudo mkdir -p /opt/spring-boot-optimization/{logs,config}
sudo chown -R springboot:springboot /opt/spring-boot-optimization
```

**3. Copy JAR file:**
```bash
sudo cp /tmp/spring-boot-optimization-0.0.1-SNAPSHOT.jar /opt/spring-boot-optimization/
sudo chown springboot:springboot /opt/spring-boot-optimization/*.jar
```

**4. Install systemd service:**
```bash
sudo cp deployment/spring-boot-optimization.service /etc/systemd/system/
sudo chmod 644 /etc/systemd/system/spring-boot-optimization.service
```

**5. Update JAVA_HOME in service file:**
```bash
# Find Java path
JAVA_HOME=$(readlink -f $(which java) | sed "s:bin/java::" | sed 's:/jre$::')

# Update service file
sudo sed -i "s|JAVA_HOME=.*|JAVA_HOME=$JAVA_HOME|" /etc/systemd/system/spring-boot-optimization.service
```

**6. Configure environment variables:**
```bash
# Edit service file to set your database credentials and JWT secret
sudo nano /etc/systemd/system/spring-boot-optimization.service

# Atau create environment file
sudo mkdir -p /etc/spring-boot-optimization
sudo nano /etc/spring-boot-optimization/application-prod.env
```

**7. Reload systemd:**
```bash
sudo systemctl daemon-reload
```

## Step 6: Configure Environment Variables

**Edit service file atau create environment file:**

```bash
sudo nano /etc/systemd/system/spring-boot-optimization.service
```

**Update environment variables:**
```ini
Environment="DB_URL=r2dbc:mysql://localhost:3306/proddb?useSSL=true"
Environment="DB_USERNAME=prod_user"
Environment="DB_PASSWORD=your_secure_password"

Environment="JWT_SECRET=your-very-secure-secret-key-minimum-256-bits"
Environment="JWT_EXPIRATION=86400000"
```

**⚠️ IMPORTANT:** 
- Gunakan password yang kuat untuk database
- Gunakan secret key yang aman untuk JWT (minimal 256 bits)
- Jangan commit secrets ke git!

## Step 7: Start Service

```bash
# Start service
sudo systemctl start spring-boot-optimization

# Enable auto-start on boot
sudo systemctl enable spring-boot-optimization

# Check status
sudo systemctl status spring-boot-optimization
```

## Step 8: Verify Deployment

```bash
# Check if service is running
sudo systemctl is-active spring-boot-optimization
# Should return: active

# Check logs
sudo journalctl -u spring-boot-optimization -f

# Test API
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/system-info
```

## Service Management Commands

### Start/Stop/Restart
```bash
sudo systemctl start spring-boot-optimization
sudo systemctl stop spring-boot-optimization
sudo systemctl restart spring-boot-optimization
```

### Status & Logs
```bash
# Check status
sudo systemctl status spring-boot-optimization

# View logs (last 100 lines)
sudo journalctl -u spring-boot-optimization -n 100

# Follow logs (real-time)
sudo journalctl -u spring-boot-optimization -f

# View logs since today
sudo journalctl -u spring-boot-optimization --since today
```

### Enable/Disable Auto-start
```bash
# Enable auto-start on boot
sudo systemctl enable spring-boot-optimization

# Disable auto-start
sudo systemctl disable spring-boot-optimization
```

## Update Application

### Opsi A: Menggunakan Update Script (Recommended)

**1. Build new JAR:**
```bash
# Di development machine
make version  # Check current version
make build-prod
```

**2. Transfer to server:**
```bash
VERSION=$(make version | grep "Current version" | awk '{print $3}')
scp build/libs/spring-boot-optimization-${VERSION}.jar user@server:/tmp/
scp deployment/update-version.sh user@server:/tmp/
```

**3. Update on server:**
```bash
# Di server
sudo chmod +x /tmp/update-version.sh
VERSION=$(ls /tmp/spring-boot-optimization-*.jar | sed 's|.*spring-boot-optimization-||' | sed 's|\.jar||')
sudo /tmp/update-version.sh $VERSION /tmp/spring-boot-optimization-${VERSION}.jar
```

### Opsi B: Manual Update

**1. Build new JAR:**
```bash
make build-prod
VERSION=$(make version | grep "Current version" | awk '{print $3}')
```

**2. Transfer to server:**
```bash
scp build/libs/spring-boot-optimization-${VERSION}.jar user@server:/tmp/
```

**3. Update on server:**
```bash
# Stop service
sudo systemctl stop spring-boot-optimization

# Create versions directory
sudo mkdir -p /opt/spring-boot-optimization/versions

# Backup old version (if exists)
if [ -L /opt/spring-boot-optimization/app.jar ]; then
    OLD_JAR=$(readlink -f /opt/spring-boot-optimization/app.jar)
    sudo cp "$OLD_JAR" "${OLD_JAR}.backup"
fi

# Copy new JAR with version
sudo cp /tmp/spring-boot-optimization-${VERSION}.jar \
        /opt/spring-boot-optimization/versions/
sudo chown springboot:springboot \
        /opt/spring-boot-optimization/versions/spring-boot-optimization-${VERSION}.jar

# Update symlink
sudo ln -sf /opt/spring-boot-optimization/versions/spring-boot-optimization-${VERSION}.jar \
            /opt/spring-boot-optimization/app.jar

# Start service
sudo systemctl start spring-boot-optimization

# Check status
sudo systemctl status spring-boot-optimization
```

### Rollback ke Version Sebelumnya

```bash
# List available versions
ls -lh /opt/spring-boot-optimization/versions/

# Rollback
sudo systemctl stop spring-boot-optimization
sudo ln -sf /opt/spring-boot-optimization/versions/spring-boot-optimization-1.0.0.jar \
            /opt/spring-boot-optimization/app.jar
sudo systemctl start spring-boot-optimization
```

## Troubleshooting

### Service won't start

**Check logs:**
```bash
sudo journalctl -u spring-boot-optimization -n 50 --no-pager
```

**Common issues:**
1. **Java not found**: Check JAVA_HOME in service file
2. **JAR file not found**: Verify path in service file
3. **Permission denied**: Check file ownership
4. **Port already in use**: Check if port 8080 is available
5. **Database connection failed**: Verify MySQL is running and credentials are correct

### Out of Memory

**Increase heap size in service file:**
```ini
ExecStart=/usr/bin/java \
  -Xms256m \
  -Xmx512m \  # Increase from 384m to 512m
  ...
```

**Then reload:**
```bash
sudo systemctl daemon-reload
sudo systemctl restart spring-boot-optimization
```

### Service keeps restarting

**Check logs for errors:**
```bash
sudo journalctl -u spring-boot-optimization -n 100 --no-pager
```

**Check restart policy:**
```bash
# View service file
sudo systemctl cat spring-boot-optimization

# Temporarily disable auto-restart for debugging
sudo systemctl edit spring-boot-optimization
# Add:
# [Service]
# Restart=no
```

### Database Connection Issues

**Test MySQL connection:**
```bash
mysql -u prod_user -p -h localhost proddb
```

**Check MySQL status:**
```bash
sudo systemctl status mysql
```

**Verify credentials in service file:**
```bash
sudo systemctl cat spring-boot-optimization | grep -A 5 "DB_"
```

## Security Best Practices

1. **Use strong passwords** for database and JWT secret
2. **Limit file permissions:**
   ```bash
   sudo chmod 600 /etc/spring-boot-optimization/application-prod.env
   ```
3. **Use firewall:**
   ```bash
   sudo ufw allow 8080/tcp
   sudo ufw enable
   ```
4. **Use reverse proxy** (Nginx/Apache) for HTTPS
5. **Regular updates:**
   ```bash
   sudo apt update && sudo apt upgrade
   ```

## Monitoring

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### System Info
```bash
curl http://localhost:8080/api/system-info
```

### Metrics
```bash
curl http://localhost:8080/actuator/metrics
```

## Reverse Proxy (Nginx) - Optional

**Install Nginx:**
```bash
sudo apt install nginx
```

**Create Nginx config:**
```bash
sudo nano /etc/nginx/sites-available/spring-boot-optimization
```

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

**Enable site:**
```bash
sudo ln -s /etc/nginx/sites-available/spring-boot-optimization /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

## Backup

**Backup JAR file:**
```bash
sudo cp /opt/spring-boot-optimization/spring-boot-optimization-0.0.1-SNAPSHOT.jar \
        /opt/spring-boot-optimization/backups/spring-boot-optimization-$(date +%Y%m%d-%H%M%S).jar
```

**Backup database:**
```bash
mysqldump -u prod_user -p proddb > /opt/spring-boot-optimization/backups/db-$(date +%Y%m%d).sql
```

## Performance Tuning

**For 1GB RAM server:**
- Current settings are already optimized
- Monitor memory usage: `curl http://localhost:8080/api/system-info`
- Adjust heap if needed (but keep total < 600MB)

**For 2GB+ RAM server:**
- Can increase heap: `-Xmx512m` or `-Xmx768m`
- Can increase metaspace: `-XX:MaxMetaspaceSize=256m`

