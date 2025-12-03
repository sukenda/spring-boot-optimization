package com.khas.optimization.service;

import com.khas.optimization.dto.UserRequest;
import com.khas.optimization.dto.UserResponse;
import com.khas.optimization.entity.User;
import com.khas.optimization.exception.EntityNotFoundException;
import com.khas.optimization.repository.RoleRepository;
import com.khas.optimization.repository.UserRepository;
import com.khas.optimization.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private RoleRepository roleRepository;
    
    @Mock
    private UserRoleRepository userRoleRepository;
    
    @Mock
    private PasswordService passwordService;
    
    @InjectMocks
    private UserService userService;
    
    private User testUser;
    private UserRequest userRequest;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedPassword");
        testUser.setEnabled(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        
        userRequest = new UserRequest();
        userRequest.setUsername("testuser");
        userRequest.setEmail("test@example.com");
        userRequest.setPassword("password123");
    }
    
    @Test
    @DisplayName("Should create user successfully")
    void testCreateUserSuccess() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(Mono.just(false));
        when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(false));
        when(passwordService.hashPassword(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(testUser));
        // Mock userRoleRepository for toUserResponseWithRoles
        when(userRoleRepository.findByUserId(1L)).thenReturn(Flux.empty());
        // Mock roleRepository for default role assignment (if needed)
        when(roleRepository.findByName(anyString())).thenReturn(Mono.empty());
        
        // When
        Mono<UserResponse> result = userService.createUser(userRequest);
        
        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals("testuser", response.getUsername());
                    assertEquals("test@example.com", response.getEmail());
                    assertTrue(response.getEnabled());
                })
                .verifyComplete();
        
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
        verify(passwordService).hashPassword("password123");
    }
    
    @Test
    @DisplayName("Should fail when username already exists")
    void testCreateUserUsernameExists() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(Mono.just(true));
        
        // When
        Mono<UserResponse> result = userService.createUser(userRequest);
        
        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> 
                    throwable instanceof RuntimeException &&
                    throwable.getMessage().equals("Username already exists"))
                .verify();
        
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    @DisplayName("Should fail when email already exists")
    void testCreateUserEmailExists() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(Mono.just(false));
        when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(true));
        
        // When
        Mono<UserResponse> result = userService.createUser(userRequest);
        
        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> 
                    throwable instanceof RuntimeException &&
                    throwable.getMessage().equals("Email already exists"))
                .verify();
        
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    @DisplayName("Should update user successfully")
    void testUpdateUserSuccess() {
        // Given
        UserRequest updateRequest = new UserRequest();
        updateRequest.setUsername("updateduser");
        updateRequest.setEmail("updated@example.com");
        updateRequest.setPassword("newpassword");
        
        when(userRepository.findById(1L)).thenReturn(Mono.just(testUser));
        when(userRepository.existsByUsername("updateduser")).thenReturn(Mono.just(false));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(Mono.just(false));
        when(passwordService.hashPassword("newpassword")).thenReturn("newHashedPassword");
        
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("updateduser");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setPasswordHash("newHashedPassword");
        updatedUser.setEnabled(true);
        updatedUser.setCreatedAt(testUser.getCreatedAt());
        updatedUser.setUpdatedAt(LocalDateTime.now());
        
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(updatedUser));
        // Mock userRoleRepository for toUserResponseWithRoles
        when(userRoleRepository.findByUserId(1L)).thenReturn(Flux.empty());
        
        // When
        Mono<UserResponse> result = userService.updateUser(1L, updateRequest);
        
        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals("updateduser", response.getUsername());
                    assertEquals("updated@example.com", response.getEmail());
                })
                .verifyComplete();
        
        verify(userRepository, times(2)).findById(1L);
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    @DisplayName("Should fail update when user not found")
    void testUpdateUserNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Mono.empty());
        
        // When
        Mono<UserResponse> result = userService.updateUser(1L, userRequest);
        
        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> 
                    throwable instanceof EntityNotFoundException &&
                    throwable.getMessage().equals("User not found"))
                .verify();
        
        // findById is called twice in updateUser (once for validation, once for update)
        verify(userRepository, atLeastOnce()).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    @DisplayName("Should update user without password when password not provided")
    void testUpdateUserWithoutPassword() {
        // Given
        UserRequest updateRequest = new UserRequest();
        updateRequest.setUsername("updateduser");
        updateRequest.setEmail("updated@example.com");
        updateRequest.setPassword(null); // No password update
        
        when(userRepository.findById(1L)).thenReturn(Mono.just(testUser));
        when(userRepository.existsByUsername("updateduser")).thenReturn(Mono.just(false));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(Mono.just(false));
        
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("updateduser");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setPasswordHash(testUser.getPasswordHash()); // Keep old password
        updatedUser.setEnabled(true);
        
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(updatedUser));
        // Mock userRoleRepository for toUserResponseWithRoles
        when(userRoleRepository.findByUserId(1L)).thenReturn(Flux.empty());
        
        // When
        Mono<UserResponse> result = userService.updateUser(1L, updateRequest);
        
        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals("updateduser", response.getUsername());
                })
                .verifyComplete();
        
        verify(passwordService, never()).hashPassword(anyString());
    }
    
    @Test
    @DisplayName("Should soft delete user successfully")
    void testDeleteUserSuccess() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Mono.just(testUser));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(testUser));
        
        // When
        Mono<Void> result = userService.deleteUser(1L);
        
        // Then
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
        assertTrue(testUser.isDeleted()); // Check soft delete was called
    }
    
    @Test
    @DisplayName("Should fail delete when user not found")
    void testDeleteUserNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Mono.empty());
        
        // When
        Mono<Void> result = userService.deleteUser(1L);
        
        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> 
                    throwable instanceof EntityNotFoundException &&
                    throwable.getMessage().equals("User not found"))
                .verify();
        
        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    @DisplayName("Should hard delete user successfully")
    void testHardDeleteUserSuccess() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Mono.just(testUser));
        when(userRepository.delete(any(User.class))).thenReturn(Mono.empty());
        
        // When
        Mono<Void> result = userService.hardDeleteUser(1L);
        
        // Then
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(userRepository).findById(1L);
        verify(userRepository).delete(testUser);
    }
    
    @Test
    @DisplayName("Should restore soft-deleted user successfully")
    void testRestoreUserSuccess() {
        // Given
        testUser.softDelete(); // Mark as deleted
        when(userRepository.findById(1L)).thenReturn(Mono.just(testUser));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(testUser));
        // Mock userRoleRepository for toUserResponseWithRoles
        when(userRoleRepository.findByUserId(1L)).thenReturn(Flux.empty());
        
        // When
        Mono<UserResponse> result = userService.restoreUser(1L);
        
        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals("testuser", response.getUsername());
                })
                .verifyComplete();
        
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
        assertFalse(testUser.isDeleted()); // Check restore was called
    }
    
    @Test
    @DisplayName("Should fail restore when user not found")
    void testRestoreUserNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Mono.empty());
        
        // When
        Mono<UserResponse> result = userService.restoreUser(1L);
        
        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> 
                    throwable instanceof EntityNotFoundException &&
                    throwable.getMessage().equals("User not found"))
                .verify();
    }
    
    @Test
    @DisplayName("Should fail restore when user is not deleted")
    void testRestoreUserNotDeleted() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Mono.just(testUser));
        
        // When
        Mono<UserResponse> result = userService.restoreUser(1L);
        
        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> 
                    throwable instanceof IllegalArgumentException &&
                    throwable.getMessage().equals("User is not deleted"))
                .verify();
    }
    
    @Test
    @DisplayName("Should get user by ID successfully")
    void testGetUserByIdSuccess() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Mono.just(testUser));
        // Mock userRoleRepository for toUserResponseWithRoles
        when(userRoleRepository.findByUserId(1L)).thenReturn(Flux.empty());
        
        // When
        Mono<UserResponse> result = userService.getUserById(1L);
        
        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals(1L, response.getId());
                    assertEquals("testuser", response.getUsername());
                    assertEquals("test@example.com", response.getEmail());
                })
                .verifyComplete();
        
        verify(userRepository).findById(1L);
    }
    
    @Test
    @DisplayName("Should fail get user by ID when not found")
    void testGetUserByIdNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Mono.empty());
        
        // When
        Mono<UserResponse> result = userService.getUserById(1L);
        
        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> 
                    throwable instanceof EntityNotFoundException &&
                    throwable.getMessage().equals("User not found"))
                .verify();
    }
    
    @Test
    @DisplayName("Should get user by username successfully")
    void testGetUserByUsernameSuccess() {
        // Given
        when(userRepository.findEnabledUserByUsername("testuser")).thenReturn(Mono.just(testUser));
        
        // When
        Mono<User> result = userService.getUserByUsername("testuser");
        
        // Then
        StepVerifier.create(result)
                .assertNext(user -> {
                    assertNotNull(user);
                    assertEquals("testuser", user.getUsername());
                })
                .verifyComplete();
        
        verify(userRepository).findEnabledUserByUsername("testuser");
    }
    
    @Test
    @DisplayName("Should get all users successfully")
    void testGetAllUsersSuccess() {
        // Given
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        
        when(userRepository.findAll()).thenReturn(Flux.just(testUser, user2));
        // Mock userRoleRepository for toUserResponseWithRoles
        when(userRoleRepository.findByUserId(1L)).thenReturn(Flux.empty());
        when(userRoleRepository.findByUserId(2L)).thenReturn(Flux.empty());
        
        // When
        Flux<UserResponse> result = userService.getAllUsers();
        
        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals("testuser", response.getUsername());
                })
                .assertNext(response -> {
                    assertEquals("user2", response.getUsername());
                })
                .verifyComplete();
        
        verify(userRepository).findAll();
    }
    
    @Test
    @DisplayName("Should return empty flux when no users exist")
    void testGetAllUsersEmpty() {
        // Given
        when(userRepository.findAll()).thenReturn(Flux.empty());
        
        // When
        Flux<UserResponse> result = userService.getAllUsers();
        
        // Then
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(userRepository).findAll();
    }
}

