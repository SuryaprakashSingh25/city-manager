# API Gateway

The **API Gateway** serves as the single entry point for all external client
requests in the City Manager system. It is responsible for request routing,
authentication enforcement, and integration with service discovery.

---

## 📌 Responsibilities

- Acts as the centralized ingress for all frontend traffic
- Routes requests to backend microservices using service discovery
- Enforces authentication and authorization policies
- Applies cross-cutting concerns such as CORS and request filtering
- Prevents direct client access to internal services

---

## 🏗️ Service Context

- Built using **Spring Cloud Gateway**
- Registered with **Eureka Service Discovery**
- Uses load-balanced routing (`lb://`)
- Contains **no business logic**
- Stateless and horizontally scalable

---

## 🔀 Routing Configuration

Routing is configured using Spring Cloud Gateway and Eureka-based service discovery.

### Configured Routes

| Route ID | Path Predicate | Target Service |
|--------|---------------|----------------|
| user-service | `/api/users/**` | `lb://user-service` |
| issue-service | `/api/issues/**` | `lb://issue-service` |
| notification-service | `/notifications/**` | `lb://notification-service` |
| media-service | `/api/media/**` | `lb://media-service` |

All services are discovered dynamically via Eureka.  
Service IDs are normalized to lowercase.

---

## 🔐 Security Responsibilities

- Validates JWT tokens on incoming requests
- Rejects unauthenticated or malformed requests
- Propagates authenticated user context to downstream services
- Works alongside service-level authorization for defense in depth

---

## 🔍 Service Discovery

- Uses **Eureka Client**
- Automatically discovers registered services
- Eliminates hard-coded service URLs
- Enables dynamic scaling and failover

---

## ⚙️ Configuration & Dependencies

- Spring Cloud Gateway
- Spring Security
- Eureka Discovery Client
- Spring Cloud Config

---

## 📘 API Documentation

- API Gateway does not expose domain-specific APIs
- Swagger / OpenAPI documentation is provided by downstream services
- Gateway remains transparent to API consumers

---

## 🎯 Why This Service Matters

- Simplifies client-side communication
- Centralizes security enforcement
- Enables clean service isolation
- Supports scalable microservice interactions


