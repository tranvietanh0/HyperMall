# HyperMall System Architecture

This architecture note captures the logical structure, data pathways, infrastructure topology, and operational caveats of HyperMall. For API details see `docs/API.md`, and for deployment instructions see `docs/DEPLOYMENT.md`.

## 1. High-level Architecture

- **Microservices fabric**: Netflix Eureka (`service-registry`) handles discovery; Spring Cloud Config (`config-server`) supplies centralized configuration; Spring Cloud Gateway (`api-gateway`) is the single ingress layer that validates JWTs, enforces rate limiting (100 req/min anonymous, 500 req/min authenticated), and applies circuit breakers via Resilience4j.
- **Domain services**: Each business module is stateless and either exposes REST APIs (synchronous) or subscribes/publishes RabbitMQ events (asynchronous). Most services persist to their own MySQL schema and rely on `common-lib` for shared contracts.
- **Frontend**: The React SPA (`frontend/hypermall-web`) calls the gateway at `http://localhost:8080/api/*`. It leverages Redux Toolkit slices, axios services, and `React Router DOM 6` routes defined in `src/routes/index.tsx`.

## 2. Service map & ports

| Service | Port | Persistence | Purpose |
|---------|------|-------------|---------|
| `service-registry` | 8761 | – | Eureka discovery + health
| `config-server` | 8888 | – | Cloud config (~`application-{env}.yml`), credentials `config/config123`
| `api-gateway` | 8080 | Redis | Gateway routing, JWT guard, rate limiting, request logging
| `user-service` | 8081 | MySQL | Authentication, profiles, addresses
| `product-service` | 8082 | MySQL + Elasticsearch sync | Catalog, categories, variants
| `cart-service` | 8083 | Redis | Per-user cart cache
| `order-service` | 8084 | MySQL | Orders, order items, RabbitMQ events
| `payment-service` | 8085 | MySQL | Payment intents, callbacks (VNPay, MoMo, ZaloPay)
| `inventory-service` | 8086 | MySQL | Stock tracking, reservations/releases
| `shipping-service` | 8087 | MySQL | Carrier integrations (GHN, GHTK, ViettelPost)
| `promotion-service` | 8088 | MySQL | Vouchers, flash sales
| `review-service` | 8089 | MySQL | Reviews, images, likes
| `search-service` | 8090 | Elasticsearch | Search querying, facets
| `notification-service` | 8091 | MySQL + RabbitMQ | Email/SMS/push templates from events
| `ai-service` | 8092 | MySQL | Experimental analytics/AI hooks
| `media-service` | 8093 | MySQL/local files | Media uploads and metadata
| `seller-service` | 8094 | MySQL | Seller portal, verifications
| `analytics-service` | 8095 | MySQL | Event ingestion, dashboards

## 3. Data flow highlights

- **User registration**: Frontend → Gateway → `user-service` (MySQL) → JWT access + refresh tokens stored in Redis (if configured) → clients store tokens, gateway validates on every request.
- **Product search**: Gateway routes `GET /api/search` → `search-service` → Elasticsearch `products` index; `product-service` maintains the index during CRUD.
- **Checkout/order creation**: `cart-service` reads Redis cart → `order-service` persists order + items in MySQL → publishes `order.created` to RabbitMQ → `notification-service` and `analytics-service` react.
- **Payment loop**: `payment-service` generates gateway link (VNPay/MoMo/ZaloPay), waits for webhook → updates order status via `order-service` → RabbitMQ events propagate.

## 4. Infrastructure & deployment

- **Docker Compose variants**:
  - `docker-compose.dev.yml`: infrastructure stack for local development (MySQL 3306, Redis 6379, RabbitMQ 5672/15672, Elasticsearch 9200).
  - `docker-compose.local.yml`: similar to dev but with local overrides (see `infrastructure/docker`).
  - `docker-compose.prod.yml`: reference for production, not automatically deployed.
  - `docker-compose.monitoring.yml`: Prometheus (9090), Grafana (3001), Alertmanager (9093), and ELK (Elasticsearch logs 9201, Logstash 5044, Kibana 5601).
- **Startup scripts**: `scripts/start-dev.*` waits for MySQL, builds backend, then runs service-registry, config-server, API gateway, user/service/product/cart services plus the React dev server (logs in `logs/`). It does not start other services—trigger them manually via Maven or IDE.
- **Stop script**: `scripts/stop-dev.*` kills the same subset of services and the Vite dev server before tearing down Docker compose. For full cleanup add `docker-compose -f docker-compose.dev.yml down -v`.

## 5. Observability & logging

- **Actuator**: Each Spring Boot service exposes `/actuator/health` (see appendix) and `/actuator/prometheus` when enabled; the gateway aggregates health.
- **Monitoring stack**: Prometheus scrapes metrics, Grafana dashboards exist under `infrastructure/docker/monitoring/grafana/dashboards`, Alertmanager handles alerts, and the ELK stack collects JVM logs.
- **Runtime artifacts**: Keep `hs_err_pid*.log` and `replay_pid*.log` files (located at the repo root) as evidence of JVM crashes; document their stack traces before deleting.

## 6. Operational notes

- **Environment variables** are defined in `backend/.env.example` (DB credentials, Redis host/port, JWT secrets). Add descriptive comments in `docs/DEPLOYMENT.md` before relying on the template.
- **Logs**: `scripts/start-dev.*` writes to `logs/{service}.log`. Monitor them when services fail to register with Eureka.
- **React Router warnings**: Vitest currently logs React Router future flag warnings; capture their text before migrating to v7.
- **Test coverage**: Many services (see `docs/codebase-summary.md`) lack automated `src/test` coverage. Prioritize test additions when touching critical domains.

## 7. Appendix: Health endpoints

| Service | Port | Health endpoint |
|---------|------|-----------------|
| service-registry | 8761 | `/actuator/health` |
| config-server | 8888 | `/actuator/health` |
| api-gateway | 8080 | `/actuator/health` |
| business services | 8081-8095 | `/actuator/health` per module |

## References

- `docs/API.md`
- `docs/DEPLOYMENT.md`
- `docs/LOCAL_TESTING.md`
- `docs/code-standards.md`
- Service-specific README (under each module, if available)
