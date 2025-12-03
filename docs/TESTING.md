# Testing Guide

## Testing Strategy

This project uses a comprehensive testing strategy covering unit tests, integration tests, and reactive testing.

## Test Structure

```
src/test/java/com/khas/springbootoptimization/
├── controller/          # Controller tests
│   └── HealthControllerTests.java
├── service/            # Service unit tests
│   ├── PasswordServiceTest.java
│   ├── JwtServiceTest.java
│   └── UserServiceTest.java
└── SpringBootoptimizationApplicationTests.java
```

## Unit Testing

### Service Layer Tests

Service tests use **Mockito** for mocking dependencies and **StepVerifier** for testing reactive code.

#### Example: UserServiceTest

```java

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordService passwordService;

    @InjectMocks
    private UserService userService;

    @Test
    void testCreateUserSuccess() {
        // Given
        when(userRepository.existsByUsername(anyString()))
                .thenReturn(Mono.just(false));

        // When
        Mono<UserResponse> result = userService.createUser(request);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals("testuser", response.getUsername());
                })
                .verifyComplete();
    }
}
```

### Test Coverage

| Service         | Test Cases | Coverage |
|-----------------|------------|----------|
| PasswordService | 10         | 100%     |
| JwtService      | 14         | 100%     |
| UserService     | 17         | 100%     |
| **Total**       | **41**     | **100%** |

## Running Tests

### All Tests

```bash
# Using Makefile
make test

# Using Gradle
./gradlew test
```

### Specific Test Class

```bash
./gradlew test --tests PasswordServiceTest
./gradlew test --tests JwtServiceTest
./gradlew test --tests UserServiceTest
```

### With Coverage

```bash
./gradlew test jacocoTestReport
# Report available at: build/reports/jacoco/test/html/index.html
```

## Test Categories

### 1. Unit Tests

Test individual components in isolation:

- **Service Tests** - Business logic
- **Utility Tests** - Helper methods
- **DTO Tests** - Data validation

### 2. Integration Tests

Test component interactions:

- **Controller Tests** - API endpoints
- **Repository Tests** - Database operations (optional)
- **End-to-End Tests** - Full request flow

## Testing Reactive Code

### Using StepVerifier

```java
StepVerifier.create(serviceMethod())
        .assertNext(result->{
        // Assertions
        })
        .verifyComplete();
```

### Testing Errors

```java
StepVerifier.create(serviceMethod())
        .expectErrorMatches(throwable->
        throwable instanceof RuntimeException&&
        throwable.getMessage().equals("Expected error"))
        .verify();
```

### Testing Empty Results

```java
StepVerifier.create(serviceMethod())
        .verifyComplete(); // For Mono<Void>
```

## Mocking with Mockito

### Mocking Repositories

```java
@Mock
private UserRepository userRepository;

        when(userRepository.findById(1L))
        .thenReturn(Mono.just(testUser));
```

### Mocking Services

```java
@Mock
private PasswordService passwordService;

        when(passwordService.hashPassword("password"))
        .thenReturn("hashedPassword");
```

## Test Data Setup

### Using @BeforeEach

```java
@BeforeEach
void setUp(){
        testUser=new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        // ... setup test data
        }
```

## Best Practices

### 1. Test Naming

Use descriptive test names:

```java
@Test
@DisplayName("Should create user successfully")
void testCreateUserSuccess(){}
```

### 2. Arrange-Act-Assert Pattern

```java
@Test
void testMethod(){
        // Arrange (Given)
        when(repository.method()).thenReturn(value);

        // Act (When)
        Mono<Result> result=service.method();

        // Assert (Then)
        StepVerifier.create(result)
        .assertNext(r->assertEquals(expected,r))
        .verifyComplete();
        }
```

### 3. Test Isolation

Each test should be independent:

- Don't share state between tests
- Reset mocks in @BeforeEach
- Use fresh test data

### 4. Test Coverage

Aim for:

- 100% service layer coverage
- Critical path coverage
- Error scenario coverage
- Edge case coverage

### 5. Test Performance

Keep tests fast:

- Use mocks instead of real dependencies
- Avoid I/O operations
- Use in-memory database for integration tests

## Common Test Patterns

### Testing Success Cases

```java
@Test
void testSuccessCase(){
        when(dependency.method()).thenReturn(Mono.just(result));

        StepVerifier.create(service.method())
        .assertNext(r->{
        assertNotNull(r);
        // Assertions
        })
        .verifyComplete();
        }
```

### Testing Error Cases

```java
@Test
void testErrorCase(){
        when(dependency.method()).thenReturn(Mono.error(new RuntimeException("Error")));

        StepVerifier.create(service.method())
        .expectError(RuntimeException.class)
        .verify();
        }
```

### Testing Validation

```java
@Test
void testValidation(){
        InvalidRequest request=new InvalidRequest();

        StepVerifier.create(service.method(request))
        .expectError(ValidationException.class)
        .verify();
        }
```

## Integration Testing

### Controller Tests

```java

@WebFluxTest(UserController.class)
class UserControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private UserService userService;

    @Test
    void testCreateUser() {
        webTestClient.post()
                .uri("/api/users")
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();
    }
}
```

## Continuous Integration

Tests run automatically in CI/CD pipeline:

```yaml
- name: Run Tests
  run: ./gradlew test
```

## Test Maintenance

### Keeping Tests Updated

- Update tests when code changes
- Remove obsolete tests
- Refactor tests for clarity
- Add tests for new features

### Test Documentation

- Document complex test scenarios
- Explain test data setup
- Note any test-specific configurations

## Resources

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Reactor Test Documentation](https://projectreactor.io/docs/test/release/reference/)

