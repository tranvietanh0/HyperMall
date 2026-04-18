# HyperMall

HyperMall is a microservices e-commerce platform with a Spring Boot/Spring Cloud backend, a React + Vite frontend, and Docker-based local infrastructure.

`docs/` is the source of truth for architecture, standards, deployment, and testing. This README is intentionally short and acts as the project entry point.

## Stack

- Backend: Java 17, Spring Boot 3.4.3, Spring Cloud 2024.0.0, Maven
- Frontend: React 18, TypeScript, Vite, Tailwind CSS, Redux Toolkit, Vitest
- Infrastructure: MySQL, Redis, RabbitMQ, Elasticsearch, Docker Compose
- Platform services: Eureka, Spring Cloud Config, Spring Cloud Gateway, Prometheus/Grafana/ELK (monitoring compose)

## Repository Layout

```text
HyperMall/
|- backend/                     Maven multi-module microservices
|- frontend/hypermall-web/      React application
|- infrastructure/docker/       Docker Compose stacks
|- docs/                        Canonical documentation
|- scripts/                     Local start/stop helpers
|- PLAN.md                      Planning notes
`- REQUIREMENTS.md              Product requirements
```

## Backend Modules

- Core: `common-lib`, `service-registry`, `config-server`, `api-gateway`
- Domain: `user-service`, `product-service`, `cart-service`, `order-service`, `payment-service`, `inventory-service`, `shipping-service`, `promotion-service`, `review-service`, `search-service`, `notification-service`, `ai-service`, `media-service`, `seller-service`, `analytics-service`

See `docs/system-architecture.md` and `docs/codebase-summary.md` for the detailed service map.

## Quick Start

### 1. Start infrastructure

```bash
cd infrastructure/docker
docker-compose -f docker-compose.dev.yml up -d
```

### 2. Build backend

```bash
cd backend
mvn clean install -DskipTests
```

### 3. Start core services in order

```bash
mvn -pl service-registry spring-boot:run
mvn -pl config-server spring-boot:run
mvn -pl api-gateway spring-boot:run
```

Then start the business services you need, for example:

```bash
mvn -pl user-service spring-boot:run
mvn -pl product-service spring-boot:run
mvn -pl cart-service spring-boot:run
```

### 4. Start frontend

```bash
cd frontend/hypermall-web
npm install
npm run dev
```

## Local Endpoints

- Frontend: `http://localhost:3000`
- API Gateway: `http://localhost:8080/api/*`
- Eureka: `http://localhost:8761`
- RabbitMQ UI: `http://localhost:15672`

## Testing

### Backend

```bash
cd backend
mvn test
```

### Frontend

```bash
cd frontend/hypermall-web
npm run test
npm run test:coverage
```

## Documentation

- `docs/project-overview-pdr.md`: project scope, goals, current risk signals
- `docs/codebase-summary.md`: codebase inventory and module map
- `docs/code-standards.md`: coding, testing, and documentation conventions
- `docs/system-architecture.md`: service architecture, ports, infra topology
- `docs/API.md`: API reference
- `docs/DEPLOYMENT.md`: deployment and environment guidance
- `docs/LOCAL_TESTING.md`: local verification workflows

## Current Project Signals

- Several backend modules still have little or no automated test coverage
- `scripts/start-dev.*` only starts a subset of services; full-stack local runs still require manual startup
- Existing JVM crash logs (`hs_err_pid*.log`, `replay_pid*.log`) indicate previous runtime instability that should be reviewed before production hardening
- Frontend test runs currently surface React Router future-flag warnings

## Notes

- Keep detailed documentation updates in `docs/` whenever service ports, environment variables, scripts, or architecture change.
- Avoid treating this README as the full spec; link back to the docs instead.
