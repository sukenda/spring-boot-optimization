# Development Guide

## Development Setup

### Prerequisites

1. **Java 21+**
   ```bash
   java -version
   # Should show Java 21 or higher
   ```

2. **Gradle 8.0+** (or use Gradle Wrapper)
   ```bash
   ./gradlew --version
   ```

3. **MySQL 8.0+** (for database)
   ```bash
   mysql --version
   ```

4. **Make** (optional, for Makefile commands)
   ```bash
   make --version
   ```

### IDE Setup

#### IntelliJ IDEA

1. Open project
2. Import Gradle project
3. Enable Lombok plugin
4. Configure annotation processing
5. Set Java 21 as SDK

#### Eclipse

1. Import as Gradle project
2. Install Lombok plugin
3. Configure annotation processing

#### VS Code

1. Install Java Extension Pack
2. Install Lombok extension
3. Configure Java 21

## Project Structure

```
src/
├── main/
│   ├── java/com/khas/springbootoptimization/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── entity/          # Entity classes
│   │   ├── filter/          # Web filters
│   │   ├── repository/      # R2DBC repositories
│   │   └── service/         # Business logic
│   └── resources/
│       ├── db/migration/    # Database migrations
│       └── application*.yml # Configuration
└── test/
    └── java/com/khas/springbootoptimization/
        ├── controller/      # Controller tests
        └── service/         # Service tests
```

## Development Workflow

### 1. Clone and Setup

```bash
git clone <repository-url>
cd spring-boot-optimization
```

### 2. Configure Database

```bash
# Create development database
mysql -u root -p
CREATE DATABASE devdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# Set environment variables
export DB_URL=r2dbc:mysql://localhost:3306/devdb?useSSL=false
export DB_USERNAME=root
export DB_PASSWORD=yourpassword
```

### 3. Run Migrations

Migrations run automatically on startup in development mode.

### 4. Start Application

```bash
# Using Makefile
make run-dev

# Or using Gradle
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### 5. Verify Setup

```bash
# Check health
curl http://localhost:8080/actuator/health

# Test login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test"}'
```

## Coding Standards

### Package Naming

- Base package: `com.khas.optimization`
- Sub-packages: lowercase, singular (e.g., `controller`, `service`)

### Class Naming

- Controllers: `*Controller` (e.g., `UserController`)
- Services: `*Service` (e.g., `UserService`)
- Repositories: `*Repository` (e.g., `UserRepository`)
- Entities: Singular noun (e.g., `User`, `Role`)
- DTOs: `*Request`, `*Response` (e.g., `UserRequest`, `UserResponse`)

### Method Naming

- Use descriptive names
- Follow Java conventions
- Use verbs for methods (e.g., `createUser`, `getUserById`)

### Code Style

- Use Lombok for getters/setters
- Keep methods focused and small
- Use reactive types (Mono/Flux)
- Handle errors reactively

## Adding New Features

### 1. Create Entity

```java

@Getter
@Setter
@Table("your_table")
public class YourEntity extends BaseEntity {
    @Column("field_name")
    private String fieldName;
}
```

### 2. Create Repository

```java
public interface YourRepository extends ReactiveCrudRepository<YourEntity, Long> {
    Mono<YourEntity> findByFieldName(String fieldName);
}
```

### 3. Create DTOs

```java

@Data
@NoArgsConstructor
@AllArgsConstructor
public class YourRequest {
    @NotBlank
    private String fieldName;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
public class YourResponse {
    private Long id;
    private String fieldName;
}
```

### 4. Create Service

```java

@Service
public class YourService {
    private final YourRepository repository;

    public Mono<YourResponse> create(YourRequest request) {
        // Business logic
    }
}
```

### 5. Create Controller

```java

@RestController
@RequestMapping("/api/your-resource")
public class YourController {
    private final YourService service;

    @PostMapping
    public Mono<ResponseEntity<YourResponse>> create(@Valid @RequestBody YourRequest request) {
        // Controller logic
    }
}
```

### 6. Create Migration

Create file: `src/main/resources/db/migration/V{N}__create_your_table.sql`

### 7. Write Tests

```java

@ExtendWith(MockitoExtension.class)
class YourServiceTest {
    @Mock
    private YourRepository repository;

    @InjectMocks
    private YourService service;

    @Test
    void testCreate() {
        // Test implementation
    }
}
```

## Database Development

### Running Migrations

Migrations run automatically in development. To run manually:

```bash
mysql -u root -p devdb < src/main/resources/db/migration/V1__initial_schema.sql
```

### Creating Migrations

1. Create new file: `V{N}__description.sql`
2. Use `IF NOT EXISTS` for safety
3. Add indexes for performance
4. Test migration on dev database first

### Migration Best Practices

- Always use transactions
- Test rollback procedures
- Document breaking changes
- Version sequentially

## Testing

### Running Tests

```bash
# All tests
make test

# Specific test class
./gradlew test --tests UserServiceTest

# With coverage
./gradlew test jacocoTestReport
```

### Writing Tests

1. **Unit Tests** - Test services in isolation
2. **Integration Tests** - Test controllers with WebTestClient
3. **Use Mockito** - Mock dependencies
4. **Use StepVerifier** - Test reactive code

### Test Structure

```java

@ExtendWith(MockitoExtension.class)
class ServiceTest {
    @Mock
    private Repository repository;

    @InjectMocks
    private Service service;

    @BeforeEach
    void setUp() {
        // Setup test data
    }

    @Test
    @DisplayName("Should do something")
    void testSomething() {
        // Given
        // When
        // Then
    }
}
```

## Debugging

### Enable Debug Logging

In `application-dev.yml`:

```yaml
logging:
  level:
    com.khas: DEBUG
```

### Actuator Endpoints

Access debugging endpoints:

- `/actuator/env` - Environment variables
- `/actuator/configprops` - Configuration properties
- `/actuator/beans` - Spring beans

### IDE Debugging

1. Set breakpoints
2. Run in debug mode
3. Use reactive debugging tools
4. Monitor reactive streams

## Common Tasks

### Adding a New Endpoint

1. Add method to controller
2. Add service method if needed
3. Update API documentation
4. Write tests
5. Update public paths in filter if needed

### Adding a New Entity

1. Create entity class extending BaseEntity
2. Create repository interface
3. Create migration file
4. Create DTOs
5. Create service
6. Create controller
7. Write tests

### Modifying Database Schema

1. Create new migration file
2. Test on development database
3. Update entity classes if needed
4. Update repository queries if needed
5. Test migration rollback

## Git Workflow

### Branch Strategy

- `main` - Production-ready code
- `develop` - Development branch
- `feature/*` - Feature branches
- `fix/*` - Bug fix branches

### Commit Messages

Follow conventional commits:

```
feat: add user soft delete functionality
fix: resolve JWT validation issue
docs: update API documentation
test: add unit tests for UserService
```

## Troubleshooting

### Application Won't Start

1. Check Java version (must be 21+)
2. Check database connection
3. Check port availability (8080)
4. Review logs for errors

### Database Connection Issues

1. Verify MySQL is running
2. Check connection string
3. Verify credentials
4. Check network connectivity

### Build Failures

1. Clean build: `./gradlew clean build`
2. Check dependency versions
3. Verify Gradle version
4. Check for compilation errors

## Resources

- [Spring WebFlux Documentation](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [R2DBC Documentation](https://r2dbc.io/)
- [Projection Reactor Documentation](https://projectreactor.io/docs/core/release/reference/)
- [JWT Best Practices](https://jwt.io/introduction)

