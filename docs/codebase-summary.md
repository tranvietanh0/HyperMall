# HyperMall Codebase Summary

## Overview

This document provides a comprehensive summary of the HyperMall e-commerce platform codebase, including project structure, key technologies, and architectural patterns.

## Project Statistics

| Metric | Value |
|--------|-------|
| Total Backend Services | 18 |
| Frontend Application | 1 (React + TypeScript) |
| Infrastructure Services | 4 (MySQL, Redis, RabbitMQ, Elasticsearch) |
| Programming Languages | Java (Backend), TypeScript (Frontend) |
| Build Tools | Maven (Backend), npm (Frontend) |

## Directory Structure

```
HyperMall/
├── backend/                    # Backend microservices
│   ├── pom.xml               # Parent POM (dependency management)
│   ├── common-lib/           # Shared library (all services depend on this)
│   ├── service-registry/     # Netflix Eureka Server (Port 8761)
│   ├── config-server/        # Spring Cloud Config Server (Port 8888)
│   ├── api-gateway/          # API Gateway (Port 8080)
│   ├── user-service/         # Authentication & User Management (Port 8081)
│   ├── product-service/      # Products, Categories, Brands (Port 8082)
│   ├── cart-service/         # Shopping Cart (Port 8083, uses Redis)
│   ├── order-service/        # Orders & Order Items (Port 8084)
│   ├── payment-service/      # Payment Processing (Port 8085)
│   ├── inventory-service/    # Stock Management (Port 8086)
│   ├── shipping-service/     # Shipping Integration (Port 8087)
│   ├── promotion-service/    # Vouchers & Flash Sales (Port 8088)
│   ├── review-service/       # Reviews & Ratings (Port 8089)
│   ├── search-service/       # Elasticsearch Integration (Port 8090)
│   ├── notification-service/ # Email/SMS/Push Notifications (Port 8091)
│   ├── ai-service/           # AI Features (Port 8092)
│   ├── media-service/        # File Uploads (Port 8093)
│   ├── seller-service/       # Seller Center (Port 8094)
│   └── analytics-service/   # Analytics Dashboard (Port 8095)
├── frontend/
│   └── hypermall-web/       # React + TypeScript frontend
│       ├── src/
│       │   ├── components/  # Reusable UI components
│       │   ├── pages/      # Page components
│       │   ├── hooks/      # Custom React hooks
│       │   ├── services/   # API service layer
│       │   ├── store/      # Redux store and slices
│       │   ├── types/      # TypeScript type definitions
│       │   ├── utils/      # Utility functions
│       │   └── config/     # Configuration files
│       └── package.json
├── infrastructure/
│   └── docker/             # Docker Compose configurations
├── docs/                   # Documentation
├── scripts/                # Utility scripts
└── README.md
```

## Backend Services Detail

### Core Infrastructure Services

| Service | Port | Purpose | Technology |
|---------|------|---------|------------|
| service-registry | 8761 | Service Discovery | Netflix Eureka |
| config-server | 8888 | Configuration Management | Spring Cloud Config |
| api-gateway | 8080 | Routing, Auth, Rate Limiting | Spring Cloud Gateway |

### Business Services

| Service | Port | Database | Key Dependencies |
|---------|------|----------|-----------------|
| user-service | 8081 | MySQL | common-lib, Eureka |
| product-service | 8082 | MySQL | common-lib, Eureka |
| cart-service | 8083 | Redis | common-lib, Eureka, Redis |
| order-service | 8084 | MySQL | common-lib, Eureka, RabbitMQ |
| payment-service | 8085 | MySQL | common-lib, Eureka |
| inventory-service | 8086 | MySQL | common-lib, Eureka |
| shipping-service | 8087 | MySQL | common-lib, Eureka |
| promotion-service | 8088 | MySQL | common-lib, Eureka |
| review-service | 8089 | MySQL | common-lib, Eureka |
| search-service | 8090 | Elasticsearch | common-lib, Eureka |
| notification-service | 8091 | MySQL + RabbitMQ | common-lib, Eureka |
| ai-service | 8092 | MySQL | common-lib, Eureka |
| media-service | 8093 | MySQL (files stored locally/S3) | common-lib, Eureka |
| seller-service | 8094 | MySQL | common-lib, Eureka |
| analytics-service | 8095 | MySQL | common-lib, Eureka |

