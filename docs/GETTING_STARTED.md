# Getting Started

Quick start guide for Spring Boot optimization.

## Prerequisites

- Java 21+
- MySQL 8.0+ (or use H2 for quick testing)
- Gradle 8.0+ (or use Gradle Wrapper)

## Quick Start (5 Minutes)

### 1. Clone Repository

```bash
git clone <repository-url>
cd spring-boot-optimization
```

### 2. Setup Database (Optional for Quick Test)

For quick testing, you can skip MySQL setup and use H2 in-memory database.

For MySQL:

```bash
mysql -u root -p
CREATE DATABASE devdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Configure (Optional)

Set environment variables if using MySQL:

```bash
export DB_URL=r2dbc:mysql://localhost:3306/devdb?useSSL=false
export DB_USERNAME=root
export DB_PASSWORD=yourpassword
```

### 4. Build

```bash
make build
# or
./gradlew clean build
```

### 5. Run

```bash
make run-dev
# or
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### 6. Test

```bash
# Health check
curl http://localhost:8080/actuator/health

# Create user (requires JWT token first)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test"}'
```

## Next Steps

1. **Read [API Documentation](./API.md)** - Learn about available endpoints
2. **Read [Development Guide](./DEVELOPMENT.md)** - Set up your development environment
3. **Read [Architecture Guide](./ARCHITECTURE.md)** - Understand the system design
4. **Read [Configuration Guide](../CONFIG.md)** - Configure for your needs

## Common Commands

```bash
# Build
make build

# Run development
make run-dev

# Run production
make run-prod

# Run tests
make test

# Build native image
make build-native

# Show help
make help
```

## Troubleshooting

### Port Already in Use

```bash
# Change port
export SERVER_PORT=8081
make run-dev
```

### Database Connection Failed

1. Check MySQL is running: `sudo systemctl status mysql`
2. Verify credentials
3. Check connection string

### Build Fails

```bash
# Clean and rebuild
make clean
make build
```

## Need Help?

- Check [Troubleshooting Guide](./TROUBLESHOOTING.md)
- Review [Main README](../README.md)
- Check [Configuration Guide](../CONFIG.md)

