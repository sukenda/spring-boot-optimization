# Architecture Guide

## System Architecture

Spring Boot optimization follows a **reactive, layered architecture** optimized for low-resource environments.

## Architecture Layers

```
┌─────────────────────────────────────────┐
│         Controller Layer                 │
│    (REST API - WebFlux Reactive)        │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│         Service Layer                   │
│    (Business Logic - Reactive)           │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│         Repository Layer                │
│    (R2DBC - Reactive Database)          │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│         Database Layer                  │
│    (MySQL - R2DBC)                      │
└─────────────────────────────────────────┘
```

## Technology Stack

### Core Framework

- **Spring Boot 3.4.0** - Application framework
- **Spring WebFlux** - Reactive web framework
- **Netty** - Non-blocking I/O server

### Database

- **R2DBC** - Reactive database connectivity
- **MySQL** - Relational database (production)
- **H2** - In-memory database (testing)

### Security

- **JWT (jjwt)** - JSON Web Token authentication
- **BCrypt** - Password hashing

### Build & Tools

- **Gradle** - Build automation
- **GraalVM Native Image** - Native compilation
- **Lombok** - Code generation
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework
- **Reactor Test** - Reactive testing

## Design Patterns

### 1. Reactive Programming Pattern

All operations are non-blocking and return `Mono` or `Flux`:

```java
public Mono<UserResponse> createUser(UserRequest request){
        return userRepository.existsByUsername(request.getUsername())
        .flatMap(usernameExists->{
        // Non-blocking operations
        });
        }
```

### 2. Repository Pattern

Data access is abstracted through repositories:

```java
public interface UserRepository extends ReactiveCrudRepository<User, Long> {
    Mono<User> findByUsername(String username);
}
```

### 3. DTO Pattern

Data Transfer Objects separate API contracts from entities:

```java
// Entity (internal)
public class User extends BaseEntity { ...
}

// DTO (API)
public class UserResponse { ...
}
```

### 4. Service Layer Pattern

Business logic is encapsulated in service classes:

```java

@Service
public class UserService {
    // Business logic here
}
```

### 5. Filter Pattern

Cross-cutting concerns handled by filters:

```java

@Component
public class JwtAuthenticationFilter implements WebFilter {
    // Authentication logic
}
```

## Entity Design

### BaseEntity Pattern

All entities extend `BaseEntity` or `BaseUuidEntity`:

```java

@Table("users")
public class User extends BaseEntity {
    // Entity-specific fields
}
```

**Benefits:**

- Consistent structure across entities
- Automatic timestamp management
- Soft delete support
- Reduced code duplication

## Security Architecture

### JWT Authentication Flow

```
1. Client → POST /api/auth/login (username, password)
2. Server → Validates credentials
3. Server → Generates JWT token
4. Client → Stores token
5. Client → Includes token in Authorization header
6. Server → Validates token on each request
```

### Filter Chain

```
Request → JwtAuthenticationFilter → Controller
           ↓
    Validates JWT Token
           ↓
    Adds user info to request
```

## Database Design

### Soft Delete Pattern

All entities support soft delete:

- `deletedAt` field marks deleted records
- Repository queries automatically filter deleted records
- Allows data recovery and audit trails

### Migration Strategy

Versioned migrations in `db/migration/`:

- Sequential versioning (V1, V2, V3...)
- Automatic execution in development
- Manual execution in production

## Reactive Flow

### Request Processing

```
HTTP Request
    ↓
Netty (Non-blocking I/O)
    ↓
WebFilter (JWT validation)
    ↓
Controller (Reactive)
    ↓
Service (Mono/Flux)
    ↓
Repository (R2DBC)
    ↓
Database (MySQL)
    ↓
Response (Reactive chain)
```

## Performance Optimizations

### Memory Optimization

- Lazy initialization
- Minimal dependencies
- Serial GC for low memory
- Virtual threads (Java 21)

### Boot Time Optimization

- GraalVM Native Image
- Layered JAR
- Optimized class loading

### Database Optimization

- Connection pooling
- Reactive queries
- Indexed columns
- Soft delete filtering

## Scalability

### Horizontal Scaling

- Stateless design (JWT tokens)
- Reactive non-blocking I/O
- Database connection pooling
- No session state

### Vertical Scaling

- Low memory footprint
- Efficient resource usage
- Optimized for 1GB RAM servers

## Error Handling

### Reactive Error Handling

```java
return userService.createUser(request)
        .map(ResponseEntity.status(HttpStatus.CREATED)::body)
        .onErrorResume(error->{
        // Handle error reactively
        });
```

### Error Response Format

Standardized error responses with appropriate HTTP status codes.

## Testing Strategy

### Unit Tests

- Service layer tests with Mockito
- Reactive testing with StepVerifier
- Comprehensive coverage

### Integration Tests

- Controller tests with WebTestClient
- Database tests with test containers (optional)

## Deployment Architecture

### Development

- H2 in-memory database
- Debug logging
- Full actuator endpoints

### Production

- MySQL database
- Minimal logging
- Security-hardened
- Native image (optional)

## Monitoring

### Actuator Endpoints

- `/actuator/health` - Health checks
- `/actuator/metrics` - Application metrics
- `/actuator/info` - Application information

### Custom Monitoring

- `/api/system-info` - System resource usage
- Custom metrics via Actuator

## Best Practices

1. ✅ Always use reactive types (Mono/Flux)
2. ✅ Keep services stateless
3. ✅ Use DTOs for API contracts
4. ✅ Implement soft delete for data recovery
5. ✅ Use Lombok to reduce boilerplate
6. ✅ Write comprehensive unit tests
7. ✅ Follow naming conventions
8. ✅ Document complex logic
9. ✅ Use environment variables for configuration
10. ✅ Implement proper error handling