## Common Library (common-lib)

The `common-lib` module provides shared components used by all microservices:

### DTOs (Data Transfer Objects)
- `ApiResponse<T>` - Standard API response wrapper
- `PageResponse<T>` - Paginated response
- `ErrorResponse` - Error response structure

### Exceptions
- `BaseException` - Base exception class
- `ResourceNotFoundException` - 404 errors
- `ValidationException` - 400 validation errors
- `BadRequestException` - 400 bad request
- `UnauthorizedException` - 401 unauthorized
- `ForbiddenException` - 403 forbidden
- `ConflictException` - 409 conflict
- `GlobalExceptionHandler` - Global exception handler

### Security
- `JwtTokenProvider` - JWT token generation/validation
- `JwtAuthenticationFilter` - Request authentication filter
- `UserPrincipal` - Authenticated user details
- `@CurrentUser` - Annotation to inject current user

### Utilities
- `DateTimeUtil` - Date/time formatting
- `StringUtil` - String manipulation
- `ValidationUtil` - Input validation

### Events
- `BaseEvent` - Base event class
- `EventPublisher` - RabbitMQ event publisher

### Configuration
- `JacksonConfig` - JSON serialization config
- `AsyncConfig` - Async processing config
- `RedisConfig` - Redis connection config

## Frontend Architecture

### Technology Stack
- React 18 with TypeScript
- Vite as build tool
- TailwindCSS for styling
- Redux Toolkit for state management
- React Router DOM 6 for routing
- Formik + Yup for form validation
- Axios for HTTP requests
- Vitest for testing

### Path Aliases
```typescript
@           // src/ root
@components // src/components/
@pages      // src/pages/
@hooks      // src/hooks/
@services   // src/services/
@store      // src/store/
@types      // src/types/
@utils      // src/utils/
@config     // src/config/
```

### Directory Structure (Frontend)
```
src/
├── components/           # Reusable components
│   ├── common/          # Generic components (Button, Input, etc.)
│   ├── layout/          # Layout components (Header, Footer, Sidebar)
│   └── features/       # Feature-specific components
├── pages/               # Page components
│   ├── Home/
│   ├── Product/
│   ├── Cart/
│   ├── Checkout/
│   ├── Order/
│   ├── User/
│   └── Seller/
├── hooks/               # Custom React hooks
├── services/            # API service layer
├── store/               # Redux store
│   └── slices/         # Redux slices
├── types/               # TypeScript definitions
├── utils/               # Utility functions
├── config/              # Configuration
└── App.tsx             # Main application component
```

## Database Schemas

### MySQL Databases (13 total)
| Database | Service | Tables |
|----------|---------|--------|
| hypermall_users | user-service | users, roles, user_roles, addresses |
| hypermall_products | product-service | products, product_images, product_variants, categories, brands |
| hypermall_cart | cart-service | (Uses Redis instead) |
| hypermall_order | order-service | orders, order_items |
| hypermall_payment | payment-service | payments, refunds |
| hypermall_inventory | inventory-service | inventory, stock_movements |
| hypermall_shipping | shipping-service | shipping_methods, shipments |
| hypermall_promotion | promotion-service | vouchers, flash_sales, promotions |
| hypermall_reviews | review-service | reviews, review_images, review_likes |
| hypermall_search | search-service | (Elasticsearch indices) |
| hypermall_notification | notification-service | notifications, notification_preferences |
| hypermall_media | media-service | media_files |
| hypermall_seller | seller-service | sellers, seller_verifications |
| hypermall_analytics | analytics-service | analytics_events, reports |

