# Spring Boot optimization

🚀 **Optimized Spring Boot starter project for low-resource servers (1GB RAM, 1 CPU Core)**

## Features

- ✅ **Spring Boot 3.4.0** (Latest version)
- ✅ **Java 21** with Virtual Threads support
- ✅ **Spring WebFlux** (Reactive Web) for better performance
- ✅ **GraalVM Native Image** support for ultra-fast boot time
- ✅ **Gradle** build system
- ✅ **JWT Authentication** (Standard JSON Web Token with jjwt)
- ✅ **R2DBC** (Reactive Database) with MySQL support
- ✅ **Soft Delete** functionality for all entities
- ✅ **Lombok** for cleaner code (reduced boilerplate)
- ✅ **BaseEntity & BaseUuidEntity** for common entity fields
- ✅ **Optimized JVM settings** for low memory
- ✅ **Lazy initialization** for faster startup
- ✅ **Health monitoring** with Actuator
- ✅ **Comprehensive Unit Tests** for all services

## Optimizations Applied

### JVM Optimizations
| Setting | Value | Description |
|---------|-------|-------------|
| Heap Size | 256MB - 384MB | Optimal for 1GB RAM server |
| Metaspace | 64MB - 128MB | Limit class metadata memory |
| Thread Stack | 512KB | Reduced from default 1MB |
| GC | SerialGC | Best for single-core CPU |

### Spring Boot Optimizations
- **Lazy Initialization**: Beans are created on-demand
- **Virtual Threads**: Better CPU utilization (Java 21)
- **Spring WebFlux**: Reactive programming model for better scalability
- **Netty Server**: Default for WebFlux, optimized for reactive workloads
- **Disabled JMX**: Saves ~10-20MB memory
- **Compression**: Reduced network bandwidth
- **GraalVM Native Image**: Ultra-fast boot time (< 100ms)

## Quick Start

### Deployment

**Untuk deploy ke Ubuntu server dengan systemd:**
- Lihat [deployment/DEPLOYMENT.md](./deployment/DEPLOYMENT.md) untuk panduan lengkap
- Quick setup: `sudo ./deployment/setup-systemd.sh`

### Prerequisites
- **Java 21+** (or GraalVM 21+ for native image)
  - **Using SDKMAN (Recommended)**: See [JAVA_VERSION.md](./JAVA_VERSION.md) for managing multiple Java versions
  - Quick setup: `sdk install java 21.0.1-tem && sdk use java 21.0.1-tem`
- Gradle 8.0+ (or use Gradle Wrapper - included)
- Make (optional, for using Makefile)

### Using Makefile (Recommended)

The easiest way to build and run the application:

**Show all available commands:**
```bash
make help
```

**Build JAR:**
```bash
# Make sure Java 21 is active (if using SDKMAN)
sdk env  # or: source use-java21.sh

# Check version
make version        # Show current version

# Build
make build          # Development build
make build-prod      # Production build (optimized)
# JAR: build/libs/spring-boot-optimization-VERSION.jar
```

**Version Management:**
```bash
make version        # Show current version
make version-bump   # Bump patch version (0.0.1 -> 0.0.2-SNAPSHOT)
make version-minor  # Bump minor version (0.0.1 -> 0.1.0-SNAPSHOT)
make version-major  # Bump major version (0.0.1 -> 1.0.0-SNAPSHOT)
make version-release # Release version (remove SNAPSHOT)
```
Lihat [VERSIONING.md](./VERSIONING.md) untuk panduan lengkap.

**Build Native Image (GraalVM):**
```bash
make build-native    # Build native executable (takes several minutes)
```

**Run Application:**
```bash
make run-dev         # Run in development mode
make run-prod        # Run in production mode (JAR)
make run-native      # Run native image (fastest boot)
make run             # Auto-detect: uses native if available, otherwise JAR
```

**Other Commands:**
```bash
make test            # Run tests
make clean           # Clean build artifacts
make info            # Show project information
```

### Using Gradle Directly

