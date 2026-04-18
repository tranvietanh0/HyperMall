# HyperMall Codebase Summary

This summary orients new contributors and serves as a living index for the backend, frontend, and infrastructure assets that make up HyperMall. Whenever the code changes, update this page and the service-specific docs listed in References.

## Snapshot

| Metric | Value |
|--------|-------|
| Backend services | 18 Spring Boot modules (common-lib + 17 runnable services) |
| Frontend application | 1 React 18 + TypeScript + Vite SPA |
| Infrastructure stacks | 4 Docker compose files (dev/local/prod/monitoring) |
| Languages | Java 17 (Spring Boot), TypeScript 5.3 (Vite) |
| Build tools | Maven 3.8+, npm 11+ (via package-lock) |
| Message broker | RabbitMQ 3.x |
| Search index | Elasticsearch 8.x |

## Backend Landscape

The parent `backend/pom.xml` wires the following modules. Ports assume the default run profile and can be overridden through `application-*.yml`.

| Module | Port | Data persistence | Notes |
|--------|------|------------------|-------|
| `service-registry` | 8761 | N/A | Eureka discovery + health dashboard (credentials `eureka/eureka123`) |
| `config-server` | 8888 | N/A | Spring Cloud Config (credentials `config/config123`) |
| `api-gateway` | 8080 | Redis (rate counters) | Spring Cloud Gateway with JWT auth + Resilience4j rate/circuit knobs |
| `user-service` | 8081 | MySQL | Authentication, profile, addresses, JWT + refresh token issuers |
| `product-service` | 8082 | MySQL | Products, categories, brands, Elasticsearch sync hooks |
| `cart-service` | 8083 | Redis | Per-user cart cache; relies on `redis.lock`? currently lightweight |
| `order-service` | 8084 | MySQL | Orders, order items, RabbitMQ events (`order.created`) |
| `payment-service` | 8085 | MySQL | Payment intents + callbacks to gateways (VNPay, MoMo, ZaloPay) |
| `inventory-service` | 8086 | MySQL | Stock reservations/releases referenced by checkout flow |
| `shipping-service` | 8087 | MySQL | Shipping methods, shipment tracking, carrier integrations |
| `promotion-service` | 8088 | MySQL | Vouchers, flash sales, headline offers |
| `review-service` | 8089 | MySQL | Reviews, review images, likes |
| `search-service` | 8090 | Elasticsearch | Search queries, facets, indexing pipe from product-service |
| `notification-service` | 8091 | MySQL + RabbitMQ | Email/SMS/push templates triggered by RabbitMQ events |
| `ai-service` | 8092 | MySQL | Placeholder for generative features (recommendations, reports) |
| `media-service` | 8093 | MySQL/local storage | Media upload CRUD, file metadata |
| `seller-service` | 8094 | MySQL | Seller profiles, onboarding, analytics delegation |
| `analytics-service` | 8095 | MySQL | Event ingestion, dashboards, seller reports |

### Shared libraries

- `common-lib` supplies `ApiResponse<T>`, `PageResponse<T>`, custom exceptions (`BadRequestException`, `ResourceNotFoundException`, etc.), JWT helpers (`JwtTokenProvider`, `JwtAuthenticationFilter`, `@CurrentUser`), RabbitMQ publishers, and configuration beans (Jackson, async, Redis). It also anchors the `springdoc-openapi-starter` dependencies defined in the parent POM.

## Frontend Overview

The React SPA lives under `frontend/hypermall-web` and bundles the following stack:

- **Core**: React 18.2 + TypeScript 5.3 + Vite 5.1 + TailwindCSS 3.4 + Redux Toolkit 2.2 + Zustand 4.5.
- **Routing**: React Router DOM 6.22 with route definitions seeded in `src/routes/index.tsx` and `src/App.tsx`.
- **State**: Global slices live in `src/store/slices/*`, while `src/services/api.service.ts` and `src/config/api.config.ts` centralize axios configuration.
- **UI & utilities**: Components in `src/components/`, hooks in `src/hooks/`, helpers in `src/utils/`, and TypeScript contracts in `src/types/`. Shared design tokens (Tailwind + clsx) keep spacing/typography consistent.
- **Path aliases** (`tsconfig.json`): `@/*`, `@components/*`, `@pages/*`, `@hooks/*`, `@services/*`, `@store/*`, `@types/*`, `@utils`/`@utils/*`, and `@config/*`.
- **Testing**: Vitest with `src/test/setup.ts` (mocks for `matchMedia`, `localStorage`, `IntersectionObserver`). Commands: `npm run test`, `npm run test:coverage`, `npx vitest run src/path/to.test.ts`.

## Infrastructure & Tooling

- **Docker**: `infrastructure/docker/docker-compose.dev.yml` boots MySQL 8.0, Redis 7, RabbitMQ 3, Elasticsearch 8, supporting the local backend stack. Variants exist for `docker-compose.local.yml`, `docker-compose.prod.yml`, and `docker-compose.monitoring.yml` (Prometheus, Grafana, Alertmanager, ELK).
- **Scripts**: `scripts/start-dev.*` installs dependencies, builds backend (`mvn clean install -DskipTests`), and starts service-registry, config-server, API gateway, user/product/cart services plus the React dev server (logs drop under `logs/`). `stop-dev.*` targets the same subset and brings Docker compose down. Additional services must be launched manually; refer to `docs/system-architecture.md` for port assignments.
- **Monitoring**: `docker-compose.monitoring.yml` orchestrates Prometheus (9090), Grafana (3001), Alertmanager (9093), and an ELK stack (Elasticsearch logs 9201, Logstash 5044, Kibana 5601). The monitoring assets are mounted from `infrastructure/docker/monitoring`.

## Testing & Quality

- **Backend**: `mvn clean install -DskipTests` (for builds); `mvn test` (all modules); `mvn -pl {module} test` for targeted suites. Most existing unit tests live under `user-service`, `service-registry`, and `config-server` (see `target/test-classes`). The remaining modules (analytics, cart, inventory, media, notification, payment, product, promotion, review, search, seller, shipping, common-lib) require new coverage before new features land.
- **Frontend**: `npm run test` (watch mode), `npm run test:coverage`, and `npx vitest run` (single-run). Tests rely on the shared setup file referenced above; keep the `window` mocks in sync with the UI.
- **Linting**: ESLint + TypeScript rules (`frontend/.eslintrc.cjs`) and Maven formatter (compare with `google-java-format`).

## Notable Gaps

- `.env.example` under `backend/` lists the required variables but the README/docs omit explanations. Fill this gap in `docs/DEPLOYMENT.md` or a dedicated env reference.
- React Router v6 warnings surface in Vitest related to future flags when the router tree renders; document the warning text before upgrading to v7.
- Residual `hs_err_pid*.log` and `replay_pid*.log` files live in the repo root as indicators that some JVM runs have crashed; treat them as signal artifacts and clear them after capturing stack traces for triage.

## References

- `docs/system-architecture.md` (service map, ports, data flow)
- `docs/API.md` (REST contracts and gateway routing)
- `docs/DEPLOYMENT.md` (compose scripts, credentials, env vars)
- `docs/LOCAL_TESTING.md` (Maven and Vitest instructions)
- `docs/code-standards.md` (naming, linting, testing expectations)
