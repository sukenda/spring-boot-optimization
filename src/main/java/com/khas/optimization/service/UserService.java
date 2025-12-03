package com.khas.optimization.service;

import com.khas.optimization.constants.RoleConstants;
import com.khas.optimization.dto.PageRequest;
import com.khas.optimization.dto.UserRequest;
import com.khas.optimization.dto.UserResponse;
import com.khas.optimization.entity.Role;
import com.khas.optimization.entity.User;
import com.khas.optimization.exception.DuplicateEntityException;
import com.khas.optimization.exception.EntityNotFoundException;
import com.khas.optimization.repository.RoleRepository;
import com.khas.optimization.repository.UserRepository;
import com.khas.optimization.repository.UserRoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * User Service (Reactive)
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordService passwordService;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       UserRoleRepository userRoleRepository,
                       PasswordService passwordService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordService = passwordService;
    }

    /**
     * Create a new user
     */
    public Mono<UserResponse> createUser(UserRequest request) {
        // Validate password strength
        passwordService.validatePasswordStrength(request.getPassword());

        // Check if username already exists
        return userRepository.existsByUsername(request.getUsername())
                .flatMap(usernameExists -> {
                    if (usernameExists) {
                        return Mono.error(new DuplicateEntityException("Username already exists"));
                    }

                    // Check if email already exists
                    return userRepository.existsByEmail(request.getEmail());
                })
                .flatMap(emailExists -> {
                    if (emailExists) {
                        return Mono.error(new DuplicateEntityException("Email already exists"));
                    }

                    // Create new user
                    User user = new User();
                    user.setUsername(request.getUsername());
                    user.setEmail(request.getEmail());
                    user.setPasswordHash(passwordService.hashPassword(request.getPassword()));
                    user.setEnabled(true);
                    user.prePersist(); // Initialize timestamps

                    return userRepository.save(user)
                            .flatMap(savedUser -> {
                                // Assign roles to user
                                List<Long> roleIds = request.getRoleIds();

                                // If no roles specified, assign default RoleConstants.ROLE_USER
                                if (roleIds == null || roleIds.isEmpty()) {
                                    return roleRepository.findByName(RoleConstants.ROLE_USER)
                                            .flatMap(defaultRole -> {
                                                return userRoleRepository.insertUserRole(
                                                        savedUser.getId(),
                                                        defaultRole.getId()
                                                ).thenReturn(savedUser);
                                            })
                                            .switchIfEmpty(Mono.just(savedUser)); // If RoleConstants.ROLE_USER not found, continue without role
                                }

                                // Assign specified roles
                                return Flux.fromIterable(roleIds)
                                        .flatMap(roleId -> {
                                            return userRoleRepository.insertUserRole(
                                                    savedUser.getId(),
                                                    roleId
                                            );
                                        })
                                        .then(Mono.just(savedUser));
                            })
                            .flatMap(this::toUserResponseWithRoles);
                });
    }

    /**
     * Update an existing user
     */
    public Mono<UserResponse> updateUser(Long id, UserRequest request) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("User not found")))
                .flatMap(existingUser -> {
                    // Check if username is being changed and if it already exists
                    if (!existingUser.getUsername().equals(request.getUsername())) {
                        return userRepository.existsByUsername(request.getUsername())
                                .flatMap(usernameExists -> {
                                    if (usernameExists) {
                                        return Mono.error(new DuplicateEntityException("Username already exists"));
                                    }
                                    return Mono.just(false);
                                });
                    }

                    // Check if email is being changed and if it already exists
                    if (!existingUser.getEmail().equals(request.getEmail())) {
                        return userRepository.existsByEmail(request.getEmail())
                                .flatMap(emailExists -> {
                                    if (emailExists) {
                                        return Mono.error(new DuplicateEntityException("Email already exists"));
                                    }
                                    return Mono.just(false);
                                });
                    }

                    return Mono.just(false);
                })
                .then(userRepository.findById(id))
                .flatMap(user -> {
                    user.setUsername(request.getUsername());
                    user.setEmail(request.getEmail());

                    // Only update password if provided
                    if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                        // Validate password strength before updating
                        passwordService.validatePasswordStrength(request.getPassword());
                        user.setPasswordHash(passwordService.hashPassword(request.getPassword()));
                    }

                    user.preUpdate(); // Update timestamp

                    return userRepository.save(user)
                            .flatMap(this::toUserResponseWithRoles);
                });
    }

    /**
     * Soft delete a user (sets deleted_at timestamp)
     */
    public Mono<Void> deleteUser(Long id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("User not found")))
                .flatMap(user -> {
                    user.softDelete(); // Soft delete instead of hard delete
                    return userRepository.save(user).then();
                });
    }

    /**
     * Hard delete a user (permanently remove from database)
     * Use with caution!
     */
    public Mono<Void> hardDeleteUser(Long id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("User not found")))
                .flatMap(userRepository::delete);
    }

    /**
     * Restore a soft-deleted user
     */
    public Mono<UserResponse> restoreUser(Long id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("User not found")))
                .flatMap(user -> {
                    if (!user.isDeleted()) {
                        return Mono.error(new IllegalArgumentException("User is not deleted"));
                    }
                    user.restore();
                    return userRepository.save(user)
                            .flatMap(this::toUserResponseWithRoles);
                });
    }

    /**
     * Get user by ID
     */
    public Mono<UserResponse> getUserById(Long id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("User not found")))
                .flatMap(this::toUserResponseWithRoles);
    }

    /**
     * Get user by username
     */
    public Mono<User> getUserByUsername(String username) {
        return userRepository.findEnabledUserByUsername(username);
    }

    /**
     * Get all roles for a user
     */
    public Flux<Role> getUserRoles(Long userId) {
        return userRoleRepository.findByUserId(userId)
                .flatMap(userRole -> roleRepository.findById(userRole.getRoleId()));
    }

    /**
     * Get all users (without pagination - for backward compatibility)
     */
    public Flux<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .flatMap(this::toUserResponseWithRoles);
    }

    /**
     * Get all users with pagination
     *
     * @param pageRequest Pagination parameters
     * @return Page of UserResponse
     */
    public Mono<Page<UserResponse>> getAllUsers(PageRequest pageRequest) {
        Sort sort = pageRequest.getSort();
        int page = pageRequest.getPage();
        int size = pageRequest.getSize();

        // Get total count
        Mono<Long> totalCount = userRepository.countAll();

        // Get paginated data
        // Note: R2DBC doesn't have built-in pagination, so we use skip/take
        // For sorting, we need to convert Sort to Comparator for User entity
        Flux<User> userEntities = userRepository.findAll();

        // Apply sorting if specified
        if (sort.isSorted()) {
            Sort.Order order = sort.iterator().next();
            String property = order.getProperty();
            boolean ascending = order.isAscending();

            userEntities = userEntities.sort((u1, u2) -> {
                // Basic sorting by common fields
                if ("id".equals(property)) {
                    return ascending
                            ? Long.compare(u1.getId(), u2.getId())
                            : Long.compare(u2.getId(), u1.getId());
                } else if ("username".equals(property)) {
                    return ascending
                            ? u1.getUsername().compareTo(u2.getUsername())
                            : u2.getUsername().compareTo(u1.getUsername());
                } else if ("email".equals(property)) {
                    return ascending
                            ? u1.getEmail().compareTo(u2.getEmail())
                            : u2.getEmail().compareTo(u1.getEmail());
                }
                // Default: sort by ID ascending
                return Long.compare(u1.getId(), u2.getId());
            });
        }

        Flux<UserResponse> users = userEntities
                .skip(page * size)
                .take(size)
                .flatMap(this::toUserResponseWithRoles);

        return users.collectList()
                .zipWith(totalCount)
                .map(tuple -> {
                    List<UserResponse> content = tuple.getT1();
                    long total = tuple.getT2();

                    // Create PageImpl manually
                    return new PageImpl<>(
                            content,
                            org.springframework.data.domain.PageRequest.of(page, size, sort),
                            total
                    );
                });
    }

    /**
     * Convert User entity to UserResponse DTO with roles
     */
    private Mono<UserResponse> toUserResponseWithRoles(User user) {
        return userRoleRepository.findByUserId(user.getId())
                .flatMap(userRole -> roleRepository.findById(userRole.getRoleId()))
                .map(Role::getName)
                .collectList()
                .map(roleNames -> new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getEnabled(),
                        roleNames,
                        user.getCreatedAt(),
                        user.getUpdatedAt()
                ));
    }
}

