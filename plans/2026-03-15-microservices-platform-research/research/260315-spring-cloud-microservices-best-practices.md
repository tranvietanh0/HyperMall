# Research Report: Spring Boot 3 / Spring Cloud E-commerce Platform

Conducted: 2026-03-15

## Scope
- Platform shape: Config Server, Eureka, API Gateway, JWT auth, Redis, RabbitMQ, Elasticsearch, Docker Compose.
- Focus: architecture validation, config/secret management, discovery/gateway drift, deployment/config consistency, maintainability, security.
- Source limit respected: 2 total, both authoritative docs.

## Executive Summary
- The biggest operational risk is config and version drift, not framework mechanics. In a stack like this, failures often come from mismatched Spring Boot/Spring Cloud versions, stale gateway routes, Eureka metadata/health misregistration, and services starting with partial config because `optional:configserver:` masked a real failure. [S1]
- Treat Config Server as the source of truth for non-secret config, but not as a reason to keep secrets sloppy. Encrypt values at rest if they live in config repos, tightly secure Config Server and actuator endpoints, and keep JWT signing keys outside normal app config whenever possible. JWT should use explicit algorithm validation, strong keys, short lifetimes, and a revocation strategy. [S1][S2]

## Best Practices

### 1. Architecture Validation
- Pin a known-compatible Spring Boot + Spring Cloud BOM and keep compatibility verification enabled; official Spring Cloud docs show startup checks for incompatible Boot versions. This should be a CI gate, not a runtime surprise. [S1]
- Standardize `spring.application.name`, Eureka service IDs, gateway route IDs, config labels, and Compose service names. One canonical name per service avoids discovery, routing, and config lookup drift. [S1]
- Prefer explicit readiness checks for Config Server, Eureka, Gateway, Redis, RabbitMQ, and Elasticsearch before declaring a service ready; registration alone is not proof that downstream dependencies are usable.
- Validate gateway route tables after deploy via actuator route inspection, not only YAML review; the gateway exposes route and filter visibility plus route-cache refresh endpoints for this purpose. [S1]

### 2. Config and Secret Management
- Use `spring.config.import=configserver:` for required remote config; reserve `optional:configserver:` for local/dev or carefully justified degraded modes. Omitting `optional:` gives fail-fast semantics. [S1]
- Configure retry/backoff for Config Client startup and either multiple Config Server URLs or discovery-first lookup if Config Server coordinates can move. [S1]
- Keep secrets out of plain Git-backed config where possible; if values must live there, use Spring Cloud Config encryption (`{cipher}`) and secure `/encrypt` and `/decrypt` endpoints. [S1]
- Add actuator to Config Server and separate actuator access from config API access; otherwise `/actuator/*` patterns can collide with config lookup paths and leak sensitive data. [S1]
- Keep JWT signing material outside normal shared config flows when practical; at minimum, use strong unique secrets or asymmetric keys and rotate them deliberately. [S2]

### 3. Discovery and Gateway Drift Control
- Enable Eureka health propagation so non-`UP` instances stop receiving traffic, and set it in `application.yml`, not `bootstrap.yml`, to avoid `UNKNOWN` registration status. [S1]
- Ensure Eureka metadata is accurate: health URL, status URL, HTTPS flags, forwarded headers, and unique instance IDs. Bad metadata causes "service is up but unreachable" failures. [S1]
- Expect registration convergence lag; Eureka can take up to roughly 3 heartbeats before clients see a new instance. Avoid declaring incidents too early during startup rollouts. [S1]
- Be cautious with Gateway `DiscoveryClient` route locator. It is convenient, but default `/serviceId/**` routing can expose services unintentionally or mask route ownership drift if naming is inconsistent. [S1]
- Set global and per-route gateway timeouts and rate limits; otherwise one slow downstream can consume gateway threads/connections and cascade failures. Redis-backed `RequestRateLimiter` is the built-in pattern. [S1]