### Redis Usage
- **cart-service**: Shopping cart storage (hash per user)
- **api-gateway**: Rate limiting counters

### Elasticsearch Indices
- **products**: Product search index (name, description, category, brand)

## API Gateway

### Features
- **Service Discovery**: Auto-discovers services via Eureka
- **Authentication**: JWT token validation
- **Rate Limiting**: 100 req/min (anonymous), 500 req/min (authenticated)
- **Circuit Breaker**: Resilience4j integration
- **CORS**: Configured for frontend origins

### Routing
All requests go through `http://localhost:8080/api/*` which routes to appropriate services based on URL patterns.

## Key Patterns

### Service Communication
1. **Synchronous**: REST calls via Spring WebClient
2. **Asynchronous**: RabbitMQ message publishing for events

### Error Handling
- All services use `GlobalExceptionHandler` from common-lib
- Standardized error response format
- Proper HTTP status codes

### Configuration Management
- External configuration via Spring Cloud Config Server
- Service-specific config in `config-server/src/main/resources/configurations/`

### Security Flow
1. User registers/logins via user-service
2. JWT access token (15 min) and refresh token (7 days) issued
3. All authenticated requests include Bearer token
4. API Gateway validates token before routing
5. Services use @CurrentUser to get authenticated user

## Build & Run

### Backend
```bash
# Build all services
cd backend
mvn clean install -DskipTests

# Run individual service
cd backend/user-service
mvn spring-boot:run
```

### Frontend
```bash
cd frontend/hypermall-web

# Install dependencies
npm install

# Development
npm run dev

# Production build
npm run build

# Run tests
npm run test
```

### Infrastructure
```bash
cd infrastructure/docker

# Start all infrastructure
docker-compose -f docker-compose.dev.yml up -d
```

## Environment Configuration

### Required Environment Variables
| Variable | Description |
|----------|-------------|
| DB_USERNAME | MySQL username |
| DB_PASSWORD | MySQL password |
| REDIS_HOST | Redis host |
| REDIS_PORT | Redis port |
| JWT_SECRET | Base64-encoded JWT signing key |

### Service Startup Order
1. service-registry (8761)
2. config-server (8888)
3. api-gateway (8080)
4. Business services (8081+)

## Development Guidelines

### Adding New Service
1. Create Spring Boot project with parent `backend/pom.xml`
2. Add common-lib dependency
3. Configure Eureka client in application.yml
4. Add config in config-server
5. Implement service with proper layering (controller/service/repository)
6. Add API routes in api-gateway

### Adding New Frontend Feature
1. Create page component in `src/pages/`
2. Add route in App.tsx
3. Create Redux slice if needed
4. Create API service method
5. Add components in `src/components/`

## Testing

### Backend Testing
```bash
# Run all tests
mvn test

# Run single test class
mvn test -Dtest=ClassName
```

### Frontend Testing
```bash
# Run tests (watch mode)
npm run test

# Run with coverage
npm run test:coverage

# Run single test file
npx vitest run src/path/to/file.test.ts
```

## CI/CD

GitHub Actions workflows are configured in `.github/workflows/`:
- Build and test on push/PR
- Docker image building (optional)

## Documentation

| Document | Location |
|----------|----------|
| API Docs | `/docs/API.md` |
| Deployment | `/docs/DEPLOYMENT.md` |
| Local Testing | `/docs/LOCAL_TESTING.md` |
| PDR | `/docs/project-overview-pdr.md` |
| Code Standards | `/docs/code-standards.md` |
| Architecture | `/docs/system-architecture.md` |

## Related Files

- **CLAUDE.md**: Project-specific AI instructions
- **AGENTS.md**: Agent coordination instructions
- **PLAN.md**: Development roadmap
- **REQUIREMENTS.md**: Detailed requirements

---

*Last Updated: 2024-03-13*