**Build JAR:**
```bash
./gradlew clean build
```

**Build Native Image (GraalVM):**
```bash
./gradlew nativeCompile
```

This will create a native executable at `build/native/nativeCompile/spring-boot-optimization`

**Run (Development):**
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

**Run (Production):**
```bash
# Using JAR
java -Xms256m -Xmx384m -XX:+UseSerialGC -jar build/libs/spring-boot-optimization-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod

# Or using scripts
chmod +x run.sh
./run.sh
```

The script will automatically use native image if available, otherwise use JAR.

## Configuration

The project uses profile-based configuration:
- `application.yml` - Base/default configuration
- `application-dev.yml` - Development profile (activated with `--spring.profiles.active=dev`)
- `application-prod.yml` - Production profile (activated with `--spring.profiles.active=prod`)

### Development Configuration (`application-dev.yml`)

**Features:**
- Lazy initialization: **Disabled** (for easier debugging)
- Logging: **DEBUG** level (verbose)
- Database: H2 in-memory
- Actuator: More endpoints exposed for debugging
- JMX: Enabled for monitoring

**Usage:**
```bash
make run-dev
# or
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Production Configuration (`application-prod.yml`)

**Features:**
- Lazy initialization: **Enabled** (reduces startup memory)
- Logging: **WARN/ERROR** level (minimal)
- Database: Configurable (PostgreSQL/MySQL/H2 file-based)
- Actuator: Minimal endpoints (health, info, metrics)
- JMX: Disabled (saves memory)
- Security: Enhanced for production

**Usage:**
```bash
make run-prod
# or
java -jar app.jar --spring.profiles.active=prod
```

### JWT Configuration

This project uses **standard JSON Web Token (JWT)** with jjwt library.

**Development:**
- Default secret key (change in production)
- Token expiration: 24 hours
- Simple symmetric key (HS256)

**Production:**
- Configure via environment variables:
  ```bash
  export JWT_SECRET=your-secure-secret-key-minimum-256-bits
  export JWT_EXPIRATION=86400000  # 24 hours in milliseconds
  export JWT_ISSUER=your-app-name
  export JWT_AUDIENCE=your-app-users
  ```

**JWT Endpoints:**
- `POST /api/auth/login` - Generate JWT token
  ```json
  {
    "username": "user123",
    "password": "password123"
  }
  ```
- `GET /api/auth/validate` - Validate token (requires Bearer token in header)

**Using JWT Token:**
```bash
# Get token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user123","password":"password123"}'

# Use token in protected endpoints
curl -X GET http://localhost:8080/api/protected \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

**Public Endpoints (no JWT required):**
- `/api/auth/login`
- `/api/auth/validate`
- `/actuator/health`
- `/actuator/info`

**Protected Endpoints (JWT required):**
- `/api/users` - User CRUD operations
- `/api/users/{id}` - Get/Update/Delete specific user
- `/api/protected` - Protected endpoint example
- `/api/system-info` - System information

### Database Configuration

**R2DBC (Reactive Database) - Fully Supports WebFlux! ✅**

This project uses **R2DBC** (Reactive Relational Database Connectivity) which is fully reactive and non-blocking, perfect for WebFlux. All database operations are asynchronous and non-blocking.

**Development:**
- **MySQL** (default, recommended)
- Or H2 in-memory (for quick testing)

**Production:**
- **MySQL** (configured by default)
- Configure via environment variables:
  ```bash
  export DB_URL=r2dbc:mysql://localhost:3306/proddb?useSSL=true
  export DB_USERNAME=prod_user
  export DB_PASSWORD=your_secure_password
  ```

**MySQL Setup:**

