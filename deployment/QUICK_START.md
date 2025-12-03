# Quick Start - Systemd Deployment

Panduan cepat untuk deploy Spring Boot optimization dengan systemd di Ubuntu.

## Prerequisites Checklist

- [ ] Ubuntu 20.04+ atau 22.04 LTS
- [ ] Java 21+ terinstall
- [ ] MySQL 8.0+ terinstall dan running
- [ ] JAR file sudah di-build (`make build-prod`)

## Quick Setup (5 Menit)

### 1. Install Java 21
```bash
sudo apt update
sudo apt install -y openjdk-21-jdk
java -version  # Verify
```

### 2. Setup Database
```bash
sudo mysql -u root -p << EOF
CREATE DATABASE proddb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'prod_user'@'localhost' IDENTIFIED BY 'secure_password_here';
GRANT ALL PRIVILEGES ON proddb.* TO 'prod_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
EOF
```

### 3. Transfer Files ke Server
```bash
# Di development machine
scp build/libs/spring-boot-optimization-0.0.1-SNAPSHOT.jar user@server:/tmp/
scp -r deployment/ user@server:/tmp/
```

### 4. Run Setup Script
```bash
# Di server
cd /tmp/deployment
sudo chmod +x setup-systemd.sh
sudo ./setup-systemd.sh
```

### 5. Configure & Start
```bash
# Edit environment variables (IMPORTANT!)
sudo nano /etc/systemd/system/spring-boot-optimization.service
# Update: DB_PASSWORD, JWT_SECRET

# Copy JAR (jika belum)
sudo cp /tmp/spring-boot-optimization-0.0.1-SNAPSHOT.jar /opt/spring-boot-optimization/
sudo chown springboot:springboot /opt/spring-boot-optimization/*.jar

# Start service
sudo systemctl daemon-reload
sudo systemctl start spring-boot-optimization
sudo systemctl enable spring-boot-optimization

# Check status
sudo systemctl status spring-boot-optimization
```

### 6. Verify
```bash
# Check health
curl http://localhost:8080/actuator/health

# View logs
sudo journalctl -u spring-boot-optimization -f
```

## Common Commands

```bash
# Service management
sudo systemctl start spring-boot-optimization
sudo systemctl stop spring-boot-optimization
sudo systemctl restart spring-boot-optimization
sudo systemctl status spring-boot-optimization

# Logs
sudo journalctl -u spring-boot-optimization -f          # Follow logs
sudo journalctl -u spring-boot-optimization -n 100     # Last 100 lines
sudo journalctl -u spring-boot-optimization --since today # Since today

# Enable/disable auto-start
sudo systemctl enable spring-boot-optimization
sudo systemctl disable spring-boot-optimization
```

## Important Notes

1. **JWT Secret**: Pastikan menggunakan secret key yang aman (minimal 256 bits)
2. **Database Password**: Gunakan password yang kuat
3. **Port**: Default port 8080, bisa diubah di service file
4. **Memory**: Optimized untuk 1GB RAM server

## Troubleshooting

**Service tidak start:**
```bash
sudo journalctl -u spring-boot-optimization -n 50 --no-pager
```

**Port sudah digunakan:**
```bash
sudo netstat -tlnp | grep 8080
```

**Database connection error:**
```bash
mysql -u prod_user -p -h localhost proddb
```

Lihat [DEPLOYMENT.md](./DEPLOYMENT.md) untuk panduan lengkap dan troubleshooting detail.

