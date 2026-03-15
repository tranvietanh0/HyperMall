# Phase 01: Edge and Control Plane Security

## Context links
- `backend/api-gateway/src/main/resources/application.yml`
- `backend/api-gateway/src/main/java/com/hypermall/gateway/config/GatewayConfig.java`
- `backend/api-gateway/src/main/java/com/hypermall/gateway/filter/AuthenticationFilter.java`
- `backend/service-registry/src/main/java/com/hypermall/registry/config/SecurityConfig.java`
- `plans/2026-03-15-microservices-platform-research/research/260315-spring-cloud-microservices-best-practices.md`

## Overview with date/priority/statuses
- Date: 2026-03-15
- Priority: Critical
- Plan status: Drafted
- Implementation status: Not started

## Key Insights
- `spring.cloud.gateway.discovery.locator.enabled=true` can expose implicit routes outside `GatewayConfig` policy.
- `GatewayConfig.java` attaches auth filter, but rate limiting is not visibly bound to explicit routes.
- `SecurityConfig.java` hardcodes `eureka/eureka123`, creating secret drift and copy-paste risk.

## Requirements
- Ensure only approved gateway routes are reachable for protected APIs.
- Bind auth, rate limit, and security-header policy through explicit gateway ownership.
- Externalize control-plane credentials and remove code-level secrets.

## Architecture
- Prefer explicit route table as source of truth; if discovery locator remains for dev, scope it behind profile and deny protected prefixes.
- Split gateway filters into reusable route policies: public, authenticated, admin, callback.
- Keep registry credentials in env/secret source; config code reads values only.

## Related code files
- `backend/api-gateway/src/main/resources/application.yml`
- `backend/api-gateway/src/main/java/com/hypermall/gateway/config/GatewayConfig.java`
- `backend/api-gateway/src/main/java/com/hypermall/gateway/filter/AuthenticationFilter.java`
- `backend/service-registry/src/main/java/com/hypermall/registry/config/SecurityConfig.java`
- `backend/config-server/src/main/resources/configurations/api-gateway.yml`

## Implementation Steps
1. Audit all reachable gateway routes, including discovery-generated ones, and classify public vs protected vs internal-only.
2. Disable discovery locator by default or gate it to a dev profile with explicit allowlist semantics.
3. Refactor gateway route config so auth and rate-limit policies are attached intentionally per route group.
4. Add gateway validation checks: route inventory, auth-required smoke tests, and 429 behavior for protected groups.
5. Replace hardcoded Eureka credentials with env-backed values and document local-dev injection path.

## Todo list
- [ ] Inventory actual gateway route table and hidden discovery routes
- [ ] Decide prod default for discovery locator
- [ ] Attach rate limiter to protected routes and callbacks only where needed
- [ ] Externalize registry username/password from Java config
- [ ] Define smoke tests for auth bypass and rate-limit enforcement

## Success Criteria
- Protected APIs are unreachable without gateway auth regardless of service discovery state.
- Gateway route table is explicit, reviewable, and testable.
- Registry credentials no longer exist in source code.

## Risk Assessment
- Highest risk is breaking legitimate routes when disabling discovery locator.
- Mitigate with route inventory snapshot and staged rollout by route group.

## Security Considerations
- Verify JWT validation path still enforces expected algorithm and claim handling.
- Avoid exposing actuator or docs endpoints broadly while adjusting route rules.

## Next steps
- After Phase 1 lands, freeze new gateway path additions until Phase 2 reconciles service inventory.

## Unresolved Questions
- Is any current traffic depending on `/service-id/**` discovery routes?
- Should rate-limit keys differ for anonymous vs authenticated users at gateway now or later?