1. **Create Database:**
   ```sql
   CREATE DATABASE devdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE DATABASE proddb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. **Set Environment Variables:**
   ```bash
   # Development
   export DB_URL=r2dbc:mysql://localhost:3306/devdb?useSSL=false
   export DB_USERNAME=root
   export DB_PASSWORD=yourpassword
   
   # Production
   export DB_URL=r2dbc:mysql://localhost:3306/proddb?useSSL=true
   export DB_USERNAME=prod_user
   export DB_PASSWORD=secure_password
   ```

3. **MySQL URL Parameters:**
   - `useSSL=true` (production) / `useSSL=false` (development)
   - `serverTimezone=Asia/Jakarta` (set your timezone)
   - `allowPublicKeyRetrieval=true` (if needed for authentication)

**Why R2DBC?**
- ✅ **Fully Reactive**: Non-blocking I/O, perfect for WebFlux
- ✅ **Better Performance**: No thread blocking, handles high concurrency
- ✅ **Resource Efficient**: Uses fewer threads, less memory
- ✅ **Scalable**: Better for microservices and cloud deployments

**Supported Databases:**
- **MySQL**: ✅ Already configured (`io.asyncer:r2dbc-mysql`)
- **PostgreSQL**: Add `io.r2dbc:r2dbc-postgresql` to `build.gradle`
- **H2**: Already included (for testing)

### Environment Variables

Production configuration supports these environment variables:

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_URL` | Database R2DBC URL | `r2dbc:mysql://localhost:3306/devdb` |
| `DB_USERNAME` | Database username | `root` (dev) / `prod_user` (prod) |
| `DB_PASSWORD` | Database password | (empty) |
| `JWT_SECRET` | JWT secret key (min 256 bits) | (default in config) |
| `JWT_EXPIRATION` | JWT expiration in milliseconds | `86400000` (24h) |
| `JWT_ISSUER` | JWT issuer | `spring-boot-optimization` |
| `JWT_AUDIENCE` | JWT audience | `spring-boot-optimization-users` |
| `SERVER_PORT` | Server port | `8080` |
| `SPRING_PROFILES_ACTIVE` | Active profile | (none) |

**Example:**
```bash
# Database configuration
export DB_URL=r2dbc:mysql://localhost:3306/proddb?useSSL=true&serverTimezone=Asia/Jakarta
export DB_USERNAME=prod_user
export DB_PASSWORD=secure_password

# JWT configuration
export JWT_SECRET=your-very-secure-secret-key-minimum-256-bits-for-hmac-sha256
export JWT_EXPIRATION=86400000

# Server configuration
export SERVER_PORT=8080
export SPRING_PROFILES_ACTIVE=prod

make run-prod
```

## API Endpoints

### Authentication Endpoints

| Endpoint | Method | Description | Auth Required |
|----------|--------|-------------|---------------|
| `/api/auth/login` | POST | Generate JWT token | No |
| `/api/auth/validate` | GET | Validate JWT token | No (but needs token) |

### User Management Endpoints

| Endpoint | Method | Description | Auth Required |
|----------|--------|-------------|---------------|
| `/api/users` | POST | Create new user | Yes |
| `/api/users` | GET | Get all users | Yes |
| `/api/users/{id}` | GET | Get user by ID | Yes |
| `/api/users/{id}` | PUT | Update user | Yes |
| `/api/users/{id}` | DELETE | Soft delete user | Yes |

### System Endpoints

| Endpoint | Method | Description | Auth Required |
|----------|--------|-------------|---------------|
| `/api/system-info` | GET | System & memory information | No |
| `/api/protected` | GET | Protected endpoint example | Yes |

### Actuator Endpoints

| Endpoint | Method | Description | Auth Required |
|----------|--------|-------------|---------------|
| `/actuator/health` | GET | Health check | No |
| `/actuator/info` | GET | Application info | No |
| `/actuator/metrics` | GET | Application metrics | No |

### API Documentation (Swagger)

| Endpoint | Method | Description | Auth Required |
|----------|--------|-------------|---------------|
| `/swagger-ui.html` | GET | Swagger UI - Interactive API documentation | No |
| `/v3/api-docs` | GET | OpenAPI JSON specification | No |

**Access Swagger UI:**
```
http://localhost:8080/swagger-ui.html
```

**Features:**
- Interactive API testing
- JWT authentication support
- Request/Response examples
- Schema documentation
- Try it out functionality

