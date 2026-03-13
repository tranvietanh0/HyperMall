# HyperMall Project Overview and Product Development Requirements (PDR)

## Table of Contents
1. [Project Overview](#project-overview)
2. [Problem Statement](#problem-statement)
3. [Target Users](#target-users)
4. [Product Features](#product-features)
5. [Functional Requirements](#functional-requirements)
6. [Non-Functional Requirements](#non-functional-requirements)
7. [Technical Architecture](#technical-architecture)
8. [Data Models](#data-models)
9. [API Requirements](#api-requirements)
10. [Security Requirements](#security-requirements)
11. [Acceptance Criteria](#acceptance-criteria)
12. [Milestones](#milestones)

---

## 1. Project Overview

### Project Name
**HyperMall** - Enterprise E-Commerce Platform

### Project Type
Full-stack microservices-based e-commerce web application

### Project Summary
HyperMall is a comprehensive e-commerce platform inspired by leading marketplaces like Shopee and Lazada. It provides a complete online shopping experience with multi-vendor support, advanced product search, secure payments, and real-time order tracking. The platform is built using modern microservices architecture to ensure scalability, reliability, and maintainability.

### Business Objectives
- Create a scalable e-commerce platform supporting 100,000+ concurrent users
- Enable multiple sellers to operate within a single marketplace
- Provide seamless shopping experience across web and mobile platforms
- Support multiple payment methods popular in the Vietnamese market
- Ensure 99.9% uptime for production environment

---

## 2. Problem Statement

The current e-commerce landscape in Vietnam lacks a unified platform that combines:
- Multi-vendor marketplace functionality with individual seller portals
- Integrated logistics support from multiple carriers (GHN, GHTK, ViettelPost)
- Multiple payment options including local payment gateways (VNPay, MoMo, ZaloPay)
- Real-time inventory management across warehouses
- Advanced product search with filters and recommendations

---

## 3. Target Users

### Primary User Segments

| User Type | Description | Key Features |
|-----------|-------------|--------------|
| **Shoppers** | End consumers browsing and purchasing products | Product search, cart management, order tracking, reviews |
| **Sellers** | Vendors managing their online stores | Product management, order fulfillment, analytics, promotions |
| **Admins** | Platform administrators | User management, category management, platform analytics |

### User Roles
- **ROLE_ADMIN**: Full system access, user management, analytics
- **ROLE_SELLER**: Seller portal access, product management, order management
- **ROLE_USER**: Standard shopper access, cart, orders, reviews

---

## 4. Product Features

### 4.1 Customer Features
- User registration and authentication (email, phone)
- Product browsing with advanced filters (category, brand, price, rating)
- Full-text product search with Elasticsearch
- Shopping cart management (add, update, remove items)
- Order placement and tracking
- Product reviews and ratings
- Wishlist/favorites
- Multiple address management

### 4.2 Seller Features
- Seller registration and verification
- Product CRUD operations (draft, published, archived)
- Product variants (size, color, etc.)
- Inventory management
- Order management and fulfillment
- Sales analytics dashboard
- Voucher and promotion management

### 4.3 Admin Features
- Category and brand management
- User and seller management
- Platform-wide analytics
- System configuration
- Content moderation

### 4.4 Payment Features
- VNPay integration
- MoMo integration
- ZaloPay integration
- Cash on Delivery (COD)
- Payment status tracking
- Refund processing

### 4.5 Shipping Features
- GHN (Giao Hàng Nhanh) integration
- GHTK (Giao Hàng Tiết Kiệm) integration
- ViettelPost integration
- Real-time shipping fee calculation
- Order tracking

### 4.6 Promotion Features
- Voucher codes (percentage, fixed amount, free shipping)
- Flash sales with countdown timers
- Product bundling
- Category-wide promotions

---

## 5. Functional Requirements

### 5.1 Authentication & Authorization
- **FR-AUTH-001**: Users can register with email and password
- **FR-AUTH-002**: Users can login with email/password
- **FR-AUTH-003**: JWT-based authentication with refresh tokens
- **FR-AUTH-004**: Password reset via email
- **FR-AUTH-005**: Role-based access control (RBAC)

### 5.2 Product Management
- **FR-PROD-001**: Create products with multiple variants
- **FR-PROD-002**: Upload product images
- **FR-PROD-003**: Set product pricing (base price, sale price)
- **FR-PROD-004**: Product categorization with hierarchical categories
- **FR-PROD-005**: Brand management
- **FR-PROD-006**: Product search with Elasticsearch
- **FR-PROD-007**: Product filtering and sorting

### 5.3 Cart & Checkout
- **FR-CART-001**: Add/remove items to cart
- **FR-CART-002**: Update item quantities
- **FR-CART-003**: Calculate cart totals with promotions
- **FR-CART-004**: Checkout flow with address selection
- **FR-CART-005**: Apply voucher codes

### 5.4 Order Management
- **FR-ORDER-001**: Create orders from cart
- **FR-ORDER-002**: Order confirmation emails
- **FR-ORDER-003**: Order status tracking (pending, confirmed, shipping, delivered, cancelled)
- **FR-ORDER-004**: Order cancellation (before shipping)
- **FR-ORDER-005**: Order history

### 5.5 Inventory Management
- **FR-INV-001**: Track stock levels per product variant
- **FR-INV-002**: Reserve stock during checkout
- **FR-INV-003**: Release stock on order cancellation
- **FR-INV-004**: Stock movement history

### 5.6 Review System
- **FR-REVIEW-001**: Submit reviews with ratings (1-5 stars)
- **FR-REVIEW-002**: Upload review images
- **FR-REVIEW-003**: Like/helpful on reviews
- **FR-REVIEW-004**: Average rating calculation

---

## 6. Non-Functional Requirements

### 6.1 Performance
- **NFR-PERF-001**: API response time < 200ms for 95th percentile
- **NFR-PERF-002**: Product search results < 500ms
- **NFR-PERF-003**: Support 100,000 concurrent users
- **NFR-PERF-004**: Page load time < 3 seconds

### 6.2 Scalability
- **NFR-SCAL-001**: Horizontal scaling for all microservices
- **NFR-SCAL-002**: Database partitioning strategy
- **NFR-SCAL-003**: Caching strategy with Redis
- **NFR-SCAL-004**: Load balancing via API Gateway

### 6.3 Availability
- **NFR-AVAIL-001**: 99.9% uptime for production
- **NFR-AVAIL-002**: Multi-region deployment capability
- **NFR-AVAIL-003**: Circuit breaker pattern for fault tolerance
- **NFR-AVAIL-004**: Rate limiting to prevent abuse

### 6.4 Security
- **NFR-SEC-001**: All API communications over HTTPS
- **NFR-SEC-002**: JWT tokens with 15-minute expiry
- **NFR-SEC-003**: Refresh tokens with 7-day expiry
- **NFR-SEC-004**: Input validation and sanitization
- **NFR-SEC-005**: SQL injection prevention
- **NFR-SEC-006**: XSS protection

### 6.5 Maintainability
- **NFR-MAIN-001**: Comprehensive API documentation (Swagger)
- **NFR-MAIN-002**: Structured logging across services
- **NFR-MAIN-002**: Health check endpoints for all services
- **NFR-MAIN-003**: Graceful shutdown handling

---

## 7. Technical Architecture

### 7.1 Backend Architecture
```
                                    +------------------+
                                    |   API Gateway    |
                                    |   (Port 8080)    |
                                    +--------+---------+
                                             |
         +-------------+-------------+--------+---------+-------------+
         |             |             |                 |             |
    +----+----+  +----+----+  +-----+-----+    +-------+-----+  +-----+-----+
    |  User    |  | Product  |  |   Cart    |    |   Order     |  | Payment  |
    | Service  |  | Service  |  | Service   |    |   Service    |  | Service  |
    | (8081)   |  | (8082)   |  | (8083)    |    |   (8084)     |  | (8085)   |
    +-----------+  +-----------+  +-----------+    +-------------+  +----------+

    +-----------+  +-----------+  +-----------+    +-------------+  +----------+
    | Inventory |  | Shipping  |  |Promotion  |    |   Review    |  | Search   |
    | Service   |  | Service   |  | Service   |    |   Service   |  | Service  |
    | (8086)    |  | (8087)    |  | (8088)    |    |   (8089)    |  | (8090)   |
    +-----------+  +-----------+  +-----------+    +-------------+  +----------+
```

### 7.2 Technology Stack

| Layer | Technology |
|-------|------------|
| Frontend | React 18 + TypeScript + Vite + TailwindCSS + Redux Toolkit |
| Backend | Spring Boot 3.4.3 + Spring Cloud 2024.0.0 |
| Service Discovery | Netflix Eureka |
| Configuration | Spring Cloud Config Server |
| API Gateway | Spring Cloud Gateway |
| Database | MySQL 8.0 |
| Cache | Redis 7 |
| Message Queue | RabbitMQ 3 |
| Search | Elasticsearch 8.x |
| Security | JWT |

### 7.3 Infrastructure Services
- **MySQL 8.0**: Primary database (13 schemas)
- **Redis 7**: Session storage, caching, cart (cart-service)
- **RabbitMQ**: Asynchronous messaging between services
- **Elasticsearch**: Product search indexing

---

## 8. Data Models

### 8.1 Core Entities

| Entity | Service | Key Fields |
|--------|---------|------------|
| User | user-service | id, email, password_hash, first_name, last_name, phone, roles |
| Address | user-service | id, user_id, full_name, phone, province, district, ward, address_line |
| Product | product-service | id, seller_id, name, slug, description, category_id, brand_id, base_price, sale_price |
| ProductVariant | product-service | id, product_id, sku, name, price, stock |
| Category | product-service | id, name, slug, parent_id, sort_order |
| Brand | product-service | id, name, slug, logo |
| Cart | cart-service | user_id, items[] |
| Order | order-service | id, user_id, status, total_amount, shipping_address, payment_method |
| OrderItem | order-service | id, order_id, product_id, variant_id, quantity, price |
| Inventory | inventory-service | product_id, variant_id, quantity, reserved |
| Review | review-service | id, product_id, user_id, rating, comment, images[] |
| Voucher | promotion-service | id, code, type, value, min_order_amount, start_date, end_date |

---

## 9. API Requirements

### 9.1 API Design Standards
- All APIs return `ApiResponse<T>` wrapper
- RESTful conventions (GET/POST/PUT/DELETE)
- Pagination support for list endpoints
- Consistent error response format

### 9.2 Key API Endpoints

| Category | Endpoint | Auth Required |
|----------|----------|---------------|
| Auth | POST /api/auth/register | No |
| Auth | POST /api/auth/login | No |
| Auth | POST /api/auth/refresh | No |
| Products | GET /api/products | No |
| Products | GET /api/products/{id} | No |
| Cart | GET /api/cart | Yes |
| Cart | POST /api/cart/items | Yes |
| Orders | POST /api/orders | Yes |
| Orders | GET /api/orders | Yes |
| Payments | POST /api/payments/create | Yes |
| Reviews | POST /api/reviews | Yes |
| Search | GET /api/search | No |

### 9.3 API Gateway Routes

| Path Pattern | Service |
|--------------|---------|
| /api/auth/** | user-service |
| /api/users/** | user-service |
| /api/products/** | product-service |
| /api/categories/** | product-service |
| /api/brands/** | product-service |
| /api/cart/** | cart-service |
| /api/orders/** | order-service |
| /api/payments/** | payment-service |
| /api/inventory/** | inventory-service |
| /api/shipping/** | shipping-service |
| /api/vouchers/** | promotion-service |
| /api/reviews/** | review-service |
| /api/search/** | search-service |

---

## 10. Security Requirements

### 10.1 Authentication
- JWT-based authentication
- Access token: 15 minutes expiry
- Refresh token: 7 days expiry
- Secure token storage (HttpOnly cookies recommended)

### 10.2 Authorization
- Role-based access control (RBAC)
- Pre-authorization checks in API Gateway
- Method-level security in services

### 10.3 Data Protection
- Password hashing with BCrypt
- Input validation on all endpoints
- SQL injection prevention via parameterized queries
- CORS configuration

---

## 11. Acceptance Criteria

### 11.1 Authentication
- [ ] User can register with email and password
- [ ] User can login and receive JWT tokens
- [ ] User can refresh expired tokens
- [ ] Unauthorized requests return 401

### 11.2 Products
- [ ] Products display with pagination
- [ ] Product filters work correctly
- [ ] Product search returns relevant results
- [ ] Product details show all information

### 11.3 Cart & Checkout
- [ ] User can add items to cart
- [ ] Cart persists across sessions
- [ ] Checkout creates order successfully
- [ ] Vouchers apply correctly

### 11.4 Orders
- [ ] Order status updates correctly
- [ ] Order history displays correctly
- [ ] Cancellation works before shipping

### 11.5 Payments
- [ ] Payment integration works (at least one method)
- [ ] Payment status updates order

### 11.6 Performance
- [ ] API response time < 200ms
- [ ] No memory leaks during extended use
- [ ] Graceful degradation under load

---

## 12. Milestones

### Phase 1: Foundation (Weeks 1-4)
- Infrastructure setup (Docker, databases)
- Service registry and config server
- API Gateway implementation
- User service with authentication
- Basic product and category services

### Phase 2: Core Commerce (Weeks 5-8)
- Cart service implementation
- Order service implementation
- Inventory service implementation
- Basic payment integration

### Phase 3: Advanced Features (Weeks 9-12)
- Search service with Elasticsearch
- Review and rating system
- Promotion/voucher system
- Shipping integration

### Phase 4: Polish (Weeks 13-16)
- Admin dashboard features
- Analytics service
- Performance optimization
- Testing and bug fixes
- Production deployment

---

## Document Version

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2024-01-01 | HyperMall Team | Initial PDR |

## Related Documents

- [API Documentation](./API.md)
- [Deployment Guide](./DEPLOYMENT.md)
- [Local Testing Guide](./LOCAL_TESTING.md)
- [System Architecture](./system-architecture.md)
- [Code Standards](./code-standards.md)
