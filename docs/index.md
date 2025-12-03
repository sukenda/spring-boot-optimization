# Spring Boot optimization

🚀 **Optimized Spring Boot starter project for low-resource servers (1GB RAM, 1 CPU Core)**

## Overview

Spring Boot optimization is a production-ready Spring Boot application template optimized for low-resource environments.
It uses reactive programming with Spring WebFlux, R2DBC for database access, and supports GraalVM Native Image for
ultra-fast boot times.

## Key Features

- ✅ **Spring Boot 3.4.0** with Java 21
- ✅ **Spring WebFlux** - Fully reactive, non-blocking
- ✅ **R2DBC with MySQL** - Reactive database access
- ✅ **JWT Authentication** - Standard JSON Web Token
- ✅ **Soft Delete** - All entities support soft delete
- ✅ **Lombok** - Reduced boilerplate code
- ✅ **GraalVM Native Image** - Ultra-fast boot (< 100ms)
- ✅ **Comprehensive Tests** - 41+ unit tests
- ✅ **Gradle Build** - Modern build system
- ✅ **Makefile** - Easy build and run commands

## Quick Start

```bash
# Clone repository
git clone <repository-url>
cd spring-boot-optimization

# Build
make build

# Run development
make run-dev

# Run tests
make test
```

## Documentation

- [Getting Started](./GETTING_STARTED.md) - Quick start guide
- [API Documentation](./API.md) - Complete API reference
- [Architecture Guide](./ARCHITECTURE.md) - System architecture
- [Development Guide](./DEVELOPMENT.md) - Development setup
- [Testing Guide](./TESTING.md) - Testing strategies
- [Deployment Guide](./DEPLOYMENT.md) - Production deployment

## Project Structure

```
spring-boot-optimization/
├── src/
│   ├── main/java/com/khas/springbootoptimization/
│   │   ├── config/          # Configuration
│   │   ├── controller/      # REST controllers
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── entity/          # Entity classes
│   │   ├── filter/          # Web filters
│   │   ├── repository/      # R2DBC repositories
│   │   └── service/         # Business logic
│   └── resources/
│       ├── db/migration/    # Database migrations
│       └── application*.yml # Configuration
├── docs/                    # Documentation
├── build.gradle             # Build configuration
└── Makefile                 # Build commands
```

## Technology Stack

- **Framework**: Spring Boot 3.4.0
- **Web**: Spring WebFlux (Reactive)
- **Database**: R2DBC + MySQL
- **Security**: JWT (jjwt)
- **Build**: Gradle
- **Native**: GraalVM Native Image
- **Testing**: JUnit 5 + Mockito + Reactor Test

## Performance

- **Boot Time**:
    - JAR: ~2-5 seconds
    - Native: ~50-200ms
- **Memory Usage**:
    - JAR: ~350-450MB
    - Native: ~80-150MB

## License

MIT License

## Contributing

Contributions are welcome! Please read our contributing guidelines and submit pull requests.

