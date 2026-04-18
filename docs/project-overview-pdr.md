# HyperMall Project Overview & Product Development Requirements (PDR)

HyperMall is a multi-tenant e-commerce platform that lifts the feature set of leading marketplaces (Shopee, Lazada) into a Spring Cloud + React stack. This document keeps high-level goals, module ownership, and risk signals aligned with the codebase; the detailed runbooks live under `docs/API.md`, `docs/DEPLOYMENT.md`, and `docs/LOCAL_TESTING.md`.

## Scope & Key Modules

| Layer | Module | Responsibility | Port/Data |
|-------|--------|----------------|-----------|
| Core | `common-lib` | Shared DTOs, exceptions, security utilities, event wiring, and SpringDoc/OpenAPI helpers | N/A |
| Infrastructure | `service-registry`, `config-server`, `api-gateway` | Eureka registration, centralized configuration, routing/authen/observability | 8761 / 8888 / 8080|
| Business services | `user`, `product`, `cart`, `order`, `payment`, `inventory`, `shipping`, `promotion`, `review`, `search`, `notification`, `ai`, `media`, `seller`, `analytics` | Domain APIs, persistence, RabbitMQ/Redis integrations, Elasticsearch indexing | 8081–8095 (see `docs/system-architecture.md`) |

## Feature Pillars

- **Customers**: registration/login, advanced filters, Elasticsearch product search, cart state persisted in Redis, checkout with voucher/promotion application, reviews/ratings, multiple payment flows (VNPay, MoMo, ZaloPay, COD), and order tracking.
- **Sellers**: onboarding, product CRUD (variants/images), stock reconciliation, order management, voucher/promotion publishing, and dashboards driven by `analytics-service` events.
- **Admins**: platform configuration (categories, brands, users), monitoring dashboards, rate limiting, and policy enforcement across services.
- **Payments & Shipping**: JWT-secured payment creation (15 min token / 7 day refresh) with local gateway hooks, plus shipping orchestration for GHN, GHTK, ViettelPost.
- **Platform reliability**: API Gateway enforces rate limits/circuit breakers, Spring Cloud Config governs environment overrides, and observability stacks (Prometheus, Grafana, Alertmanager, ELK) collect metrics/logs via `infrastructure/docker/docker-compose.monitoring.yml`.

## Current State & Observations

- **Documentation posture**: `docs/` is the source of truth; treat `README.md` as a navigation layer and update the detailed guides (`API`, `DEPLOYMENT`, `LOCAL_TESTING`) whenever code or infrastructure changes.
- **Testing gaps**: The following modules do not yet include automated tests under `src/test`: `analytics-service`, `cart-service`, `inventory-service`, `media-service`, `notification-service`, `payment-service`, `product-service`, `promotion-service`, `review-service`, `search-service`, `seller-service`, `shipping-service`, and `common-lib`. Coverage defenses are thin; prioritize controller/service unit tests or integration suites before touching production APIs.
- **Frontend contracts**: `frontend/hypermall-web` relies on `src/store` (Redux Toolkit slices) and `src/services` (axios wrappers) but lacks a living spec. Update `docs/code-standards.md` if the store/services contract changes, and note that Vitest runs against `src/test/setup.ts` (mocks for `matchMedia`, `localStorage`, `IntersectionObserver`). React Router v6 currently emits future-flag warnings when the test runner spawns; capture the warning text in doc updates if the next upgrade hits v7.
- **Environment configuration**: `backend/.env.example` lists variables but omits descriptions. Add context to `docs/DEPLOYMENT.md` or a dedicated env reference before onboarding new teammates.
- **Startup scripts**: `scripts/start-dev.*` and `scripts/stop-dev.*` bring up infrastructure plus service-registry, config-server, API gateway, and the user/product/cart services only. Additional services must be launched manually or via IDE run configs; logs stream into `logs/` under the repo root. The scripts assume Docker resources use default ports (MySQL 3306, Redis 6379, RabbitMQ 5672, Elasticsearch 9200).
- **Runtime health signals**: Residual `hs_err_pid*.log` and `replay_pid*.log` files at the repo root signal past JVM crashes; keep them around as indicators of unstable runs, but clean them before shipping (and document any recurring stack traces in `docs/DEPLOYMENT.md`).

## Risk & Release Management

- **Drift vs. deployment guides**: Double-check port numbers/credentials in `README.md`, `docs/DEPLOYMENT.md`, and `scripts/*` whenever a module or compose file changes; discrepancies keep cropping up because multiple docs describe the same ports. Prefer `docs/system-architecture.md` for architecture-level references.
- **Gap tracking**: Keep this doc updated with new milestones. The next priority is stabilizing payment/shipping integration coverage and surface-level analytics dashboards for seller/admin workflows. Document incremental milestones (in `PLAN.md` if present) so the roadmap and this PDR stay aligned.

## References

- `docs/API.md` (REST contracts and API gateway routes)
- `docs/system-architecture.md` (service map, data flows, observability)
- `docs/DEPLOYMENT.md` (compose files, credentials, environment variables)
- `docs/LOCAL_TESTING.md` (test suites, Vitest/Maven commands)
- `docs/code-standards.md` (naming, linting, and testing expectations)