### 4. JWT and Edge Security
- Explicitly enforce the expected JWT signing algorithm during validation; do not rely on library defaults. [S2]
- Use short-lived access tokens, a revocation path (denylist or equivalent), and refresh-token/session strategy appropriate for your clients. JWT has no built-in logout. [S2]
- Do not place sensitive internal data in JWT claims; JWT payloads are only encoded, not encrypted by default. [S2]
- For browser clients, reduce replay risk with hardened cookies/fingerprints and CSP; if tokens are cookie-based, also address CSRF. [S2]
- Add gateway security headers and rate limiting at the edge; Spring Cloud Gateway includes `SecureHeaders` and `RequestRateLimiter` support. [S1]

### 5. Deployment and Maintainability
- Keep one per-environment contract document covering ports, service names, profiles, labels, secrets source, health endpoints, and dependency URIs for Compose and non-Compose deployments.
- Version config changes with app changes when behavior depends on both; if not possible, at least pin config labels/branches to release versions instead of mutable defaults. [S1]
- Avoid hot-refreshing Eureka client settings in production unless necessary; Spring warns refresh can briefly unregister instances. [S1]
- Prefer boring, explicit ownership: gateway routes owned by gateway, domain events owned by RabbitMQ contracts, search schema owned by Elasticsearch mappings, cache keys/TTLs owned by service code.

## Common Failure Modes
- `optional:configserver:` left enabled in prod, service starts with local defaults, then fails later in harder-to-debug ways. [S1]
- Boot/Cloud release mismatch compiles but fails at startup or exhibits subtle autoconfiguration issues; compatibility verifier catches only part of this. [S1]
- Eureka `defaultZone` casing or URL mistakes break registration; `serviceUrl.defaultZone` is case-sensitive. [S1]
- Health check settings placed in `bootstrap.yml` register instances as `UNKNOWN`, causing odd routing behavior. [S1]
- HTTPS/proxy metadata wrong in Eureka, so gateway or clients call bad host/protocol/port values. [S1]
- Gateway route cache not refreshed after route source changes, so observed traffic does not match committed config. [S1]
- Missing gateway timeouts or weak rate-limit keys causes hanging requests or accidental blanket 429s; empty rate-limit keys are denied by default. [S1]
- JWT secrets are weak/shared across environments, algorithm validation is loose, or no revocation exists after token theft. [S2]
- Config Server actuator/config path exposure or over-broad credentials leaks configuration data. [S1]

## Practical Recommendations
- Make CI block on: Boot/Cloud compatibility, Config Server fetch, Eureka registration, gateway route audit, and a minimal auth smoke test.
- Use one release checklist: confirm config label, Compose env file/version, gateway routes, Eureka metadata, JWT key version, Redis/RabbitMQ/Elasticsearch endpoints.
- Store non-secret distributed config in Config Server; store true secrets in a dedicated secret system when feasible, or encrypted values with strict access controls as an interim step. [S1]
- Keep JWT simple: signed, short-lived, minimally scoped, revocable, and validated identically at gateway and services. [S2]

## Source Notes
- [S1] Spring Cloud Reference Documentation, sections on compatibility verification, Config Server/Client, Gateway, and Eureka: https://docs.spring.io/spring-cloud/docs/current/reference/htmlsingle/
- [S2] OWASP JSON Web Token for Java Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_for_Java_Cheat_Sheet.html

## Unresolved Questions
- Which exact Spring Cloud release train is paired with this repo's stated Spring Boot `3.4.3`, and is that pairing officially supported in the build today?
- Are JWTs validated only at the gateway, only in downstream services, or both, and is there a shared revocation/key-rotation strategy?
- Are Config Server secrets merely encrypted in Git, or sourced from a dedicated secret manager?
- Are gateway routes explicit, discovery-generated, or mixed, and who owns route review during releases?
- What are the current readiness/health criteria for Redis, RabbitMQ, and Elasticsearch in Docker Compose and non-local deployments?