### Example API Usage

**Create User:**
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "username": "newuser",
    "email": "newuser@example.com",
    "password": "password123"
  }'
```

**Get All Users:**
```bash
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Update User:**
```bash
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "username": "updateduser",
    "email": "updated@example.com",
    "password": "newpassword"
  }'
```

**Delete User (Soft Delete):**
```bash
curl -X DELETE http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Memory Usage Estimation

With these optimizations, expected memory usage:

| Component | JAR Mode | Native Image |
|-----------|----------|--------------|
| Heap | ~200-350MB | ~50-100MB |
| Metaspace | ~60-80MB | N/A |
| Native Memory | ~50-80MB | ~30-50MB |
| **Total** | **~350-450MB** | **~80-150MB** |

This leaves ~500-600MB for the OS and other processes on a 1GB RAM server.

## Boot Time Comparison

- **JAR Mode**: ~2-5 seconds
- **Native Image**: ~50-200ms (ultra-fast!)

## JVM Arguments Explained

```bash
# Production settings
java \
  -Xms256m \                    # Initial heap size
  -Xmx384m \                    # Maximum heap size
  -XX:MetaspaceSize=64m \       # Initial metaspace
  -XX:MaxMetaspaceSize=128m \   # Maximum metaspace
  -Xss512k \                    # Thread stack size
  -XX:+UseSerialGC \            # Serial garbage collector
  -XX:+DisableExplicitGC \      # Disable System.gc() calls
  -jar app.jar
```

## GraalVM Native Image

> **Note:** JAR biasa yang sudah dibuat **TIDAK memerlukan GraalVM** untuk dijalankan. Native Image adalah build terpisah yang opsional. Lihat [GRAALVM_EXPLANATION.md](./GRAALVM_EXPLANATION.md) untuk penjelasan lengkap.

Build native executable for ultra-fast boot time:

```bash
make build-native
# atau
./gradlew nativeCompile
```

Requirements:
- GraalVM 21+ (JDK with native-image tool)
- Native Build Tools (already configured in build.gradle)

The native image will be created at: `build/native/nativeCompile/spring-boot-optimization`

### Native Image Benefits
- **Ultra-fast boot**: < 100ms startup time
- **Lower memory**: ~50-100MB total memory
- **No JVM overhead**: Direct native execution
- **Smaller footprint**: Single executable file

### Troubleshooting Native Image Build
Jika build Native Image gagal di task `processAot`:
- **JAR biasa tidak terpengaruh** - masih bisa digunakan
- Untuk fix Native Image build, lihat [GRAALVM_EXPLANATION.md](./GRAALVM_EXPLANATION.md)

## Performance Tuning Tips

1. **Monitor Memory**: Use `/api/system-info` to check actual usage
2. **Adjust Heap**: If OOM occurs, increase `-Xmx` slightly
3. **Connection Pools**: Keep pool sizes small (2-5 connections)
4. **Use Native Image**: For production, prefer native image over JAR
5. **Async Operations**: Leverage WebFlux reactive programming

## Troubleshooting

### Out of Memory Error
```bash
# Increase heap slightly
java -Xmx448m -jar app.jar
```

### Slow Startup
- Use GraalVM Native Image for fastest boot
- Ensure lazy initialization is enabled
- Check for unnecessary dependencies

### High CPU Usage
- Use WebFlux reactive model
- Enable virtual threads
- Use async processing

### Native Image Build Issues
- Ensure GraalVM is properly installed
- Check that `native-image` tool is available
- Review reflection configuration in `src/main/resources/META-INF/native-image/reflect-config.json`

## Database Migrations

Database migrations are organized in `src/main/resources/db/migration/`:

```
db/migration/
├── README.md                    # Migration documentation
├── V1__initial_schema.sql       # Initial database schema
├── V2__insert_default_roles.sql # Default data
├── V3__create_audit_tables.sql  # Audit tables
├── V4__add_deleted_at_for_soft_delete.sql # Soft delete support
└── ...
```

**Naming Convention:** `V{version}__{description}.sql`

Migrations run automatically on startup (development) or can be executed manually (production).

See `db/migration/README.md` for detailed migration guide.

## Project Structure

```
spring-boot-optimization/
├── src/
│   ├── main/
│   │   ├── java/com/khas/springbootoptimization/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── dto/             # Data Transfer Objects (with Lombok)
│   │   │   ├── entity/           # Entity classes (BaseEntity, BaseUuidEntity)
│   │   │   ├── filter/           # Web filters (JWT authentication)
│   │   │   ├── repository/      # R2DBC repositories
│   │   │   └── service/         # Business logic services
│   │   └── resources/
│   │       ├── db/migration/    # Database migrations
│   │       └── application*.yml # Configuration files
│   └── test/
│       └── java/com/khas/springbootoptimization/
│           ├── controller/      # Controller tests
│           └── service/         # Service unit tests
├── docs/                        # Documentation (see below)
├── build.gradle                 # Gradle build configuration
├── Makefile                     # Build and run commands
└── README.md                    # This file
```

## Key Features

### Soft Delete
All entities support soft delete functionality:
- `deletedAt` field in BaseEntity and BaseUuidEntity
- `softDelete()` method to mark entity as deleted
- `restore()` method to restore deleted entity
- Repository queries automatically exclude soft-deleted records

### Lombok Integration
- All entities and DTOs use Lombok annotations
- `@Getter` and `@Setter` for automatic getter/setter generation
- `@Data` for DTOs (includes toString, equals, hashCode)
- Reduces boilerplate code significantly

### Base Entities
- **BaseEntity**: For auto-increment ID (Long) with timestamps and soft delete
- **BaseUuidEntity**: For UUID primary key with timestamps and soft delete
- Common fields: `id`, `createdAt`, `updatedAt`, `deletedAt`
- Helper methods: `prePersist()`, `preUpdate()`, `softDelete()`, `restore()`

## Testing

Comprehensive unit tests for all services:

```bash
# Run all tests
make test

