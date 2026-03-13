# HyperMall System Architecture

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [High-Level Architecture Diagram](#high-level-architecture-diagram)
3. [System Components](#system-components)
4. [Data Flow](#data-flow)
5. [Infrastructure Architecture](#infrastructure-architecture)
6. [Security Architecture](#security-architecture)
7. [Scalability Design](#scalability-design)
8. [Deployment Architecture](#deployment-architecture)

---

## 1. Architecture Overview

HyperMall is built using a **microservices architecture** with the following characteristics:

- **Service Discovery**: Netflix Eureka for dynamic service registration and discovery
- **Centralized Configuration**: Spring Cloud Config Server for externalized configuration
- **API Gateway**: Single entry point for all client requests with routing, authentication, and rate limiting
- **Database per Service**: Each service owns its data and exposes APIs
- **Asynchronous Communication**: RabbitMQ for event-driven communication
- **Distributed Tracing**: Log correlation across services

### Key Architectural Principles
1. **Single Responsibility**: Each service handles one business domain
2. **Loose Coupling**: Services communicate via well-defined APIs
3. **High Cohesion**: Related functionality grouped together
4. **Resilience**: Circuit breakers and fault tolerance patterns
5. **Observability**: Health checks, logging, and monitoring

---

## 2. High-Level Architecture Diagram

```
                                    ┌─────────────────────────────────────────────┐
                                    │                                             │
                                    │           INTERNET / CLIENTS                │
                                    │                                             │
                                    └──────────────────┬──────────────────────────┘
                                                       │
                                    ┌──────────────────▼──────────────────────────┐
                                    │                                             │
                                    │              CDN (CloudFlare)                │
                                    │                                             │
                                    └──────────────────┬──────────────────────────┘
                                                       │
                                    ┌──────────────────▼──────────────────────────┐
                                    │                                             │
                                    │    LOAD BALANCER (Nginx / Cloud LB)          │
                                    │                                             │
                                    └──────────────────┬──────────────────────────┘
                                                       │
                                    ┌──────────────────▼──────────────────────────┐
                                    │                                             │
                                    │            API GATEWAY (8080)               │
                                    │    ┌─────────────────────────────────────┐  │
                                    │    │ • JWT Authentication                │  │
                                    │    │ • Rate Limiting (100-500 req/min)  │  │
                                    │    │ • Request Routing                  │  │
                                    │    │ • Circuit Breaker                  │  │
                                    │    │ • CORS Configuration               │  │
                                    │    └─────────────────────────────────────┘  │
                                    │                                             │
                                    └──────────────────┬──────────────────────────┘
                                                       │
                    ┌───────────────────────────────────┼───────────────────────────────────┐
                    │                                   │                                   │
     ┌──────────────▼──────────────┐    ┌──────────────▼──────────────┐    ┌────────────▼────────────┐
     │                             │    │                             │    │                         │
     │   SERVICE REGISTRY (8761)  │    │    CONFIG SERVER (8888)     │    │   BUSINESS SERVICES     │
     │                             │    │                             │    │                         │
     │   ┌───────────────────┐    │    │   ┌───────────────────┐    │    │   ┌─────────────────┐    │
     │   │  Eureka Server    │    │    │   │  Spring Cloud    │    │    │   │  user-service   │    │
     │   │  (eureka/eureka) │    │    │   │  Config Server    │    │    │   │    (8081)       │    │
     │   └───────────────────┘    │    │   └───────────────────┘    │    │   └─────────────────┘    │
     └───────────────────────────┘    └───────────────────────────┘    │   ┌─────────────────┐    │
                                                                       │   │  product-service│    │
                                                                       │   │    (8082)       │    │
                                                                       │   └─────────────────┘    │
                                                                       │   ┌─────────────────┐    │
                                                                       │   │  cart-service   │    │
                                                                       │   │    (8083)       │    │
                                                                       │   └─────────────────┘    │
                                                                       │           ...            │
                                                                       │   ┌─────────────────┐    │
                                                                       │   │ analytics-svc   │    │
                                                                       │   │    (8095)       │    │
                                                                       │   └─────────────────┘    │
                                                                       └───────────────────────────┘
                                                                                  │
                    ┌───────────────────────────────────────────────────────────────────────────────┘
                    │
     ┌──────────────▼──────────────┐
     │                             │
     │     MESSAGE BROKER          │
     │        (RabbitMQ)            │
     │    ┌───────────────────┐    │
     │    │  • order.created  │    │
     │    │  • payment.done   │    │
     │    │  • stock.reserved │    │
     │    └───────────────────┘    │
     └──────────────────────────────┘
                    │
┌───────────────────┼───────────────────┐
│                   │                   │
│  ┌──────────────▼─┴──────────────────▼┐
│  │                                   │
│  │      DATA LAYER                   │
│  │  ┌─────────────────────────────┐  │
│  │  │  MySQL Cluster (8.0)        │  │
│  │  │  • hypermall_users          │  │
│  │  │  • hypermall_products      │  │
│  │  │  • hypermall_order         │  │
│  │  │  • ... (13 schemas total)  │  │
│  │  └─────────────────────────────┘  │
│  │                                   │
│  │  ┌─────────────────────────────┐  │
│  │  │  Redis Cluster (7.x)        │  │
│  │  │  • Sessions                 │  │
│  │  │  • Cart (cart-service)      │  │
│  │  │  • Rate Limiting            │  │
│  │  └─────────────────────────────┘  │
│  │                                   │
│  │  ┌─────────────────────────────┐  │
│  │  │  Elasticsearch (8.x)        │  │
│  │  │  • Product Search Index     │  │
│  │  └─────────────────────────────┘  │
│  │                                   │
│  └───────────────────────────────────┘
```

---

## 3. System Components

### 3.1 API Gateway (Port 8080)

The API Gateway is the single entry point for all client requests.

**Responsibilities:**
- Request routing to appropriate microservices
- JWT authentication and authorization
- Rate limiting (100 req/min anonymous, 500 req/min authenticated)
- Circuit breaker integration
- Request/response logging
- CORS handling

**Technology:** Spring Cloud Gateway

**Key Configuration:**
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/auth/**,/api/users/**
        - id: product-service
          uri: lb://product-service
          predicates:
            - Path=/api/products/**,/api/categories/**
```

### 3.2 Service Registry (Port 8761)

**Responsibilities:**
- Service registration and discovery
- Health monitoring of registered services
- Load balancing support

**Technology:** Netflix Eureka Server

**Credentials:** eureka / eureka123

### 3.3 Config Server (Port 8888)

**Responsibilities:**
- Centralized configuration management
- Environment-specific configurations
- Secure credential storage

**Technology:** Spring Cloud Config Server

**Credentials:** config / config123

### 3.4 Business Services

| Service | Port | Purpose | Database |
|---------|------|---------|----------|
| user-service | 8081 | Authentication, User management | MySQL |
| product-service | 8082 | Products, Categories, Brands | MySQL |
| cart-service | 8083 | Shopping cart | Redis |
| order-service | 8084 | Order management | MySQL |
| payment-service | 8085 | Payment processing | MySQL |
| inventory-service | 8086 | Stock management | MySQL |
| shipping-service | 8087 | Shipping integration | MySQL |
| promotion-service | 8088 | Vouchers, Flash sales | MySQL |
| review-service | 8089 | Reviews, Ratings | MySQL |
| search-service | 8090 | Elasticsearch integration | Elasticsearch |
| notification-service | 8091 | Notifications | MySQL |
| ai-service | 8092 | AI features | MySQL |
| media-service | 8093 | File uploads | MySQL |
| seller-service | 8094 | Seller portal | MySQL |
| analytics-service | 8095 | Analytics | MySQL |

---

## 4. Data Flow

### 4.1 User Registration Flow
```
┌─────────┐     ┌─────────────┐     ┌──────────────┐     ┌───────────┐
│ Client  │────►│ API Gateway │────►│ user-service │────►│   MySQL   │
└─────────┘     └─────────────┘     └──────────────┘     └───────────┘
                     │                     │
                     │◄────────────────────┘
                     │  JWT Token Response
                     │
                     ▼
                ┌─────────┐
                │  Redis  │  (Session cache)
                └─────────┘
```

### 4.2 Product Search Flow
```
┌─────────┐     ┌─────────────┐     ┌──────────────┐     ┌────────────┐
│ Client  │────►│ API Gateway │────►│search-service│────►│Elasticsearch│
└─────────┘     └─────────────┘     └──────────────┘     └────────────┘
                     │                     │
                     │◄────────────────────┘
                     │  Search Results
                     │
                     ▼
                ┌──────────────┐     ┌───────────┐
                │product-service│◄────│   MySQL   │
                └──────────────┘     └───────────┘
                     │   (Product details)
                     │◄────────────────────┘
                     ▼
                ┌─────────┐
                │ Client  │
```

### 4.3 Order Creation Flow (Synchronous)
```
┌─────────┐     ┌─────────────┐     ┌────────────┐     ┌────────────┐
│ Client  │────►│ API Gateway │────►│cart-service │────►│   Redis    │
└─────────┘     └─────────────┘     └────────────┘     └────────────┘
                     │                      │
                     │◄─────────────────────┘
                     │  Cart Response
                     │
                     ▼
                ┌────────────┐     ┌───────────┐     ┌────────────┐
                │order-service│────►│  MySQL    │────►│inventory-  │
                └────────────┘     └───────────┘     │  service   │
                     │                                 └────────────┘
                     │◄────────────────────────────────────────────┘
                     │   Order Confirmation
                     ▼
                ┌─────────────────┐     ┌──────────────────┐
                │notification-    │────►│  RabbitMQ        │
                │service         │     │  (order.created) │
                └─────────────────┘     └──────────────────┘
```

### 4.4 Payment Flow (Asynchronous)
```
┌─────────┐     ┌─────────────┐     ┌────────────┐
│ Client  │────►│ API Gateway │────►│payment-    │
└─────────┘     └─────────────┘     │service    │
                     │              └────────────┘
                     │                  │
                     │                  ▼
                     │             ┌─────────┐
                     │             │ VNPay/  │
                     │             │ MoMo/   │
                     │             │ ZaloPay │
                     │             └─────────┘
                     │                  │
                     │◄─────────────────┘
                     │  Payment URL/QR
                     ▼
                ┌─────────┐
                │ Client  │
                │(redirect│
                │ to pay) │
                └─────────┘
                     │
                     │ (Webhook)
                     ▼
                ┌─────────────┐     ┌─────────────┐
                │payment-     │────►│order-service│
                │service     │     │(update status)│
                └─────────────┘     └─────────────┘
```

---

## 5. Infrastructure Architecture

### 5.1 Docker Infrastructure

```
┌─────────────────────────────────────────────────────────────────┐
│                      Docker Network: hypermall                  │
│                                                                 │
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐      │
│  │   MySQL 8.0   │  │  Redis 7.x    │  │  RabbitMQ 3   │      │
│  │               │  │               │  │               │      │
│  │  Port: 3306   │  │  Port: 6379   │  │  Ports:5672   │      │
│  │               │  │               │  │          15672│      │
│  └───────────────┘  └───────────────┘  └───────────────┘      │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │              Elasticsearch 8.x                           │  │
│  │                                                           │  │
│  │  Port: 9200                                              │  │
│  │                                                           │  │
│  └───────────────────────────────────────────────────────────┘  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 5.2 Database Schema Distribution

| Service | Schema | Tables |
|---------|--------|--------|
| user-service | hypermall_users | users, roles, user_roles, addresses |
| product-service | hypermall_products | products, product_images, product_variants, categories, brands |
| order-service | hypermall_order | orders, order_items |
| payment-service | hypermall_payment | payments, refunds |
| inventory-service | hypermall_inventory | inventory, stock_movements |
| shipping-service | hypermall_shipping | shipping_methods, shipments |
| promotion-service | hypermall_promotion | vouchers, flash_sales |
| review-service | hypermall_reviews | reviews, review_images |
| notification-service | hypermall_notification | notifications |
| seller-service | hypermall_seller | sellers |
| media-service | hypermall_media | media_files |
| analytics-service | hypermall_analytics | analytics_events |

### 5.3 Redis Usage

| Data Type | Service | TTL |
|-----------|---------|-----|
| Cart (Hash) | cart-service | 30 days |
| Rate Limit Counter | api-gateway | 1 minute |
| User Session | user-service | 7 days |

---

## 6. Security Architecture

### 6.1 Authentication Flow
```
┌─────────┐     ┌─────────────┐     ┌──────────────┐     ┌───────────┐
│ Client  │────►│ API Gateway │────►│ user-service │────►│   MySQL   │
└─────────┘     └─────────────┘     └──────────────┘     └───────────┘
                     │                     │
                     │◄────────────────────┘
                     │  JWT Access + Refresh Token
                     │
                     ▼
                ┌─────────┐
                │  Redis  │  (Store refresh token)
                └─────────┘
```

### 6.2 Token Structure

**Access Token:**
- Type: JWT (RS256)
- Expiry: 15 minutes
- Claims: userId, email, roles

**Refresh Token:**
- Type: JWT (RS256)
- Expiry: 7 days
- Storage: Server-side (Redis)

### 6.3 Security Layers

1. **Network Level**: Firewall, Load Balancer
2. **Application Level**: API Gateway authentication
3. **Service Level**: Method-level authorization (@PreAuthorize)
4. **Data Level**: Encrypted database fields for sensitive data

### 6.4 Role-Based Access Control

| Role | Permissions |
|------|-------------|
| ROLE_USER | Browse products, manage cart, place orders, write reviews |
| ROLE_SELLER | All USER permissions + manage products, view seller dashboard |
| ROLE_ADMIN | All permissions + manage users, categories, brands, view analytics |

---

## 7. Scalability Design

### 7.1 Horizontal Scaling
- All services are stateless and can scale horizontally
- Session data stored in Redis (shared across instances)
- Database connections pooled

### 7.2 Caching Strategy

| Cache Level | Technology | Data |
|-------------|------------|------|
| L1 Cache | Caffeine | Recently accessed products in product-service |
| L2 Cache | Redis | Cart data, user sessions |
| CDN | CloudFlare | Static assets (images, CSS, JS) |

### 7.3 Database Scaling
- Read replicas for read-heavy services (product-service, search-service)
- Sharding for large tables (orders, products)
- Connection pooling via HikariCP

### 7.4 Circuit Breaker

```
                    ┌──────────────┐
                    │   Request   │
                    └──────┬──────┘
                           │
                    ┌──────▼──────┐
                    │  Circuit    │
                    │  Closed     │
                    └──────┬──────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
       ┌──────▼──────┐ ┌────▼────┐ ┌────▼────┐
       │ Service A   │ │Service B│ │Service C│
       │ (Success)   │ │(Success)│ │(Fail x3)│
       └──────┬──────┘ └────┬────┘ └────┬────┘
              │            │            │
              └────────────┼────────────┘
                           │
                    ┌──────▼──────┐
                    │ Circuit     │
                    │ OPEN        │
                    │ (Falling   │
                    │  Back)     │
                    └────────────┘
```

**Configuration:**
- Failure threshold: 50%
- Wait duration: 10 seconds
- Timeout: 3 seconds

---

## 8. Deployment Architecture

### 8.1 Development Environment

```
┌─────────────────────────────────────────────────────────────────┐
│                        Developer Machine                        │
│                                                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │   Backend    │  │   Frontend   │  │   Docker     │          │
│  │  (Maven/IDE) │  │  (VS Code)   │  │   Desktop    │          │
│  │              │  │              │  │              │          │
│  │ 18 Services  │  │  React App   │  │Infrastructure│          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│                                                                 │
│  Ports: 3000, 8080-8095, 3306, 6379, 5672, 9200                 │
└─────────────────────────────────────────────────────────────────┘
```

### 8.2 Production Environment (High-Level)

```
                    ┌─────────────────────┐
                    │    CDN (CloudFlare) │
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │  Load Balancer      │
                    │  (AWS ALB/Nginx)    │
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │  API Gateway Cluster│
                    │  (3+ instances)     │
                    └──────────┬──────────┘
                               │
         ┌─────────────────────┼─────────────────────┐
         │                     │                     │
┌────────▼────────┐   ┌───────▼───────┐   ┌────────▼────────┐
│  Service Group 1│   │ Service Group 2│   │ Service Group 3│
│ • user-service  │   │ • order-service│   │ • analytics-   │
│ • product-service│  │ • payment-service│  │   service      │
│ • search-service │  │ • inventory-     │   └────────────────┘
│                  │   │   service        │
└──────────────────┘   └──────────────────┘
         │                     │
         │    ┌────────────────┼────────────────┐
         │    │                │                │
         ▼    ▼                ▼                ▼
    ┌─────────┐          ┌─────────┐        ┌─────────┐
    │MySQL    │          │MySQL    │        │Elasticsearch│
    │Primary  │◄─────────│Primary  │        │Cluster     │
    └────┬────┘          └────┬────┘        └─────────┘
         │                    │
    ┌────▼────┐          ┌────▼────┐
    │MySQL    │          │MySQL    │
    │Replica  │          │Replica  │
    └─────────┘          └─────────┘
```

### 8.3 Service Startup Order

```
┌──────────────┐
│     DNS      │
└──────┬───────┘
       │
┌──────▼────────┐      ┌──────────────┐
│  Infrastructure│ ──► │ 1. MySQL    │
│   (Docker)     │      │ 2. Redis    │
└───────────────┘      │ 3. RabbitMQ │
                       │ 4. ES       │
                       └──────┬───────┘
                              │
                    ┌──────────▼──────────┐
                    │  Spring Services  │
                    │                    │
                    │ 1. service-registry│
                    │    (Eureka :8761)  │
                    │                    │
                    │ 2. config-server   │
                    │    (:8888)         │
                    │                    │
                    │ 3. api-gateway     │
                    │    (:8080)         │
                    │                    │
                    │ 4. Business       │
                    │    Services        │
                    │    (:8081-8095)    │
                    └───────────────────┘
```

---

## Appendix: Port Reference

| Service | Port | Health Endpoint |
|---------|------|-----------------|
| service-registry | 8761 | /actuator/health |
| config-server | 8888 | /actuator/health |
| api-gateway | 8080 | /actuator/health |
| user-service | 8081 | /actuator/health |
| product-service | 8082 | /actuator/health |
| cart-service | 8083 | /actuator/health |
| order-service | 8084 | /actuator/health |
| payment-service | 8085 | /actuator/health |
| inventory-service | 8086 | /actuator/health |
| shipping-service | 8087 | /actuator/health |
| promotion-service | 8088 | /actuator/health |
| review-service | 8089 | /actuator/health |
| search-service | 8090 | /actuator/health |
| notification-service | 8091 | /actuator/health |
| ai-service | 8092 | /actuator/health |
| media-service | 8093 | /actuator/health |
| seller-service | 8094 | /actuator/health |
| analytics-service | 8095 | /actuator/health |

---

*Last Updated: 2024-03-13*
