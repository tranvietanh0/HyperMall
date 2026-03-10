# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

HyperMall is an e-commerce platform (Shopee/Lazada clone) using microservices architecture with Spring Cloud (backend) and React (frontend).

## Tech Stack

- **Frontend**: React 18 + TypeScript + Vite + TailwindCSS + Redux Toolkit
- **Backend**: Spring Boot 3.4.3 (Java 17) + Spring Cloud 2024.0.0
- **Infrastructure**: MySQL 8.0, Redis, RabbitMQ, Elasticsearch 8.x
- **Service Discovery**: Netflix Eureka
- **Configuration**: Spring Cloud Config Server

## Development Commands

### Frontend (frontend/hypermall-web/)
```bash
npm run dev           # Start dev server (port 3000)
npm run build         # Build: tsc && vite build
npm run lint          # ESLint with TypeScript rules
npm run test          # Run all Vitest tests
npm run test:coverage # Run tests with coverage

# Run single test file
npx vitest run src/path/to/file.test.ts

# Run tests matching pattern
npx vitest run -t "test name pattern"
```

### Backend
```bash
# Build all services (from backend/)
mvn clean install -DskipTests

# Build single service
cd backend/<service-name> && mvn clean package -DskipTests

# Run service
cd backend/<service-name> && mvn spring-boot:run

# Run tests
cd backend/<service-name> && mvn test

# Run single test class
mvn test -Dtest=ClassName

# Run single test method
mvn test -Dtest=ClassName#methodName

# STARTUP ORDER (required):
# 1. service-registry (8761)
# 2. config-server (8888)
# 3. api-gateway (8080)
# 4. business services
```

### Infrastructure
```bash
cd infrastructure/docker
docker-compose -f docker-compose.dev.yml up -d
```

## Architecture

### Backend Microservices (Ports)
| Service | Port | Status | Description |
|---------|------|--------|-------------|
| service-registry | 8761 | ✅ | Eureka Server (eureka/eureka123) |
| config-server | 8888 | ✅ | Spring Cloud Config (config/config123) |
| api-gateway | 8080 | ✅ | Routes, JWT auth, rate limiting |
| user-service | 8081 | ✅ | Auth, User CRUD, Address |
| product-service | 8082 | ✅ | Product, Category, Brand |
| cart-service | 8083 | ✅ | Shopping Cart (Redis) |
| order-service | 8084 | ✅ | Orders, Order Items |
| payment-service | 8085 | ✅ | VNPay, MoMo, ZaloPay, COD |
| inventory-service | 8086 | ✅ | Stock, Reservation, Movement |
| shipping-service | 8087 | ✅ | GHN, GHTK, ViettelPost |
| promotion-service | 8088 | ✅ | Vouchers, Flash Sales |
| review-service | 8089 | ✅ | Reviews, Ratings, Likes |
| search-service | 8090 | ✅ | Elasticsearch Integration |
| notification-service | 8091 | ⏳ | Email/SMS/Push |
| ai-service | 8092 | ⏳ | Chatbot, Recommendations |
| media-service | 8093 | ⏳ | File Uploads |
| seller-service | 8094 | ⏳ | Seller Center |
| analytics-service | 8095 | ⏳ | Analytics |

### Key URLs
- **Frontend**: http://localhost:3000
- **API Gateway**: http://localhost:8080/api/*
- **Eureka Dashboard**: http://localhost:8761
- **Swagger UI**: http://localhost:{service-port}/swagger-ui.html
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)

### Shared Library (backend/common-lib/)
All microservices depend on `common-lib`:
- **DTOs**: `ApiResponse<T>`, `PageResponse<T>`, `ErrorResponse`
- **Exceptions**: `BaseException`, `ResourceNotFoundException`, `ValidationException`, `BadRequestException`, `UnauthorizedException`, `ForbiddenException`, `ConflictException`, `GlobalExceptionHandler`
- **Security**: `JwtTokenProvider`, `JwtAuthenticationFilter`, `UserPrincipal`, `@CurrentUser` annotation
- **Utils**: `DateTimeUtil`, `StringUtil`, `ValidationUtil`
- **Events**: `BaseEvent`, `EventPublisher`
- **Config**: `JacksonConfig`, `AsyncConfig`, `RedisConfig`

### Frontend Structure (frontend/hypermall-web/src/)
- **Path aliases**: `@components`, `@pages`, `@hooks`, `@services`, `@store`, `@types`, `@utils`, `@config`
- **State**: Redux Toolkit slices in `store/slices/`
- **API**: Services in `services/` use axios, proxied to `localhost:8080/api`

## Key Patterns

- All services expose health checks at `/actuator/health`
- Service config files in `config-server/src/main/resources/configurations/`
- MapStruct for DTO mapping, Lombok available in all modules
- Parent POM (`backend/pom.xml`) manages all dependency versions

## Database

Each microservice has its own schema (auto-created via `infrastructure/docker/mysql/init.sql`):
- `hypermall_users` (user-service)
- `hypermall_products` (product-service)
- `hypermall_order` (order-service)
- `hypermall_payment` (payment-service)
- `hypermall_inventory` (inventory-service)
- `hypermall_shipping` (shipping-service)
- `hypermall_promotion` (promotion-service)

Note: cart-service uses Redis, not MySQL.

## Troubleshooting

If a service fails to start:
1. Check Eureka: http://localhost:8761
2. Check Config Server: http://localhost:8888
3. Check MySQL and Redis are running
4. Check database schema exists
5. Review logs for connection errors