# Run specific test class
./gradlew test --tests PasswordServiceTest
./gradlew test --tests JwtServiceTest
./gradlew test --tests UserServiceTest
```

**Test Coverage:**
- ✅ PasswordService: 10 test cases
- ✅ JwtService: 14 test cases
- ✅ UserService: 17 test cases
- **Total: 41 test cases**

## Documentation

📚 **Comprehensive documentation is available in the `docs/` folder:**

- [Getting Started](./docs/GETTING_STARTED.md) - Quick start guide
- [Installation Guide](./docs/INSTALLATION.md) - Detailed installation
- [API Documentation](./docs/API.md) - Complete API reference with examples
- [Architecture Guide](./docs/ARCHITECTURE.md) - System architecture and design
- [Development Guide](./docs/DEVELOPMENT.md) - Development setup and guidelines
- [Testing Guide](./docs/TESTING.md) - Testing strategies (41+ test cases)
- [Deployment Guide](./docs/DEPLOYMENT.md) - Production deployment instructions
- [Performance Guide](./docs/PERFORMANCE.md) - Performance optimization
- [Security Guide](./docs/SECURITY.md) - Security best practices
- [Monitoring Guide](./docs/MONITORING.md) - Application monitoring
- [Troubleshooting Guide](./docs/TROUBLESHOOTING.md) - Common issues and solutions
- [Code Style Guide](./docs/CODE_STYLE.md) - Coding standards

**Additional Documentation:**
- [Configuration Guide](./CONFIG.md) - Configuration reference
- [Database Guide](./DATABASE.md) - Database setup and migrations
- [Entity Documentation](./src/main/java/com/khas/optimization/entity/README.md) - BaseEntity guide

**📖 Full Documentation Index:** [docs/README.md](./docs/README.md)

**🌐 Online Documentation:** Documentation is automatically built and published to GitHub Pages on every push to main/develop branch.

## License

MIT License

## Contributing

Feel free to submit issues and pull requests!
