# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

HyperMall is a large-scale e-commerce platform using microservices architecture with Spring Cloud (backend) and React (frontend). The project aims to replicate functionality similar to Shopee/Lazada/Tiki with AI integration for chatbot, recommendations, and image search.

## Tech Stack

- **Frontend**: React 18 + TypeScript + Vite + TailwindCSS + Redux Toolkit
- **Backend**: Spring Boot 3.4.3 (Java 17) + Spring Cloud 2024.0.0
- **Infrastructure**: MySQL 8.0, Redis, RabbitMQ, Elasticsearch 8.x
- **Service Discovery**: Netflix Eureka
- **Configuration**: Spring Cloud Config Server

## Development Commands

### Frontend (frontend/hypermall-web/)
```bash
npm run dev        # Start dev server (port 3000)
npm run build      # Build: tsc && vite build
npm run lint       # ESLint with TypeScript rules
npm run test       # Run Vitest tests
npm run test:coverage  # Run tests with coverage
```

### Backend
```bash
# Build all services from backend/
mvn clean install -DskipTests

# Run individual service
cd backend/<service-name>
mvn spring-boot:run

# Start services in order: service-registry → config-server → api-gateway → other services
```

### Infrastructure
```bash
# Start dev infrastructure (MySQL, Redis, RabbitMQ, Elasticsearch)
cd infrastructure/docker
docker-compose -f docker-compose.dev.yml up -d
```

## Architecture

### Backend Microservices (Ports)
| Service | Port | Description |
|---------|------|-------------|
| service-registry | 8761 | Eureka Server (credentials: eureka/eureka123) |
| config-server | 8888 | Spring Cloud Config (credentials: config/config123) |
| api-gateway | 8080 | Routes all API requests, JWT auth, rate limiting |
| user-service | 8081 | User & Authentication |
| product-service | 8082 | Products & Categories |
| cart-service | 8083 | Shopping Cart |
| order-service | 8084 | Orders |
| payment-service | 8085 | Payment Integration |
| inventory-service | 8086 | Stock Management |
| shipping-service | 8087 | Shipping |
| promotion-service | 8088 | Vouchers & Flash Sales |
| review-service | 8089 | Reviews & Ratings |
| search-service | 8090 | Elasticsearch Integration |
| notification-service | 8091 | Email/SMS/Push |
| ai-service | 8092 | Chatbot, Recommendations |
| media-service | 8093 | File Uploads |
| seller-service | 8094 | Seller Center |
| analytics-service | 8095 | Analytics |

### Shared Library (backend/common-lib/)
All microservices depend on `common-lib` which provides:
- **DTOs**: `ApiResponse<T>`, `PageResponse<T>`, `ErrorResponse`
- **Exceptions**: `BaseException`, `ResourceNotFoundException`, `ValidationException`, `BadRequestException`, `UnauthorizedException`, `ForbiddenException`, `ConflictException`, `GlobalExceptionHandler`
- **Security**: `JwtTokenProvider`, `JwtAuthenticationFilter`, `UserPrincipal`, `@CurrentUser` annotation
- **Utils**: `DateTimeUtil`, `StringUtil`, `ValidationUtil`
- **Events**: `BaseEvent`, `EventPublisher`
- **Config**: `JacksonConfig`, `AsyncConfig`, `RedisConfig`

### Frontend Structure (frontend/hypermall-web/src/)
- **Path aliases**: `@components`, `@pages`, `@hooks`, `@services`, `@store`, `@types`, `@utils`, `@config`
- **State**: Redux Toolkit slices in `store/slices/` (auth, cart, product, ui)
- **API**: Services in `services/` use axios with base URL proxied to `localhost:8080/api`

### Implemented Services

| Service | Status | Key Features |
|---------|--------|--------------|
| service-registry | ✅ | Eureka Server |
| config-server | ✅ | Centralized config |
| api-gateway | ✅ | Routing, JWT filter, Rate limiting |
| user-service | ✅ | Auth (register/login/JWT), User CRUD, Address management |

## Key Patterns

- All backend services expose health checks at `/actuator/health`
- Service configuration is centralized in `config-server/src/main/resources/configurations/`
- Frontend dev server proxies `/api/*` requests to the API Gateway
- Use MapStruct for DTO mapping (configured in parent POM)
- Lombok is available in all backend modules
