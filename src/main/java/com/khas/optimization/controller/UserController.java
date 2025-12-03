package com.khas.optimization.controller;

import com.khas.optimization.annotation.RequiresRole;
import com.khas.optimization.dto.ApiResponse;
import com.khas.optimization.dto.PageRequest;
import com.khas.optimization.dto.PaginatedResponse;
import com.khas.optimization.dto.UserRequest;
import com.khas.optimization.dto.UserResponse;
import com.khas.optimization.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * User Controller
 * Provides CRUD endpoints for user management
 */
@Tag(name = "User Management", description = "User CRUD operations")
@SecurityRequirement(name = "bearer-jwt")
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * Create a new user
     * POST /api/users
     * Requires: RoleConstants.ROLE_ADMIN
     * 
     * Note: Annotation value must be string literal (compile-time constant)
     */
    @RequiresRole("ROLE_ADMIN") // See RoleConstants.ROLE_ADMIN
    @Operation(
        summary = "Create user",
        description = "Creates a new user account (Admin only)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "User created successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden - Admin role required"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Username or email already exists"
        )
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ApiResponse<UserResponse>>> createUser(@Valid @RequestBody UserRequest request) {
        return userService.createUser(request)
                .map(user -> ApiResponse.success(user, "User created successfully"))
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
        // Error handling is done by GlobalExceptionHandler
    }
    
    /**
     * Get user by ID
     * GET /api/users/{id}
     * Requires: RoleConstants.ROLE_ADMIN or RoleConstants.ROLE_MODERATOR
     * 
     * Note: Annotation values must be string literals (compile-time constants)
     */
    @RequiresRole({"ROLE_ADMIN", "ROLE_MODERATOR"}) // See RoleConstants
    @Operation(
        summary = "Get user by ID",
        description = "Retrieves a user by their ID (Admin or Moderator)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "User found",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden - Admin or Moderator role required"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "User not found"
        )
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ApiResponse<UserResponse>>> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> ApiResponse.success(user, "User retrieved successfully"))
                .map(ResponseEntity::ok);
        // Error handling is done by GlobalExceptionHandler
    }
    
    /**
     * Get all users with pagination
     * GET /api/users?page=0&size=10&sort=username,asc
     * Requires: RoleConstants.ROLE_ADMIN or RoleConstants.ROLE_MODERATOR
     * 
     * Query Parameters:
     * - page: Page number (0-indexed, default: 0)
     * - size: Page size (default: 10, max: 100)
     * - sort: Sort field and direction (format: "field,direction", default: "id,asc")
     * 
     * Note: Annotation values must be string literals (compile-time constants)
     */
    @RequiresRole({"ROLE_ADMIN", "ROLE_MODERATOR"}) // See RoleConstants
    @Operation(
        summary = "Get all users with pagination",
        description = "Retrieves paginated list of users (excludes soft-deleted) (Admin or Moderator)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Paginated list of users",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden - Admin or Moderator role required"
        )
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PaginatedResponse<UserResponse>>> getAllUsers(
            @Valid PageRequest pageRequest) {
        return userService.getAllUsers(pageRequest)
                .map(page -> PaginatedResponse.of(
                    page.getContent(),
                    page.getNumber(),
                    page.getSize(),
                    page.getTotalElements(),
                    "Users retrieved successfully"
                ))
                .map(ResponseEntity::ok);
        // Error handling is done by GlobalExceptionHandler
    }
    
    /**
     * Update user
     * PUT /api/users/{id}
     * Requires: RoleConstants.ROLE_ADMIN or RoleConstants.ROLE_MODERATOR
     * 
     * Note: Annotation values must be string literals (compile-time constants)
     */
    @RequiresRole({"ROLE_ADMIN", "ROLE_MODERATOR"}) // See RoleConstants
    @Operation(
        summary = "Update user",
        description = "Updates an existing user. Password is optional. (Admin or Moderator)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "User updated successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden - Admin or Moderator role required"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "User not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Username or email already exists"
        )
    })
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ApiResponse<UserResponse>>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request) {
        return userService.updateUser(id, request)
                .map(user -> ApiResponse.success(user, "User updated successfully"))
                .map(ResponseEntity::ok);
        // Error handling is done by GlobalExceptionHandler
    }
    
    /**
     * Delete user
     * DELETE /api/users/{id}
     * Requires: RoleConstants.ROLE_ADMIN
     * 
     * Note: Annotation value must be string literal (compile-time constant)
     */
    @RequiresRole("ROLE_ADMIN") // See RoleConstants.ROLE_ADMIN
    @Operation(
        summary = "Delete user (soft delete)",
        description = "Soft deletes a user by setting deletedAt timestamp (Admin only)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "204",
            description = "User deleted successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden - Admin role required"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "User not found"
        )
    })
    @DeleteMapping(value = "/{id}")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
        // Error handling is done by GlobalExceptionHandler
    }
}

