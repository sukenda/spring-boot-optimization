# API Documentation

Complete API reference for Spring Boot optimization.

## Base URL

```
http://localhost:8087
```

## Authentication

This API uses JWT (JSON Web Token) for authentication. Include the token in the Authorization header:

```
Authorization: Bearer <your-token>
```

## Authentication Endpoints

### Login

Generate a JWT token by providing username and password.

**Endpoint:** `POST /api/auth/login`

**Request Body:**

```json
{
  "username": "string",
  "password": "string"
}
```

**Response (Success - 200):**

```json
{
  "success": true,
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "testuser"
}
```

**Response (Error - 401):**

```json
{
  "success": false,
  "message": "Invalid username or password"
}
```

**Example:**

```bash
curl -X POST http://localhost:8087/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

### Validate Token

Validate a JWT token.

**Endpoint:** `GET /api/auth/validate`

**Headers:**

```
Authorization: Bearer <token>
```

**Response (Valid - 200):**

```json
{
  "valid": true,
  "username": "testuser",
  "roles": [
    "USER"
  ]
}
```

**Response (Invalid - 200):**

```json
{
  "valid": false,
  "message": "Token is invalid or expired"
}
```

## User Management Endpoints

### Create User

Create a new user account.

**Endpoint:** `POST /api/users`

**Headers:**

```
Authorization: Bearer <token>
Content-Type: application/json
```

**Request Body:**

```json
{
  "username": "string (3-100 chars)",
  "email": "string (valid email)",
  "password": "string (min 6 chars)"
}
```

**Response (Success - 201):**

```json
{
  "id": 1,
  "username": "newuser",
  "email": "newuser@example.com",
  "enabled": true,
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-01T00:00:00"
}
```

**Response (Error - 409):**

```
Username or email already exists
```

**Example:**

```bash
curl -X POST http://localhost:8087/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "username": "newuser",
    "email": "newuser@example.com",
    "password": "password123"
  }'
```

### Get All Users

Retrieve all users (excludes soft-deleted).

**Endpoint:** `GET /api/users`

**Headers:**

```
Authorization: Bearer <token>
```

**Response (200):**

```json
[
  {
    "id": 1,
    "username": "user1",
    "email": "user1@example.com",
    "enabled": true,
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  },
  {
    "id": 2,
    "username": "user2",
    "email": "user2@example.com",
    "enabled": true,
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  }
]
```

### Get User by ID

Retrieve a specific user by ID.

**Endpoint:** `GET /api/users/{id}`

**Path Parameters:**

- `id` (Long) - User ID

**Headers:**

```
Authorization: Bearer <token>
```

**Response (Success - 200):**

```json
{
  "id": 1,
  "username": "testuser",
  "email": "test@example.com",
  "enabled": true,
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-01T00:00:00"
}
```

**Response (Not Found - 404):**

```
User not found
```

### Update User

Update an existing user.

**Endpoint:** `PUT /api/users/{id}`

**Path Parameters:**

- `id` (Long) - User ID

**Headers:**

```
Authorization: Bearer <token>
Content-Type: application/json
```

**Request Body:**

```json
{
  "username": "string (3-100 chars)",
  "email": "string (valid email)",
  "password": "string (min 6 chars, optional)"
}
```

**Response (Success - 200):**

```json
{
  "id": 1,
  "username": "updateduser",
  "email": "updated@example.com",
  "enabled": true,
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-01T01:00:00"
}
```

**Note:** Password is optional. If not provided, the existing password will be kept.

### Delete User

Soft delete a user (sets deletedAt timestamp).

**Endpoint:** `DELETE /api/users/{id}`

**Path Parameters:**

- `id` (Long) - User ID

**Headers:**

```
Authorization: Bearer <token>
```

**Response (Success - 204):**

```
No Content
```

**Response (Not Found - 404):**

```
User not found
```

**Note:** This is a soft delete. The user record remains in the database but is marked as deleted.

## System Endpoints

### System Information

Get system and memory information.

**Endpoint:** `GET /api/system-info`

**Response (200):**

```json
{
  "heap_used_mb": 150,
  "heap_max_mb": 384,
  "non_heap_used_mb": 45,
  "available_processors": 4,
  "system_load_average": 0.5,
  "uptime_seconds": 3600,
  "java_version": "21.0.1",
  "virtual_threads_enabled": true
}
```

### Protected Endpoint

Example protected endpoint requiring JWT authentication.

**Endpoint:** `GET /api/protected`

**Headers:**

```
Authorization: Bearer <token>
```

**Response (200):**

```json
{
  "message": "This is a protected endpoint",
  "username": "testuser",
  "roles": [
    "USER"
  ],
  "timestamp": "2024-01-01T00:00:00Z"
}
```

## Actuator Endpoints

### Health Check

**Endpoint:** `GET /actuator/health`

**Response (200):**

```json
{
  "status": "UP"
}
```

### Application Info

**Endpoint:** `GET /actuator/info`

### Metrics

**Endpoint:** `GET /actuator/metrics`

## Error Responses

### Standard Error Format

```json
{
  "timestamp": "2024-01-01T00:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/users"
}
```

### HTTP Status Codes

| Code | Description                                           |
|------|-------------------------------------------------------|
| 200  | OK - Request successful                               |
| 201  | Created - Resource created successfully               |
| 204  | No Content - Request successful, no content to return |
| 400  | Bad Request - Invalid request data                    |
| 401  | Unauthorized - Authentication required or failed      |
| 404  | Not Found - Resource not found                        |
| 409  | Conflict - Resource already exists                    |
| 500  | Internal Server Error - Server error                  |

## Rate Limiting

Currently, no rate limiting is implemented. Consider adding rate limiting for production use.

## Pagination

User list endpoints do not currently support pagination. For large datasets, consider implementing pagination.

## Filtering and Sorting

Filtering and sorting are not currently implemented. Consider adding these features for production use.

