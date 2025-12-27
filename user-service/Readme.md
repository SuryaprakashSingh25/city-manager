# User Service

The **User Service** manages user authentication, identity, and role lifecycle within
the City Manager system. It acts as the **single source of truth** for users and roles
and exposes authentication APIs used by the frontend and API Gateway.

---

## 1. Service Responsibilities

- User registration & authentication
- Email verification
- JWT access & refresh token management
- Password reset workflows
- User role management (ADMIN-only)
- Publishing role-related domain events

---

## 2. Service Context

- Exposed via **API Gateway**
- Registered with **Eureka Service Discovery**
- Stateless authentication using **JWT**
- Central authority for user roles used by downstream services

---

## 3. Security & Role-Based Access

| Endpoint | Method | Description | Role |
|--------|--------|-------------|------|
| `/api/users/register` | POST | Register new user | Public |
| `/api/users/login` | POST | Authenticate user | Public |
| `/api/users/verify` | GET | Verify email | Public |
| `/api/users/forgot-password` | POST | Request password reset | Public |
| `/api/users/reset-password` | POST | Reset password | Public |
| `/api/users/refresh-token` | POST | Refresh JWT token | Public |
| `/api/users/allUsers` | GET | Fetch all users | ADMIN |
| `/api/users/allUsers/{userId}/role` | PUT | Change user role | ADMIN |

✔ Method-level security enforced using `@PreAuthorize`

---

## 4. API Endpoints Overview

### Register User
**POST** `/api/users/register`

- Creates a new user
- Default role assigned: **CITIZEN**
- Email verification token generated
- Password securely hashed

---

### Login
**POST** `/api/users/login`

- Authenticates user credentials
- Returns:
  - Access Token (JWT)
  - Refresh Token

---

### Verify Email
**GET** `/api/users/verify?token=`

- Verifies user email via token
- Activates account

---

### Forgot Password
**POST** `/api/users/forgot-password`

- Initiates password reset flow
- Sends reset link via email

---

### Reset Password
**POST** `/api/users/reset-password`

- Resets password using valid reset token
- Invalidates old credentials

---

### Refresh Token
**POST** `/api/users/refresh-token`

- Issues new access token
- Validates refresh token

---

### Get All Users
**GET** `/api/users/allUsers`

- ADMIN-only endpoint
- Returns list of all registered users

---

### Change User Role
**PUT** `/api/users/allUsers/{userId}/role`

- ADMIN-only
- Updates user role (e.g., CITIZEN → STAFF)
- Publishes role update event

---

## 5. Event-Driven Communication (Kafka)

### a Published Events
- **UserRoleUpdatedEvent**
  - Emitted when user role changes
  - Enables other services to react asynchronously

### b Consumed Events
- None

---

## 6. Internal Design Notes

- Uses **hexagonal architecture**
  - `exposition` → controllers & DTOs
  - `application` → use cases
  - `domain` → core logic
- JWT tokens carry user ID & role claims
- Role-based authorization enforced at controller level
- Password & token flows designed to be secure and stateless

---

## 7. Configuration & Dependencies

- MongoDB – user persistence
- Kafka – domain event publishing
- Spring Security + JWT
- Config Server – externalized configuration

---

## 8. API Documentation

- Swagger / OpenAPI enabled
- Accessible via API Gateway
- Acts as the source of truth for request/response schemas

---

## 9. Why This Service Matters

- Centralized authentication & authorization
- Decouples identity concerns from business services
- Enables secure, scalable role-based access across the system

---

