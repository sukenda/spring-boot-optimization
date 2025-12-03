# Code Style Guide

## Java Code Style

### Naming Conventions

#### Packages

- Lowercase, singular
- Example: `com.khas.optimization.service`

#### Classes

- PascalCase
- Descriptive names
- Examples:
    - `UserController`
    - `UserService`
    - `UserRepository`

#### Methods

- camelCase
- Verb-based names
- Examples:
    - `createUser()`
    - `getUserById()`
    - `isDeleted()`

#### Variables

- camelCase
- Descriptive names
- Examples:
    - `userService`
    - `userRepository`
    - `isEnabled`

#### Constants

- UPPER_SNAKE_CASE
- Example: `MAX_RETRY_COUNT`

### Code Organization

#### Package Structure

```
com.khas.optimization/
├── config/          # Configuration classes
├── controller/      # REST controllers
├── dto/             # Data Transfer Objects
├── entity/          # Entity classes
├── filter/          # Web filters
├── repository/      # R2DBC repositories
└── service/         # Business logic
```

#### Class Organization

1. Fields
2. Constructors
3. Methods (public → private)
4. Inner classes

### Annotations

#### Entity Classes

```java

@Getter
@Setter
@Table("table_name")
public class Entity extends BaseEntity {
    @Column("field_name")
    private String fieldName;
}
```

#### DTOs

```java

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dto {
    @NotBlank
    private String field;
}
```

#### Controllers

```java

@RestController
@RequestMapping("/api/resource")
public class ResourceController {
    // Controller methods
}
```

### Reactive Code Style

#### Always Use Reactive Types

```java
// Good
public Mono<UserResponse> getUser(Long id){
        return repository.findById(id)
        .map(this::toResponse);
        }

// Bad
public UserResponse getUser(Long id){
        return toResponse(repository.findById(id).block());
        }
```

#### Error Handling

```java
return service.method()
        .map(ResponseEntity::ok)
        .onErrorResume(error->{
        // Handle error
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
        });
```

### Documentation

#### JavaDoc for Public APIs

```java
/**
 * Creates a new user.
 *
 * @param request User creation request
 * @return Mono containing created user response
 * @throws RuntimeException if username or email already exists
 */
public Mono<UserResponse> createUser(UserRequest request){
        // Implementation
        }
```

#### Inline Comments

- Explain "why", not "what"
- Use clear, concise comments
- Update comments when code changes

### Best Practices

1. ✅ Use Lombok to reduce boilerplate
2. ✅ Keep methods focused and small
3. ✅ Use meaningful variable names
4. ✅ Handle errors reactively
5. ✅ Write unit tests
6. ✅ Follow SOLID principles
7. ✅ Use dependency injection
8. ✅ Avoid null returns (use Optional/Mono.empty())
9. ✅ Validate inputs
10. ✅ Log important events

### Formatting

#### Indentation

- 4 spaces (not tabs)
- Consistent indentation

#### Line Length

- Maximum 120 characters
- Break long lines appropriately

#### Imports

- Organize imports
- Remove unused imports
- Use wildcard imports sparingly

### Testing Style

#### Test Naming

```java
@Test
@DisplayName("Should create user successfully")
void testCreateUserSuccess(){}
```

#### Test Structure

```java
@Test
void testMethod(){
        // Given (Arrange)
        when(repository.method()).thenReturn(value);

        // When (Act)
        Mono<Result> result=service.method();

        // Then (Assert)
        StepVerifier.create(result)
        .assertNext(r->assertEquals(expected,r))
        .verifyComplete();
        }
```

## Resources

- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [Spring Framework Code Style](https://github.com/spring-projects/spring-framework/wiki/Code-Style)

