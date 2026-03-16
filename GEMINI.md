# HyperMall - AI Assistant Context

This file provides essential context, architectural patterns, and development standards for the HyperMall project.

## Project Overview
HyperMall is a comprehensive microservices-based e-commerce platform inspired by Shopee and Lazada. It supports multi-vendor seller portals, advanced search, multiple payment/shipping integrations, and real-time inventory management.

### Tech Stack
- **Backend:** Java 17, Spring Boot 3.4.3, Spring Cloud 2024.0.0 (Eureka, Config, Gateway)
- **Frontend:** React 18, TypeScript, Vite, TailwindCSS, Redux Toolkit, Zustand, Formik
- **Infrastructure:** MySQL 8.0, Redis 7, RabbitMQ 3, Elasticsearch 8.x, Docker
- **Testing:** JUnit 5, Mockito, AssertJ (Backend); Vitest, React Testing Library (Frontend)

## Architecture & Structure
The project follows a microservices architecture with 18 specialized services.

- `backend/`: Maven multi-module project.
    - `common-lib/`: Shared DTOs, exceptions, security, and utilities.
    - `service-registry/`: Netflix Eureka server (Port 8761).
    - `config-server/`: Spring Cloud Config server (Port 8888).
    - `api-gateway/`: Spring Cloud Gateway (Port 8080).
    - `user-service/`, `product-service/`, `order-service/`, etc.: Core business services.
- `frontend/hypermall-web/`: React + Vite storefront application.
- `infrastructure/docker/`: Docker Compose files for development and production environments.
- `docs/`: Comprehensive documentation on API, standards, and architecture.

## Development Workflows

### 1. Infrastructure Setup
```bash
cd infrastructure/docker
docker-compose -f docker-compose.dev.yml up -d
```

### 2. Backend Development
- **Build all:** `mvn clean install -DskipTests` in `backend/`.
- **Run service:** `mvn spring-boot:run` in a service directory (e.g., `backend/user-service`).
- **Required Order:** Start `service-registry` -> `config-server` -> `api-gateway` -> other services.
- **Active Profile:** Use `dev` profile locally.

### 3. Frontend Development
```bash
cd frontend/hypermall-web
npm install
npm run dev
```

## Coding Standards & Conventions

Refer to `docs/code-standards.md` for full details. Key highlights:

### Java (Backend)
- **Naming:** PascalCase for classes, camelCase for methods/variables. `{EntityName}Service`, `{EntityName}Controller`.
- **Injection:** Prefer constructor injection over `@Autowired`.
- **DTOs:** Use MapStruct for mapping between Entities and DTOs.
- **Lombok:** Use `@Getter`, `@Setter`, `@Builder`, and `@RequiredArgsConstructor`.
- **Errors:** Use custom exceptions extending `com.hypermall.common.exception.BaseException`.

### React (Frontend)
- **Naming:** PascalCase for components (`ProductCard.tsx`), camelCase for hooks/utils.
- **State:** Redux Toolkit for global UI state, Zustand for lightweight feature state, `useState` for local state.
- **Styling:** TailwindCSS with a "precision commerce" design language (70-80% neutral, restrained accents).
- **Icons:** Heroicons (Outline/Solid).

### API Design
- RESTful endpoints using plural nouns (`/api/products`).
- Standard response format: `{ "success": boolean, "message": string, "data": T }`.
- Pagination using Spring Data Pageable style.

## Testing Standards
- **Backend:** Unit tests for services, Integration tests for controllers. Aim for 80% coverage.
- **Frontend:** Component tests with Vitest + React Testing Library.
- **Commands:**
    - Backend: `mvn test` (root or service level).
    - Frontend: `npm run test` or `npm run test:coverage`.

## Ongoing Initiatives
- **High-Tech UI Revamp:** Currently implementing a premium, modern UI refresh focused on storefront shell and home page. Follows a cool graphite + soft white palette.

## Key File Locations
- **Shared Backend Logic:** `backend/common-lib/src/main/java/com/hypermall/common/`
- **Frontend Components:** `frontend/hypermall-web/src/components/`
- **Docker Config:** `infrastructure/docker/docker-compose.dev.yml`
- **API Reference:** `docs/API.md`
